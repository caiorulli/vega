(ns vega.consumer
  (:require [clojure.string :as s]
            [datahike.api :as d]
            [integrant.core :as ig]
            [java-time :as t]
            [morse.handlers :refer [handlers command-fn message-fn]]
            [morse.polling :as polling]
            [taoensso.timbre :as timbre]
            vega.morse
            [vega.protocols.api :as api]))

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

(defn start [api db-setup producer-chan]
  (polling/create-consumer
   producer-chan
   (handlers
    (command-fn "start"
      (fn [{{id :id :as chat} :chat}]
        (timbre/info "Bot joined new chat: " chat)
        (api/send-text api id "Vega initialized.")))

    (command-fn "help"
      (fn [{{id :id :as chat} :chat}]
        (timbre/info "Help was requested in " chat)
        (api/send-text api id "No.")))

    (command-fn "time"
      (fn [{:keys [text chat]}]
        (api/send-text api (:id chat) (friend-time db-setup
                                                   (second (s/split text #" "))))))

    (message-fn
        (fn [{:keys [text chat]}]
          (when (s/includes? (s/lower-case text) "this is the way")
            (api/send-text api (:id chat) "This is the way."))))

    (message-fn
      (fn [message]
        (println "Intercepted message: " message)
        (println "Not doing anything with this message."))))))

(defmethod ig/init-key :core/consumer [_ {:keys [api db-setup producer]}]
  (start api db-setup producer))
