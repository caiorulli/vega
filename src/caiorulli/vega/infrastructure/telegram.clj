(ns caiorulli.vega.infrastructure.telegram
  (:require [caiorulli.vega.core :refer [blurp!]]
            [caiorulli.vega.protocols.telegram :as telegram]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [integrant.core :as ig]
            [morse.api :as api]
            [taoensso.timbre :as timbre]))

(def base-url "https://api.telegram.org/bot")

(defn- -get-updates
  ([{:keys [token limit timeout]} offset]
   (let [url (str base-url token "/getUpdates")]

     (-> (client/get url {:query-params {:timeout timeout
                                         :offset  offset
                                         :limit   limit}})
         :body
         (json/parse-string true)
         :result))))

(defrecord MorseApi [token limit timeout]

  telegram/TelegramApi
  (send-text [this chat-id text]
    (api/send-text (:token this) chat-id text))

  (send-photo [this chat-id url caption]
    (let [file (blurp! url)]

      (api/send-photo (:token this) chat-id
                      {:caption caption}
                      file)
      (io/delete-file file)))

  (get-updates [this]
    (-get-updates this 0))

  (get-updates [this offset]
    (-get-updates this offset)))

(defmethod ig/init-key :telegram/api [_ {:keys [token limit timeout]}]
  (when (s/blank? token)
    (timbre/info "Please provide token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (->MorseApi token limit timeout))
