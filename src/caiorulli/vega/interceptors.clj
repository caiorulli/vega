(ns caiorulli.vega.interceptors
  (:require [caiorulli.vega.protocols :as protocols]
            [clojure.string :as s]
            [datahike.api :as d]
            [taoensso.timbre :as log]))

(defn reaction
  [api db-setup {:keys [text chat]}]
  (let [conn      (d/connect db-setup)
        reactions (into {} (d/q '[:find ?t ?s
                                  :where
                                  [?e :reaction/trigger ?t]
                                  [?e :reaction/sentence ?s]]
                                @conn))]

    (doseq [trigger (keys reactions)]
      (when (and text (s/includes? text trigger))
        (protocols/send-text api (:id chat) (get reactions trigger))))))

(defn default
  [_api _db-setup message]
  (log/info (str "Intercepted message: " message)))
