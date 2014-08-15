(ns org.pathirage.freshet.benchmark.utils.workflowactivitygenerator
  (:import [java.util UUID]))

(defrecord WorkflowInstance [id process-name activities num-svc-invokcations])

(defrecord WorkflowActivity [name type entry-url target-url])

(def workflow-names ["hello" "httpstatus" "stateful" "errorcode" "receiveget" "receivepost"
                     "putjson" "securityconfig" "scripttest" "whiletest" "arrayinput"])

(def workflow-entry-points ["init" "hello" "start" "begin"])

(def service-endpoints ["http://ibm.com/people?name=Milinda"
                        "http://google.com/translate?ssh=dkdkdkd"
                        "http://example.com/trasnlate?djff=ssksks"
                        "http://facebook.com/user?id=dkdkdk"
                        "http://cloudant.com/tes/put"
                        "http://cloudant.com/tes/get"
                        "http://localhost/addUser"
                        "http://localhost:getUser"])

(def at-receive ["receiveget" "receivepost"])

(def at-invoke ["get" "put" "post" "delete"])

(def at-reply {"receiveget" "replyget" "receivepost" "replypost"})

(def at-script "script")

(def version "1.0")

(defn addone [n] (+ n 1))

(defn generate-workflow-instance
  [num-activities num-svc-invokes]
  (let [instance-id (UUID/randomUUID)
        workflow-name (rand-nth workflow-names)
        activities (atom [])
        prev-is-script (atom false)
        receive-activity (rand-nth at-receive)
        num-actual-invokes (atom 0)
        entry-point (rand-nth workflow-entry-points)]
    (loop [i 0]
      (when (< i num-activities)
          (cond
            (= i 0) (swap!
                      activities
                      (fn [a]
                        (conj a (WorkflowActivity.
                                  (str receive-activity entry-point)
                                  receive-activity
                                  entry-point
                                  nil))))
            (= i (- num-activities 1)) (swap!
                                         activities
                                         (fn [a]
                                           (conj a (WorkflowActivity.
                                                     (str (get at-reply receive-activity) entry-point)
                                                     (get at-reply receive-activity)
                                                     entry-point
                                                     nil))))
            (or (not @prev-is-script)  (= @num-actual-invokes num-svc-invokes)) (do
                                                                              (swap!
                                                                                activities
                                                                                (fn [a]
                                                                                  (conj a (WorkflowActivity.
                                                                                            (str at-script i)
                                                                                            at-script
                                                                                            nil
                                                                                            nil))))
                                                                              (swap! prev-is-script (fn [_] true)))
            :else (let [invoke-type (rand-nth at-invoke)]
                    (swap! activities (fn [a]
                                        (conj a (WorkflowActivity.
                                                  (str invoke-type i)
                                                  invoke-type
                                                  nil
                                                  (rand-nth service-endpoints)))))
                    (swap! prev-is-script (fn [_] false))
                    (swap! num-actual-invokes addone)))
          (recur (+ i 1))))
    (WorkflowInstance. instance-id workflow-name @activities @num-actual-invokes)))

(defn create-workflow-activity
  ([timestamp version workflow instance-id event-type event-source-type]
   {:timestamp timestamp :version version :workflow workflow :instance-id instance-id :event-type event-type :event-source-type event-source-type})
  ([timestamp version workflow instance-id event-type event-source-type activity-type activity-name]
   {:timestamp timestamp :version version :workflow workflow :instance-id instance-id :event-type event-type :event-source-type event-source-type :activity-type activity-type :activity-name activity-name})
  ([timestamp version workflow instance-id event-type event-source-type source-activity target-activity is-data-link]
   {:timestamp timestamp :version version :workflow workflow :instance-id instance-id :event-type event-type :event-source-type event-source-type :source-activity source-activity :target-activity target-activity :data-link is-data-link :error-link (not is-data-link)}))

(defn generate-receive-activity-events [instance-id workflow-name activity time]
  [(create-workflow-activity time version workflow-name instance-id "MESSAGE_RECEIVED" "MESSAGE")
   (create-workflow-activity time version workflow-name instance-id "INSTANCE_CREATED" "INSTANCE")
   (create-workflow-activity time version workflow-name instance-id "ACTIVITY_ACTIVATED" "ACTIVITY" (:type activity) (:name activity))
   (create-workflow-activity (+ (rand-int 2) time) version workflow-name instance-id "ACTIVITY_RECEIVED_MESSAGE" "ACTIVITY" (:type activity) (:name activity))
   (create-workflow-activity (+ (rand-int 2) time) version workflow-name instance-id "ACTIVITY_COMPLETED" "ACTIVITY" (:type activity) (:name activity))])

(defn generate-link-events [source-activity target-activity instance-id workflow-name time]
  [(create-workflow-activity time version workflow-name instance-id "LINK_EVALUATED" "LINK" (:name source-activity) (:name target-activity) false)])

(defn generate-script-activity-events [activity instance-id workflow-name time]
  [])

(defn generate-reply-activity-events [activity instance-id workflow-name time]
  [])

(defn generate-svc-invoke-activity-events [activity instance-id workflow-name time]
  [])

(defn generate-workflow-activity-events-for-process-instance [process-instance]
  (let [current-time (System/currentTimeMillis)]
    []))
