(ns vega.core
  (:require [clojure.core.async :refer [<!!]]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [morse.handlers :as h]
            [morse.polling :as p]
            [morse.api :as t])
  (:gen-class))

(def token (env :telegram-token))

(h/defhandler handler

  (h/command-fn "start"
    (fn [{{id :id :as chat} :chat}]
      (println "Bot joined new chat: " chat)
      (t/send-text token id "Vega initialized.")))

  (h/command-fn "help"
    (fn [{{id :id :as chat} :chat}]
      (println "Help was requested in " chat)
      (t/send-text token id "Help is on the way")))

  (h/message-fn
    (fn [{:keys [text chat]}]
      (when (str/includes? (str/lower-case text) "this is the way")
        (t/send-text token (:id chat) "This is the way."))))

  (h/message-fn
    (fn [message]
      (println "Intercepted message: " message)
      (println "Not doing anything with this message."))))

(defn -main
  [& _args]
  (when (str/blank? token)
    (println "Please provide token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (println "Starting Vega...")
  (<!! (p/start token handler)))
