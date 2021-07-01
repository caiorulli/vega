(ns vega.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]))

(s/def :user/id int?)
(s/def :user/is-bot boolean?)
(s/def :user/first-name string?)

(s/def :user/last-name string?)
(s/def :user/username string?)

(s/def :telegram/user (s/keys :req [:user/id
                                    :user/is-bot
                                    :user/first-name]))

(s/def :chat/id int?)
(s/def :chat/type #{"private" "group" "supergroup" "channel"})

(s/def :telegram/chat (s/keys :req [:chat/id
                                    :chat/type]))

(s/def :message/message-id int?)
(s/def :message/date int?)
(s/def :message/chat :telegram/chat)

(s/def :message/from :telegram/user)
(s/def :message/text string?)

(s/def :telegram/message (s/keys :req [:message/message-id
                                       :message/date
                                       :message/chat]
                                 :opt [:message/from
                                       :message/text]))

#_(gen/sample (s/gen :telegram/message))
