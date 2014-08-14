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

(def at-reply {"receiveget" "replyget", "receivepost" "replypost"})

(def at-script "script")

(defn addone [n] (+ n 1))

(defn generate-workflow-instance
  [num-activities num-svc-invokes]
  (let [instance-id (UUID/randomUUID)
        workflow-name (rand-nth workflow)
        activities (atom [])
        prev-is-script (atom false)
        receive-activity (rand-nth at-receive)
        num-invoke-activities (atom 0)
        num-actual-invokes (atom 0)
        entry-point (rand-nth workflow-entry-points)]
    (for [i 0]
      (if (< i num-activities)
        (do
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
            (or (not @prev-is-script)  (= @num-invoke-activities num-svc-invokes)) (do
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
          (recur (+ i 1)))))
    (WorkflowInstance. instance-id workflow-name activities num-invoke-activities)))
