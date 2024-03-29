(ns caiorulli.vega.datahike
  (:require [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]
            [datahike.api :as d]
            [integrant.core :as ig]))

(defmethod ig/init-key ::setup [_ opts]
  (when-not (d/database-exists? opts)
    (d/create-database opts)

    (let [conn (d/connect opts)
          seed (edn/read-string (slurp (io/resource "seed.edn")))]
      (d/transact conn seed)))

  opts)
