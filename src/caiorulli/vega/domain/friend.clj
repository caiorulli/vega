(ns caiorulli.vega.domain.friend
  (:require [clojure.spec.alpha :as s])
  (:import (java.time ZoneId)))

(s/def ::name string?)
(s/def ::zone-id (set (ZoneId/getAvailableZoneIds)))

(s/def ::friend
  (s/keys :req [::name
                ::zone-id]))
