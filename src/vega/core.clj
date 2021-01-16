(ns vega.core
  (:require [clojure.core.async :refer [<!!]]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [java-time :as t]
            [morse.api :as api]
            [morse.handlers :refer [defhandler command-fn message-fn]]
            [morse.polling :as polling])
  (:gen-class))

(def token (env :telegram-token))

(defn now
  []
  (t/local-date-time))

(def modifier
  {"thiago"   #(t/plus % (t/hours 3))
   "pedrotti" #(t/plus % (t/hours 4))
   "castro"   #(t/minus % (t/hours 1))})

(defn friend-time
  [friend]
  (let [modifier-fn (get modifier friend identity)]
    (-> (now) modifier-fn str)))

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
  [& _args]
  (when (s/blank? token)
    (println "Please provide token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (println "Starting Vega...")
  (<!! (polling/start token handler)))
