(ns vega.core
  (:require [clojure.core.async :refer [<!!]]
            [clojure.string :as s]
            [datahike.api :as d]
            [environ.core :refer [env]]
            [integrant.core :as ig]
            [java-time :as t]
            [morse.api :as api]
            [morse.handlers :refer [defhandler command-fn message-fn]]
            [morse.polling :as polling]
            [taoensso.timbre :as timbre])
  (:gen-class))

(def db-config
  {:store      {:backend :file
                :path    "/tmp/vegadb"}
   :initial-tx [{:db/ident       :friend/name
                 :db/valueType   :db.type/string
                 :db/cardinality :db.cardinality/one}
                {:db/ident       :friend/zone-id
                 :db/valueType   :db.type/string
                 :db/cardinality :db.cardinality/one}]})

(def config
  {:database db-config})

(defmethod ig/init-key :database [_ opts]
  (when-not (d/database-exists? opts)
    (d/create-database opts)

    (let [conn (d/connect opts)]
      (d/transact conn [#:friend {:name    "thiago"
                                  :zone-id "Europe/Lisbon"}
                        #:friend {:name    "pedrotti"
                                  :zone-id "Europe/Berlin"}
                        #:friend {:name    "castro"
                                  :zone-id "America/Campo_Grande"}])))

  opts)

(def token (env :telegram-token))
(def default-zone (t/zone-id "America/Sao_Paulo"))

(defn now
  []
  (t/zoned-date-time default-zone))

(defn friend-time
  [friend]
  (let [conn      (d/connect db-config)
        [zone-id] (d/q '[:find [?z]
                         :in $ ?name
                         :where
                         [?f :friend/name ?name]
                         [?f :friend/zone-id ?z]]
                       @conn friend)]
    (t/format "HH:mm"
              (t/with-zone-same-instant (now) (or zone-id
                                                  default-zone)))))

(defhandler handler

  (command-fn "start"
    (fn [{{id :id :as chat} :chat}]
      (println "Bot joined new chat: " chat)
      (api/send-text token id "Vega initialized.")))

  (command-fn "help"
    (fn [{{id :id :as chat} :chat}]
      (println "Help was requested in " chat)
      (api/send-text token id "No.")))

  (command-fn "time"
    (fn [{:keys [text chat]}]
      (api/send-text token (:id chat) (friend-time (second (s/split text #" "))))))

  (message-fn
    (fn [{:keys [text chat]}]
      (when (s/includes? (s/lower-case text) "this is the way")
        (api/send-text token (:id chat) "This is the way."))))

  (message-fn
    (fn [message]
      (println "Intercepted message: " message)
      (println "Not doing anything with this message."))))

(defn start
  [{:keys [log-level]
    :or   {log-level :info}}]

  (timbre/set-level! log-level)

  (when (s/blank? token)
    (timbre/info "Please provide token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (ig/init config)

  (println "Starting Vega...")
  (<!! (polling/start token handler
                      {:timeout 4})))

(defn -main
  []
  (start {}))
