(ns caiorulli.vega.producer
  (:require [caiorulli.vega.protocols.error-reporting :as error-reporting]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [clj-http.conn-mgr :as cm ]
            [clojure.core.async :refer [chan go-loop close! <! put!]]
            [integrant.core :as ig]
            [taoensso.timbre :as log]))

(def base-url "https://api.telegram.org/bot")

(defn- parse-response
  [response]
  (-> response :body (json/parse-string true) :result))

(defn- handle-error
  [error-reporting e]
  (error-reporting/send-event error-reporting
                              {:message   "Error fetching updates"
                               :throwable e}))

(defn- create-producer
  [token error-reporting scheduler]
  (let [producer (chan 4 (mapcat parse-response))
        cm       (cm/make-reusable-async-conn-manager {})
        url      (str base-url token "/getUpdates")]

    (go-loop []
      (when-let [time (<! scheduler)]
        (log/info (str "Fetching updates at " time "..."))
        (client/get url
                    {:query-params       {:limit 100}
                     :async?             true
                     :connection-manager cm}
                    (partial put! producer)
                    (partial handle-error error-reporting))

        (log/info "Waiting for new scheduler event...")
        (recur)))

    producer))

(defmethod ig/init-key :core/producer [_ {:keys [token
                                                  error-reporting
                                                  scheduler]}]
  (log/info "Starting producer.")
  (create-producer token error-reporting scheduler))

(defmethod ig/halt-key! :core/producer [_ producer]
  (close! producer))
