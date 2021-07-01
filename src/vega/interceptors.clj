(ns vega.interceptors
  (:require [clojure.string :as s]
            [datahike.api :as d]
            [taoensso.timbre :as timbre]
            [vega.protocols.telegram :as telegram]))

(defn reaction
  [api db-setup {:keys [text chat]}]
  (let [conn      (d/connect db-setup)
        reactions (into {} (d/q '[:find ?t ?s
                                  :where
                                  [?e :reaction/trigger ?t]
                                  [?e :reaction/sentence ?s]]
                                @conn))]

    (doseq [trigger (keys reactions)]
      (when (s/includes? (s/lower-case text) trigger)
        (telegram/send-text api (:id chat) (get reactions trigger))))))

(defn default
  [_api _db-setup message]
  (timbre/info (str "Intercepted message: " message))
  (timbre/info "Not doing anything with this message."))
