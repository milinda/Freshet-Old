(ns org.pathirage.freshet.dsl.helpers
  (:refer-clojure :exclude [range])
  (:import [org.pathirage.freshet Constants]
           [org.apache.samza.config.factories PropertiesConfigFactory]
           [org.apache.samza.job JobRunner]
           [java.net URI]
           [java.io File FileInputStream]
           [java.util Properties])
  (:require [clojurewerkz.propertied.properties :as props]
            [org.pathirage.freshet.dsl.core :refer [defstream ts stream-fields]])
  (:gen-class))

(defn yarn-package-path
  []
  (let [freshet-home (System/getenv "FRESHET_HOME")
        freshet-conf (str freshet-home "/deploy/freshet/conf/freshet.conf")
        freshet-conf-in (FileInputStream. freshet-conf)
        props (Properties.)]
    (.load props freshet-conf-in)
    (.get props "yarn.package.path")))

(defn default-without-mterics-props
  "Create map of default properties for Freshet Samza jobs.

  zookeeper-list should look like - zk1.example.com:2181,zk2.example.com:2181,..
  broker-list should look like -  kafka1.example.com:9092,kafka2.example.com:9092, .."
  [zookeeper-list broker-list yarn-package-path]
  {"job.factory.class" "org.apache.samza.job.yarn.YarnJobFactory"
   "yarn.package.path" yarn-package-path
   "systems.kafka.samza.factory" "org.apache.samza.system.kafka.KafkaSystemFactory"
   "serializers.registry.streamelement.class" "org.pathirage.freshet.serde.StreamElementSerdeFactory"
   "systems.kafka.samza.msg.serde" "streamelement"
   "systems.kafka.consumer.zookeeper.connect" zookeeper-list
   "systems.kafka.consumer.auto.offset.reset" "largest"
   "systems.kafka.producer.metadata.broker.list" broker-list
   "systems.kafka.producer.producer.type" "sync"
   "systems.kafka.producer.batch.num.messages" "1"})

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
(defn wikipedia-activity-feed-job [zookeeper kafka-brokers]
  {:job-name "wikipedia-feed"
   :inputs "wikipedia.#en.wikipedia,wikipedia.#en.wiktionary,wikipedia.#en.wikinews"
   :zookeeper zookeeper
   :broker kafka-brokers
   :yarn-package (yarn-package-path)
   :task-class "org.pathirage.freshet.utils.WikipediaFeedStreamTask"})

(defn- file-path-to-uri
  [path]
  (let [f (File. path)]
    (if (.exists f)
      (.toString (.toURI f))
      path)))

(defn gene-wikipedia-feed-job-props
  [op-config]
  (let [wikiprops (default-without-mterics-props (:zookeeper op-config) (:broker op-config) (file-path-to-uri (:yarn-package op-config)))
        wikiprops (-> (assoc wikiprops Constants/CONF_SAMZA_JOB_NAME (:job-name op-config))
                      (assoc Constants/CONF_SAMZA_TASK_CLASS (:task-class op-config))
                      (assoc Constants/CONF_SAMZA_TASK_INPUTS (:inputs op-config))
                      (assoc Constants/CONF_SYSTEMS_WIKIPEDIA_FACTORY "org.pathirage.freshet.utils.system.WikipediaSystemFactory")
                      (assoc Constants/CONF_SYSTEMS_WIKIPEDIA_HOST "irc.wikimedia.org")
                      (assoc Constants/CONF_SYSTEMS_WIKIPEDIA_PORT "6667")
                      (assoc "serializers.registry.json.class" "org.apache.samza.serializers.JsonSerdeFactory"))
        properties-file-name (str "helper-" (:job-name op-config))
        properties-file (java.io.File/createTempFile properties-file-name ".properties")]
    (props/store-to wikiprops properties-file)
    (.toString (.toURI properties-file))))