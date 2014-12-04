(ns org.pathirage.freshet.utils.config
  (:import [java.io File FileInputStream]
           [java.util Properties]
           [org.pathirage.freshet Constants]
           [org.apache.commons.codec.binary Base64])
  (:require [clojure.string :as string]))

(defmacro read-property-from-freshet-configuration
  [property]
  `(let [freshet-home (System/getenv "FRESHET_HOME")
         freshet-conf (str freshet-home "/deploy/freshet/conf/freshet.conf")
         freshet-conf-in (FileInputStream. freshet-conf)
         props (Properties.)]
     (.load props freshet-conf-in)
     (.get props ~property)))

(defn yarn-package-path
  "Read freshet YARN package path from freshet.conf.

  Freshet configuration is read relative to the FRESHET_HOME directory."
  []
  (read-property-from-freshet-configuration "freshet.yarn.package.path"))

(defn zookeeper-node-list
  "Read Zookeeper node list from freshet.conf."
  []
  (read-property-from-freshet-configuration "freshet.kafka.zookeeper.connect"))

(defn kafka-broker-list
  "Read Kafka broker list from freshet.conf"
  []
  (read-property-from-freshet-configuration "freshet.kafka.broker.list"))

(defn serialize-streamdef
  "Serialize stream definition to a string representation."
  [stream]
  (let [fields (:fields stream)]
    (string/join "," (map (fn [kv] (str (name (key kv)) "=" (name (val kv)))) fields))))

(defn stream-to-streamdef-prop
  "Generate stream definition as a Samza job config property."
  [stream]
  {(str Constants/CONF_OPERATOR_INPUT_STREAMS (:name stream)) (serialize-streamdef stream)})

(defn streams-to-streamdef-props
  "Generate list of stream definition properties"
  [streams]
  (reduce merge (map stream-to-streamdef-prop streams)))

(defn base64-encode
  [^String str]
  (let [original-bytes (.getBytes str)]
    (String. (Base64/encodeBase64 original-bytes))))