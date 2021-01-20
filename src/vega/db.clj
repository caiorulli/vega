(ns vega.db
  (:require [datahike.api :as d]
            [integrant.core :as ig]
            vega.morse))

(defmethod ig/init-key :db/setup [_ opts]
  (when-not (d/database-exists? opts)
    (d/create-database opts)

    (let [conn (d/connect opts)]
      (d/transact conn [#:friend {:name    "thiago"
                                  :zone-id "Europe/Lisbon"}
                        #:friend {:name    "pedrotti"
                                  :zone-id "Europe/Berlin"}
                        #:friend {:name    "castro"
                                  :zone-id "America/Campo_Grande"}])))

  opts)
