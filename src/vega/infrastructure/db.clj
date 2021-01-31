(ns vega.infrastructure.db
  (:require [datahike.api :as d]
            [integrant.core :as ig]
            [clojure.tools.reader.edn :as edn]))

(defmethod ig/init-key :db/setup [_ opts]
  (when-not (d/database-exists? opts)
    (d/create-database opts)

    (let [conn (d/connect opts)
          seed (edn/read-string (slurp "config/seed.edn"))]
      (d/transact conn seed)))

  opts)
