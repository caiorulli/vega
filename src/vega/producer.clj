(ns vega.producer
  (:require [clojure.core.async :refer [close! chan go-loop poll! put! >!]]
            [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            [vega.protocols.error-reporting :as error-reporting]
            [vega.protocols.telegram :as telegram]))

(defn- new-offset
  [updates default]
  (if (seq updates)
    (-> updates last :update_id inc)
    default))

(defn- fetch-updates
  [api error-reporting offset]
  (try
    (telegram/get-updates api offset)

    (catch Throwable t
      (error-reporting/send-event error-reporting
                                  {:message   "Error fetching updates"
                                   :throwable t})
      (timbre/error "Error fetching updates" t)
      [])))

(defn- create-producer
  [api error-reporting stop-chan opts]
  (let [updates-chan (chan)
        timeout      (:timeout opts)]

    (go-loop [offset 0]
      (Thread/sleep timeout)

      (if (poll! stop-chan)
        (close! updates-chan)

        (let [updates     (fetch-updates api error-reporting offset)
              next-offset (if (seq updates)
                            (new-offset updates offset)
                            offset)]

          (doseq [update updates]
            (>! updates-chan update))

          (recur next-offset))))

    updates-chan))

(defmethod ig/init-key :core/producer [_ {:keys [api error-reporting opts]}]
  (timbre/info "Starting producer.")
  (let [stop-chan (chan)]
    {:stop-chan    stop-chan
     :updates-chan (create-producer api error-reporting stop-chan opts)}))

(defmethod ig/halt-key! :core/producer [_ {:keys [stop-chan]}]
  (put! stop-chan true))
