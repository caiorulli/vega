(ns caiorulli.vega.producer
  (:require [caiorulli.vega.core :as core]
            [caiorulli.vega.protocols.error-reporting :as error-reporting]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.core.async :refer [chan go-loop close! >! <!]]
            [integrant.core :as ig]
            [taoensso.timbre :as timbre]))

(def base-url "https://api.telegram.org/bot")
#_
(def url (str base-url (environ.core/env :telegram-token) "/getUpdates"))
#_
(client/get url {:query-params
                 {:offset 0
                  :limit  100}
                 :throw-exceptions true})

(defn- fetch-updates
  [token error-reporting offset]
  (try
    (let [url (str base-url token "/getUpdates")]
      (timbre/info "Making request...")
      (-> (client/get url {:query-params     {;; :timeout
                                              :offset  offset
                                              :limit   100}
                           :throw-exceptions true})
          :body
          (json/parse-string true)
          :result))

    (catch Throwable t
      (timbre/error "Error fetching updates" t)
      (error-reporting/send-event error-reporting
                                  {:message   "Error fetching updates"
                                   :throwable t})
      [])))

(defn- new-offset
  [updates]
  (-> updates last :update_id inc))

(defn- create-producer
  [token error-reporting scheduler]
  (let [producer (chan)]

    (go-loop [offset 0]
      (when-let [time (<! scheduler)]
        (timbre/info (str "Fetching updates at " time "..."))
        (let [updates     (fetch-updates token error-reporting offset)
              next-offset (if (seq updates)
                            (new-offset updates)
                            offset)]

          (doseq [update updates]
            (timbre/info "Sending update to producer channel!")
            (>! producer update))

          (timbre/info "Waiting for new scheduler event...")
          (recur next-offset))))

    producer))

(defmethod ig/init-key ::core/producer [_ {:keys [token
                                                  error-reporting
                                                  scheduler]}]
  (timbre/info "Starting producer.")
  (create-producer token error-reporting scheduler))

(defmethod ig/halt-key! ::core/producer [_ producer]
  (close! producer))
