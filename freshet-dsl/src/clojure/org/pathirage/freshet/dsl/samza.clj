(ns org.pathirage.freshet.dsl.samza
  (:import [org.pathirage.freshet Constants]
           [org.pathirage.freshet.utils ExpressionSerde]
           [org.pathirage.freshet.operators.select Expression]
           [org.apache.samza.config.factories PropertiesConfigFactory]
           [org.apache.samza.job JobRunner]
           [java.net URI])
  (:require [clojurewerkz.propertied.properties :as props]
            [org.pathirage.freshet.dsl.helpers :as helpers]
            [org.pathirage.freshet.utils.config :as configutils])
  (:gen-class))

(defn run-samza-job*
  [props-file]
  (let [config-factory (PropertiesConfigFactory.)
        config (.getConfig config-factory (URI. props-file))
        job-runner (JobRunner. config)]
    (.run job-runner)))

(defn run-samza-job
  [props-file]
  (let [freshet-home (str (System/getenv "FRESHET_HOME") "/deploy/freshet")
        job-submission-result (clojure.java.shell/sh "bin/run-job.sh" (str "--config-factory=org.apache.samza.config.factories.PropertiesConfigFactory " "--config-path=" props-file) :dir freshet-home)]
    (if (not (= (:exit job-submission-result) 0))
      (let [err (:err job-submission-result)]
        (println (str "Error submitting Samza job to YARN: " err))))))

(defn default-with-mterics-props
  "Create map of default properties for Freshet Samza jobs.

  zookeeper-list should look like - zk1.example.com:2181,zk2.example.com:2181,..
  broker-list should look like -  kafka1.example.com:9092,kafka2.example.com:9092, .."
  [zookeeper-list broker-list yarn-package-path]
  {Constants/CONF_SAMZA_JOB_FACTORY_CLASS "org.apache.samza.job.yarn.YarnJobFactory"
   "yarn.package.path" yarn-package-path
   "metrics.reporters" "snapshot,jmx"
   "serializers.registry.string.class" "org.apache.samza.serializers.StringSerdeFactory"
   "serializers.registry.queuenode.class" "org.pathirage.freshet.serde.QueueNodeSerdeFactory"
   "metrics.reporter.snapshot.class" "org.apache.samza.metrics.reporter.MetricsSnapshotReporterFactory"
   "metrics.reporter.snapshot.stream" "kafka.metrics"
   "metrics.reporter.jmx.class" "org.apache.samza.metrics.reporter.JmxReporterFactory"
   "systems.kafka.samza.factory" "org.apache.samza.system.kafka.KafkaSystemFactory"
   "serializers.registry.streamelement.class" "org.pathirage.freshet.serde.StreamElementSerdeFactory"
   "serializers.registry.metrics.class" "org.apache.samza.serializers.MetricsSnapshotSerdeFactory"
   "systems.kafka.samza.key.serde" "string"
   "systems.kafka.samza.msg.serde" "streamelement"
   "systems.kafka.consumer.zookeeper.connect" zookeeper-list
   "systems.kafka.consumer.auto.offset.reset" "largest"
   "systems.kafka.producer.metadata.broker.list" broker-list
   "systems.kafka.producer.producer.type" "sync"
   "systems.kafka.producer.batch.num.messages" "1"
   "systems.kafka.streams.metrics.samza.msg.serde" "metrics"})

(defn- config-range
  [props op-config]
  (if (contains? op-config :window-range)
    (assoc props Constants/CONF_WINDOW_RANGE (:window-range op-config))
    props))

(defn- config-rows
  [props op-config]
  (if (contains? op-config :window-rows)
    (assoc props Constants/CONF_WINDOW_ROWS (:window-rows op-config))
    props))

(defn window-operator-default-config
  [query-id job-name input-stream output-stream zk kafka-bk]
  {:yarn-package (configutils/yarn-package-path)
   :zookeeper zk
   :broker kafka-bk
   :input-stream input-stream
   :job-name job-name
   :query-id query-id
   :output-stream output-stream})

(defn window-operator-range-config
  [query-id job-name input-stream output-stream zk kafka-bk range]
  (let [default-config (window-operator-default-config query-id job-name input-stream output-stream zk kafka-bk)]
    (assoc default-config :window-range range)))

(defn window-operator-rows-config
  [query-id job-name input-stream output-stream zk kafka-bk rows]
  (let [default-config (window-operator-default-config query-id job-name input-stream output-stream zk kafka-bk)]
    (assoc default-config :window-rows rows)))

(defn select-operator-config
  [query-id job-name input-stream output-stream intput-stream-defs zk kafka-bk where]
  {:yarn-package (configutils/yarn-package-path)
   :zookeeper zk
   :broker kafka-bk
   :input-stream input-stream
   :input-stream-defs intput-stream-defs
   :job-name job-name
   :query-id query-id
   :output-stream output-stream
   :where-clause (ExpressionSerde/serialize where)})

(defn properties-file-name [operator job-name]
  (str "samza-" operator "-operator-job-" job-name))

