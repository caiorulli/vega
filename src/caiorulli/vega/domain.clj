(ns caiorulli.vega.domain
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str])
  (:import (java.time ZoneId)))

(s/def ::trigger (s/and string? (complement str/blank?)))
(s/def ::sentence (s/and string? (complement str/blank?)))

(s/def ::reaction
  (s/keys :req [::trigger
                ::sentence]))

(s/def ::name string?)
(s/def ::zone-id (set (ZoneId/getAvailableZoneIds)))

(s/def ::friend
  (s/keys :req [::name
                ::zone-id]))
