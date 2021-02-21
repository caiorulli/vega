(ns vega.core
  (:require [clj-http.client :as client]
            [clojure.core.async :refer [chan close!]]
            [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]
            [java-time :as t]
            [environ.core :refer [env]]
            [integrant.core :as ig]
            [taoensso.timbre :as timbre]))

(def config
  {:core/runtime  {}
   :core/consumer {:api        (ig/ref :telegram/api)
                   :db-setup   (ig/ref :db/setup)
                   :producer   (ig/ref :telegram/producer)
                   :reddit-api (ig/ref :reddit/api)}

   :telegram/producer {:token   (env :telegram-token)
                       :runtime (ig/ref :core/runtime)
                       :opts    {:timeout 10}}
   :telegram/api      {:token (env :telegram-token)}

   :reddit/api {}

   :db/setup {:store      {:backend :file
                           :path    "/tmp/vegadb"}
              :initial-tx (edn/read-string (slurp (io/resource "schema.edn")))
              :name       "vegadb"}

   :etc/logging {:level (keyword (env :log-level))}})

(defmethod ig/init-key :core/runtime [_ _]
  (chan))

(defmethod ig/halt-key! :core/runtime [_ runtime]
  (close! runtime))

(defmethod ig/init-key :etc/logging [_ {:keys [level]
                                        :or   {level :info}}]
  (timbre/set-level! level))

(def default-zone (t/zone-id "America/Sao_Paulo"))

(defn now []
  (t/zoned-date-time default-zone))

(defn try-get [url]
  (try (client/get url)
       (catch Exception _e {})))

(defn- random-string! []
  (apply str (take 15 (repeatedly #(rand-nth "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")))))

(defn blurp!
  "Shamelessly copied from https://stackoverflow.com/a/8281307/5935298.
  Makes url into file. Returns file object"
  [url-string]
  (let [file-name (str "/tmp/" (random-string!) ".jpg")
        dest-file (io/file file-name)]

    (with-open [src (io/input-stream (io/as-url url-string))]
      (io/copy src dest-file))

    dest-file))
