(ns caiorulli.vega.commands
  (:require [caiorulli.vega.protocols :as protocols]
            [caiorulli.vega.utils :refer [default-zone try-get]]
            [clojure.data.xml :as xml]
            [clojure.string :as s]
            [datahike.api :as d]
            [environ.core :refer [env]]
            [taoensso.timbre :as log]
            [tick.core :as t]))

(def ^:const backoff 200)
(def rss-cache (atom {}))

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
              (t/in (t/now) (or zone-id default-zone)))))

(defn reaction
  [api db-setup {{id :id} :chat
                 text     :text}]
  (let [[_ trigger sentence] (filter (complement s/blank?) (s/split text #"\""))

        conn (d/connect db-setup)]

    (d/transact conn [#:reaction {:trigger  trigger
                                  :sentence sentence}])
    (protocols/send-text api id "Reaction added successfully.")))

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

    (protocols/send-text api id text)))


(defn- fetch-rss
  [subreddit]
  (if-let [cached-feed (get @rss-cache subreddit)]
    (do
      (log/info "Using cached rss feed.")
      cached-feed)

    (let [url (str "https://reddit.com/r/" subreddit ".rss")]
      (loop [result (try-get url)]

        (if (seq result)
          (let [feed (:body result)]
            (swap! rss-cache #(assoc % subreddit feed))
            feed)

          (do
            (log/warn "Retrying: fetch rss feed")
            (Thread/sleep backoff)
            (recur (try-get url))))))))

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
  [api _db-setup {{id :id} :chat
                  text     :text}]
  (let [subreddit (or (second (s/split text #" ")) default-subreddit)
        amount    (Integer/parseInt (nth (s/split text #" ") 2 "1"))

        rss  (fetch-rss subreddit)
        urls (reddit-image-urls rss)]

    (when (seq urls)
      (doseq [url (take amount (shuffle urls))]
        (future (protocols/send-photo api id url (str "Random image from r/" subreddit)))))))

(defn start
  [api _db-setup {{id :id :as chat} :chat}]
  (log/info "Bot joined new chat: " chat)
  (protocols/send-text api id "Vega initialized."))

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
  (log/info "Help was requested in " chat)
  (protocols/send-text api id help-text))

(defn time-command
  [api db-setup {:keys [text chat]}]
  (protocols/send-text api (:id chat) (friend-time db-setup
                                                  (second (s/split text #" ")))))
