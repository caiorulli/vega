(ns caiorulli.vega.scheduler
  (:require [chime.core :as chime]
            [chime.core-async :refer [chime-ch]]
            [clojure.core.async :refer [close!]]
            [integrant.core :as ig]
            [taoensso.timbre :as timbre])
  (:import [java.time Instant Duration]))

(defn create-chime
  [recurrence]
  (chime-ch (chime/periodic-seq (Instant/now)
                                (Duration/ofMinutes recurrence))))

(defmethod ig/init-key :core/scheduler [_ {:keys [recurrence]}]
  (timbre/info "Starting scheduler.")
  (create-chime recurrence))

(defmethod ig/halt-key! :core/scheduler [_ scheduler]
  (close! scheduler))
