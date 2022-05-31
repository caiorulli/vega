(ns user
  (:require [caiorulli.vega.core :refer [config]]
            [integrant.repl :refer [clear go halt prep init reset reset-all]]))

(integrant.repl/set-prep! (constantly config))
