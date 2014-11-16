(ns org.pathirage.freshet.dsl.helpers
  (:refer [org.pathirage.freshet.dsl.core :all]))

(defn wikipedia-stream-def []
  (defstream wikipedia-activity
    (stream-fields [:title :string
                    :user :string
                    :diff-bytes :integer
                    :is-talk :boolean
                    :is-new :boolean
                    :is-bot-edit :boolean
                    :timestamp :long])
    (ts :timestamp)))

; TODO: Clojure maps describing wikipedia activity feed and window operator jobs. Use samza default conf.
(defn wikipedia-activity-feed-job [])
(defn example-window-op-job [])
