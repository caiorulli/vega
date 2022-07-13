(ns caiorulli.vega.test-helpers
  (:require [caiorulli.vega.consumer :as consumer]
            [caiorulli.vega.protocols :as protocols]
            [caiorulli.vega.utils :refer [edn-from-resource]]
            [clojure.core.async :refer [chan onto-chan!! <!! >!! close!]]
            [datahike.api :as d]
            [taoensso.timbre :as log]))

(defrecord MorseMockApi [requests]
  protocols/TelegramApi
  (send-text [_this _chat-id text]
    (swap! requests conj text))

  (send-photo [_this _chat-id url _caption]
    (swap! requests conj url)))

(def db-config
  {:initial-tx (concat
                (edn-from-resource "schema.edn")
                (edn-from-resource "seed.edn"))})

(def ^:dynamic *api*)

(defn- set-global-error-log!
  []
  (Thread/setDefaultUncaughtExceptionHandler
   (reify Thread$UncaughtExceptionHandler
     (uncaughtException [_ _thread t]
       (log/error "Error occurred during test execution." t)))))

(defn with-context-fn
  [inner-fn]
  (binding [*api* (->MorseMockApi (atom []))]
    (set-global-error-log!)
    (d/create-database db-config)
    (try
      (inner-fn)
      (catch Throwable t
        (log/error "Error in test thread" t)
        (throw t))
      (finally
        (d/delete-database db-config)))))

(defmacro with-context
  [& body]
  `(with-context-fn
     (fn []
       ~@body)))

(defn- consumer
  [producer]
  (consumer/create *api* db-config producer))

(defn requests
  []
  (-> *api* :requests deref))

(defn reset-requests!
  []
  (reset! (:requests *api*) []))

(defn- message
  [text]
  {:message {:text text
             :chat 1}})

(defn execute!
  [& messages]
  (let [producer (chan)
        consumer (consumer producer)]
    (onto-chan!! producer (map message messages))
    (<!! consumer)))

(defn exec!
  [msg]
  (when-not *api*
    (throw (ex-info "Context not set!" {})))

  (let [producer (chan)
        consumer (consumer producer)]
    (>!! producer (message msg))
    (close! producer)
    (<!! consumer))

  (first (requests)))
