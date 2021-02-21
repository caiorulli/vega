(ns vega.commands
  (:require [clojure.data.xml :as xml]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [datahike.api :as d]
            [environ.core :refer [env]]
            [java-time :as t]
            [taoensso.timbre :as timbre]
            [vega.core :refer [try-get blurp! now default-zone]]
            [vega.protocols.telegram :as telegram]))

(def ^:private default-subreddit
  (or (env :default-subreddit) "wallpaper"))

(defn friend-time
  [db-setup friend]
  (let [conn      (d/connect db-setup)
        [zone-id] (d/q '[:find [?z]
                         :in $ ?name
                         :where
                         [?f :friend/name ?name]
                         [?f :friend/zone-id ?z]]
                       @conn friend)]
    (t/format "HH:mm"
              (t/with-zone-same-instant (now) (or zone-id
                                                  default-zone)))))

(defn reaction
  [api db-setup {{id :id} :chat
                 text     :text}]
  (let [[_ trigger sentence] (filter (complement s/blank?) (s/split text #"\""))

        conn (d/connect db-setup)]

    (d/transact conn [#:reaction {:trigger  trigger
                                  :sentence sentence}])
    (telegram/send-text api id "Reaction added successfully.")))

(defn reaction-list
  [api db-setup {{id :id} :chat}]
  (let [conn         (d/connect db-setup)
        reactions (d/q '[:find ?t ?s
                         :where
                         [?e :reaction/trigger ?t]
                         [?e :reaction/sentence ?s]]
                       @conn)

        lines (for [[trigger sentence] reactions]
                (str "\"" trigger "\""
                     " => "
                     "\"" sentence "\"\n"))

        text (apply str (cons "Registered reactions:\n" lines))]

    (telegram/send-text api id text)))

(defn- reddit-rss
  [subreddit]
  (let [url (str "https://reddit.com/r/" subreddit ".rss")]
    (loop [result  (try-get url)
           backoff 1000]
      (if (seq result)
        result
        (do
          (timbre/warn "Retrying: fetch rss feed")
          (Thread/sleep backoff)
          (recur (try-get url)
                 (max 8000 (* 2 backoff))))))))

(defn- reddit-image-urls
  [rss-feed]
  (let [xml-map (xml/parse-str (:body rss-feed))

        entries (->> (:content xml-map)
                     (filter #(= (:tag %) :entry))
                     (mapcat :content)
                     (filter #(= (:tag %) :content))
                     (map (comp first :content)))]

    (->> entries
         (map xml/parse-str)
         (map (fn [html]
                (some-> html
                        :content
                        first
                        :content
                        second
                        :content
                        (nth 3 {})
                        :content
                        first
                        :attrs
                        :href)))
         (filter identity))))

(defn reddit
  [api _db-setup {{id :id} :chat
                  text     :text}]
  (let [subreddit (or (second (s/split text #" ")) default-subreddit)

        rss  (reddit-rss subreddit)
        url  (rand-nth (reddit-image-urls rss))
        file (blurp! url)]

    (telegram/send-photo api id file)
    (io/delete-file file)))

(defn start
  [api _db-setup {{id :id :as chat} :chat}]
  (timbre/info "Bot joined new chat: " chat)
  (telegram/send-text api id "Vega initialized."))

(defn help
  [api, _db-setup {{id :id :as chat} :chat}]
  (timbre/info "Help was requested in " chat)
  (telegram/send-text api id "
Available commands:
/help - Display this help text.
/time [friend name] - Displays current time for your friend's timezone. Be sure to check on this only after you bombard him with messages late at night.
/reaction [trigger] [sentence] - Ensures vega will say [sentence] whenever anyone says [trigger].
/reaction_list - Lists registered reactions.
/reddit [subreddit?] - Gets random image from specified subreddit. If no subreddit is specified, fetches random wallpaper
"))

(defn time-command
  [api db-setup {:keys [text chat]}]
  (telegram/send-text api (:id chat) (friend-time db-setup
                                                  (second (s/split text #" ")))))
