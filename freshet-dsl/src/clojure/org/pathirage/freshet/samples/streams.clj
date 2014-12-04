(ns org.pathirage.freshet.samples.streams
  (require [org.pathirage.freshet.dsl.core :refer [defstream ts stream-fields]]))

(defstream wikipedia-raw
           (stream-fields [:title :string
                           :user :string
                           :diff-bytes :integer
                           :diff-url :string
                           :unparsed-flags :string
                           :summary :string
                           :is-minor :boolean
                           :is-unpatrolled :boolean
                           :is-special :boolean
                           :is-talk :boolean
                           :is-new :boolean
                           :is-bot-edit :boolean
                           :timestamp :long])
           (ts :timestamp))
