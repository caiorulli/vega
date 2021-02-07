(ns vega.commands
  (:require [clojure.string :as s]
            [datahike.api :as d]
            [java-time :as t]
            [taoensso.timbre :as timbre]
            [vega.protocols.telegram :as telegram]))

(def default-zone (t/zone-id "America/Sao_Paulo"))

(defn now []
  (t/zoned-date-time default-zone))

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
"))

(defn time-command
  [api db-setup {:keys [text chat]}]
  (telegram/send-text api (:id chat) (friend-time db-setup
                                                  (second (s/split text #" ")))))