(defn gen-window-job-props
  "Generate properties file for Freshet Window operator based on operator config.

  Required config options
    - Query Id
    - Input stream
    - Down stream
    - System
    - Input stream definitions
    - Output stream definitions
    - Range or Rows"
  [op-config]
  (let [wprops (default-with-mterics-props (:zookeeper op-config) (:broker op-config) (:yarn-package op-config))
        wprops (-> (assoc wprops Constants/CONF_SAMZA_JOB_NAME (:job-name op-config))
                   (assoc Constants/CONF_QUERY_ID (:query-id op-config))
                   (assoc Constants/CONF_INPUT_STREAM (:input-stream op-config))
                   (assoc Constants/CONF_DOWN_STREAM_TOPIC (:output-stream op-config))
                   (assoc Constants/CONF_SAMZA_TASK_INPUTS (str "kafka." (:input-stream op-config)))
                   (assoc Constants/CONF_SAMZA_TASK_CLASS "org.pathirage.freshet.operators.WindowOperator")
                   (assoc "stores.windowing-synopses.factory" "org.apache.samza.storage.kv.KeyValueStorageEngineFactory")
                   (assoc "stores.windowing-synopses.key.serde" "string")
                   (assoc "stores.windowing-synopses.msg.serde" "queuenode")
                   (assoc "stores.windowing-metadata.factory" "org.apache.samza.storage.kv.KeyValueStorageEngineFactory")
                   (assoc "stores.windowing-metadata.key.serde" "string")
                   (assoc "stores.windowing-metadata.msg.serde" "string")
                   (assoc Constants/CONF_SAMZA_TASK_CHECKPOINT_SYSTEM "kafka")
                   (assoc Constants/CONF_SAMZA_TASK_CHECKPOINT_REPLICATION_FACTOR "1")
                   (assoc Constants/CONF_SAMZA_TASK_CHECKPOINT_FACTORY "org.apache.samza.checkpoint.kafka.KafkaCheckpointManagerFactory")
                   (config-range op-config)
                   (config-rows op-config))
        properties-file-name (properties-file-name "window" (:job-name op-config))
        properties-file (java.io.File/createTempFile properties-file-name ".properties")]
    (props/store-to wprops properties-file)
    (.getAbsolutePath properties-file)))

(defn gen-select-job-props
  "Generate properties file for select job based on operator config"
  [op-config]
  (let [select-props (default-with-mterics-props (:zookeeper op-config) (:broker op-config) (:yarn-package op-config))
        select-props (-> (assoc select-props Constants/CONF_SAMZA_JOB_NAME (:job-name op-config))
                         (assoc Constants/CONF_QUERY_ID (:query-id op-config))
                         (assoc Constants/CONF_INPUT_STREAM (:input-stream op-config))
                         (assoc Constants/CONF_DOWN_STREAM_TOPIC (:output-stream op-config))
                         (assoc Constants/CONF_SAMZA_TASK_INPUTS (str "kafka." (:input-stream op-config)))
                         (assoc Constants/CONF_SAMZA_TASK_CLASS "org.pathirage.freshet.operators.SelectOperator")
                         (assoc Constants/CONF_SELECT_WHERE_EXPRESSION (configutils/base64-encode (:where-clause op-config)))
                         (assoc Constants/CONF_SAMZA_TASK_CHECKPOINT_SYSTEM "kafka")
                         (assoc Constants/CONF_SAMZA_TASK_CHECKPOINT_REPLICATION_FACTOR "1")
                         (assoc Constants/CONF_SAMZA_TASK_CHECKPOINT_FACTORY "org.apache.samza.checkpoint.kafka.KafkaCheckpointManagerFactory")
                         (into (configutils/streams-to-streamdef-props (:input-stream-defs op-config))))
        properties-file-name (properties-file-name "select" (:job-name op-config))
        properties-file (java.io.File/createTempFile properties-file-name ".properties")]
    (prn select-props)
    (props/store-to select-props properties-file)
    (.getAbsolutePath properties-file)))

(defn gen-aggregate-job-props
  [op-config]
  (let [aggr-props (default-with-mterics-props (:zookeeper op-config) (:broker op-config) (:yarn-package op-config))
        properties-file-name (properties-file-name "aggregate" (:job-name op-config))
        properties-file (java.io.File/createTempFile properties-file-name ".properties")]
    (props/store-to aggr-props properties-file)
    (.getAbsolutePath properties-file)))

(defmacro submitjob
  [gen-job-props op-config]
  `(let [jobprops# (~gen-job-props ~op-config)]
     (prn jobprops#)
     (run-samza-job jobprops#)))

(defn submit-window-op-job
  [op-config]
  (submitjob gen-window-job-props op-config))

(defn submit-select-op-job
  [op-config]
  (submitjob gen-select-job-props op-config))

(defn submit-wikipedia-op-job
  [op-config]
  (submitjob helpers/gene-wikipedia-feed-job-props op-config))

