(ns org.pathirage.freshet.dsl.samza
  (:import [org.pathirage.freshet Constants]
           [org.apache.samza.config.factories PropertiesConfigFactory]
           [org.apache.samza.job JobRunner]
           [java.net URI])
  (:require [clojurewerkz.propertied.properties :as props]
            [org.pathirage.freshet.dsl.helpers :as fhelpers]))

(comment
  "Sample Samza Property File
  ---------------------------
  # Job
  job.factory.class=org.apache.samza.job.yarn.YarnJobFactory
  job.name=wikipedia-parser
  # YARN
  yarn.package.path=file://${basedir}/target/${project.artifactId}-${pom.version}-dist.tar.gz

  # Task
  task.class=samza.examples.wikipedia.task.WikipediaParserStreamTask
  task.inputs=kafka.wikipedia-raw
  task.checkpoint.factory=org.apache.samza.checkpoint.kafka.KafkaCheckpointManagerFactory
  task.checkpoint.system=kafka
  # Normally, this would be 3, but we have only one broker.
  task.checkpoint.replication.factor=1

  # Metrics
  metrics.reporters=snapshot,jmx
  metrics.reporter.snapshot.class=org.apache.samza.metrics.reporter.MetricsSnapshotReporterFactory
  metrics.reporter.snapshot.stream=kafka.metrics
  metrics.reporter.jmx.class=org.apache.samza.metrics.reporter.JmxReporterFactory

  # Serializers
  serializers.registry.json.class=org.apache.samza.serializers.JsonSerdeFactory
  serializers.registry.metrics.class=org.apache.samza.serializers.MetricsSnapshotSerdeFactory

  # Systems
  systems.kafka.samza.factory=org.apache.samza.system.kafka.KafkaSystemFactory
  systems.kafka.samza.msg.serde=json
  systems.kafka.consumer.zookeeper.connect=localhost:2181/
  systems.kafka.consumer.auto.offset.reset=largest
  systems.kafka.producer.metadata.broker.list=localhost:9092
  systems.kafka.producer.producer.type=sync
  # Normally, we'd set this much higher, but we want things to look snappy in the demo.
  systems.kafka.producer.batch.num.messages=1
  systems.kafka.streams.metrics.samza.msg.serde=metrics
  ")

(defn run-samza-job*
  [props-file]
  (let [config-factory (PropertiesConfigFactory.)
        config (.getConfig config-factory props-file)
        job-runner (JobRunner. config)]
    (.run job-runner)))

(defn run-samza-job
  [props-file]
  (let [hello-samza-dir "/Users/mpathira/Workspace/Personal/Code/samza/hello-samza"]
    (clojure.java.shell/sh "deploy/samza/bin/run-job.sh" (str "--config-factory=org.apache.samza.config.factories.PropertiesConfigFactory " "--config-path=" props-file) :dir hello-samza-dir)))

(defn default-with-mterics-props
  "Create map of default properties for Freshet Samza jobs.

  zookeeper-list should look like - zk1.example.com:2181,zk2.example.com:2181,..
  broker-list should look like -  kafka1.example.com:9092,kafka2.example.com:9092, .."
  [zookeeper-list broker-list yarn-package-path]
  {"job.factory.class" "org.apache.samza.job.yarn.YarnJobFactory"
   "yarn.package.path" yarn-package-path
   "metrics.reporters" "snapshot,jmx"
   "metrics.reporter.snapshot.class" "org.apache.samza.metrics.reporter.MetricsSnapshotReporterFactory"
   "metrics.reporter.snapshot.stream" "kafka.metrics"
   "metrics.reporter.jmx.class" "org.apache.samza.metrics.reporter.JmxReporterFactory"
   "systems.kafka.samza.factory" "org.apache.samza.system.kafka.KafkaSystemFactory"
   "serializers.registry.streamelement.class" "org.pathirage.freshet.serde.StreamElementSerdeFactory"
   "serializers.registry.metrics.class" "org.apache.samza.serializers.MetricsSnapshotSerdeFactory"
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
    (assoc props Constants/CONF_WINDOW_RANGE (:window-range op-config))))

(defn- config-rows
  [props op-config]
  (if (contains? op-config :window-rows)
    (assoc props Constants/CONF_WINDOW_ROWS (:window-rows op-config))))

(defn get-window-properties-file [job-name]
  (str "samza-job-" job-name))

(defn gen-window-job-props
  "Generate properties file for Freshet Window operator.

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
                   (assoc Constants/CONF_SAMZA_TASK_CHECKPOINT_SYSTEM "kafka")
                   (assoc Constants/CONF_SAMZA_TASK_CHECKPOINT_REPLICATION_FACTOR "1")
                   (assoc Constants/CONF_SAMZA_TASK_CHECKPOINT_FACTORY "org.apache.samza.checkpoint.kafka.KafkaCheckpointManagerFactory")
                   (config-range op-config)
                   (config-rows op-config))
        properties-file-name (get-window-properties-file (:job-name op-config))
        properties-file (java.io.File/createTempFile properties-file-name ".properties")]
    (props/store-to wprops properties-file)
    (.getAbsolutePath properties-file)))

(defmacro submitjob
  [gen-job-props op-config]
  `(let [jobprops# (~gen-job-props ~op-config)]
     (prn jobprops#)
     (run-samza-job jobprops#)))

(defn submit-window-op-job
  [op-config]
  (submitjob gen-window-job-props op-config))

(defn submit-wikipedia-op-job
  [op-config]
  (submitjob fhelpers/gene-wikipedia-feed-job-props op-config))

