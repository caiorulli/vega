(ns vega.commands
  (:require [clojure.data.xml :as xml]
            [clojure.string :as s]
            [datahike.api :as d]
            [environ.core :refer [env]]
            [java-time :as t]
            [taoensso.timbre :as timbre]
            [vega.core :refer [now default-zone]]
            [vega.protocols.reddit :as reddit]
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

(defn- with-tag
  [tag node]
  (filter #(= (name (:tag %)) tag) node))

(defn- reddit-image-urls
  [rss-feed]
  (let [xml-map (xml/parse-str rss-feed)

        entries (->> (:content xml-map)
                     (with-tag "entry")
                     (mapcat :content)
                     (with-tag "content")
                     (map (comp first :content))
                     (filter #(s/starts-with? % "<table>")))]

    (->> entries
         (map xml/parse-str)
         (map (fn [html]
                (some-> html
                        :content
                        second
                        :content
                        second
                        :content
                        (nth 5 {})
                        :content
                        first
                        :attrs
                        :href)))
         (filter identity))))

(defn reddit
  [api _db-setup reddit-api {{id :id} :chat
                             text     :text}]
  (let [subreddit (or (second (s/split text #" ")) default-subreddit)
        amount    (Integer/parseInt (nth (s/split text #" ") 2 "1"))

        rss  (reddit/rss reddit-api subreddit)
        urls (reddit-image-urls rss)]

    (when (seq urls)
      (doseq [url (take amount (shuffle urls))]
        (telegram/send-photo api id url (str "Random image from r/" subreddit))))))

(defn start
  [api _db-setup {{id :id :as chat} :chat}]
  (timbre/info "Bot joined new chat: " chat)
  (telegram/send-text api id "Vega initialized."))

(def ^:private help-text "
Available commands:
/help - Display this help text.
/time [friend name] - Displays current time for your friend's timezone. Be sure to check on this only after you bombard him with messages late at night.
/reaction [trigger] [sentence] - Ensures vega will say [sentence] whenever anyone says [trigger].
/reaction_list - Lists registered reactions.
/reddit [subreddit?] - Gets random image from specified subreddit. If no subreddit is specified, fetches random wallpaper
")

(defn help
  [api, _db-setup {{id :id :as chat} :chat}]
  (timbre/info "Help was requested in " chat)
  (telegram/send-text api id help-text))

(defn time-command
  [api db-setup {:keys [text chat]}]
  (telegram/send-text api (:id chat) (friend-time db-setup
                                                  (second (s/split text #" ")))))
