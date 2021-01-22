(ns vega.interceptors
  (:require [clojure.string :as s]
            [taoensso.timbre :as timbre]
            [vega.protocols.telegram :as telegram]))

(defn reaction
  [api _db-setup {:keys [text chat]}]
  (when (s/includes? (s/lower-case text) "this is the way")
    (telegram/send-text api (:id chat) "This is the way.")))

(defn default
  [_api _db-setup message]
  (timbre/info (str "Intercepted message: " message))
  (timbre/info "Not doing anything with this message."))
