(ns caiorulli.vega.producer
  (:require [caiorulli.vega.protocols.error-reporting :as error-reporting]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [clj-http.conn-mgr :as cm]
            [clojure.core.async :refer [chan go go-loop close! <! onto-chan!]]
            [integrant.core :as ig]
            [taoensso.timbre :as log]))

(def base-url "https://api.telegram.org/bot")

(defn- new-offset
  [updates]
  (let [ids (map :update_id updates)]
    (when (seq ids)
      (inc (apply max ids)))))

(defn- handle-response
  [producer offset]
  (fn [response]
    (go
      (let [updates (-> response
                        :body
                        (json/parse-string true)
                        :result)]

        (log/debug "Updating offset...")
        (when-let [next-offset (new-offset updates)]
          (reset! offset next-offset))

        (log/debug "Forwarding updates...")
        (onto-chan! producer updates)))))

(defn- handle-error
  [error-reporting]
  (fn [e]
    (error-reporting/send-event error-reporting
                                {:message   "Error fetching updates"
                                 :throwable e})))

(defn- create-producer
  [token error-reporting scheduler]
  (let [producer (chan 4)
        offset   (atom 0)
        cm       (cm/make-reusable-async-conn-manager {})
        url      (str base-url token "/getUpdates")]

    (add-watch offset :log
               (fn [_ _ _ new-state]
                 (log/debug (str "Offset updated to " new-state "."))))

    (go-loop []
      (if-let [time (<! scheduler)]
        (do
          (log/info (str "Fetching updates at " time "..."))
          (client/get url
                      {:query-params       {:limit  100
                                            :offset @offset}
                       :async?             true
                       :connection-manager cm}
                      (handle-response producer offset)
                      (handle-error error-reporting))

          (log/debug "Waiting for new scheduler event...")
          (recur))

        (close! producer)))

    producer))

(defmethod ig/init-key :core/producer [_ {:keys [token
                                                  error-reporting
                                                  scheduler]}]
  (log/info "Starting producer.")
  (create-producer token error-reporting scheduler))

(defmethod ig/halt-key! :core/producer [_ producer]
  (close! producer))
