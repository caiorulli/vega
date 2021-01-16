(ns vega.core
  (:require [clojure.core.async :refer [<!!]]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [java-time :as t]
            [morse.api :as api]
            [morse.handlers :refer [defhandler command-fn message-fn]]
            [morse.polling :as polling]
            [taoensso.timbre :as timbre])
  (:gen-class))

(def token (env :telegram-token))
(def default-zone (t/zone-id "America/Sao_Paulo"))

(defn now
  []
  (t/zoned-date-time default-zone))

(def friend->zone
  {"thiago"   "Europe/Lisbon"
   "pedrotti" "Europe/Berlin"
   "castro"   "America/Campo_Grande"})

(defn friend-time
  [friend]
  (let [zone-id (get friend->zone friend default-zone)]
    (t/format "HH:mm"
              (t/with-zone-same-instant (now) zone-id))))

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

  (println "Starting Vega...")
  (<!! (polling/start token handler
                      {:timeout 4})))

(defn -main
  []
  (start {}))
