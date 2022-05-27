(ns caiorulli.vega.test-helpers
  (:require [caiorulli.vega.consumer :as consumer]
            [caiorulli.vega.protocols.error-reporting :as error-reporting]
            [caiorulli.vega.protocols.telegram :as telegram]
            [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]
            [datahike.api :as d]
            [taoensso.timbre :as log]))

(defrecord MorseMockApi [requests]
  telegram/TelegramApi
  (send-text [_this _chat-id text]
    (swap! requests conj text))

  (send-photo [_this _chat-id url _caption]
    (swap! requests conj url)))

(defrecord MockErrorReporting []
  error-reporting/ErrorReporting
  (init! [_] nil)
  (send-event [_ event]
    (log/error "Error reported during testing" event)))

(def db-config
  {:initial-tx (-> "schema.edn"
                   io/resource
                   slurp
                   edn/read-string)})

(def ^:dynamic *api*)
(def ^:dynamic *error-reporting*)

(defn with-context-fn
  [inner-fn]
  (binding [*api*             (->MorseMockApi (atom []))
            *error-reporting* (->MockErrorReporting)]
    (d/create-database db-config)
    (try
      (inner-fn)
      (catch Throwable t
        (log/error "Error in test thread" t)
        t)
      (finally
        (d/delete-database db-config)))))

(defmacro with-context
  [& body]
  `(with-context-fn
     (fn []
       ~@body)))

(defn consumer
  [producer]
  (consumer/create *api* db-config *error-reporting* producer))

(defn requests
  []
  (-> *api* :requests deref))
