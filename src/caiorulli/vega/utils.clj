(ns caiorulli.vega.utils
  (:require [java-time :as t]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]))

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

(defn edn-from-resource
  [filename]
  (-> filename io/resource slurp edn/read-string))
