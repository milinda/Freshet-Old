(defproject org.pathirage.freshet/benchmark-data-generator "0.1.0-SNAPSHOT"
  :description "Generate test data for Freshet benchmarking purposes."
  :url "https://github.com/milinda/Freshet"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-kafka "0.2.6-0.8"]
                 [org.pathirage.freshet/thrift-lib "1.0"]
                 [org.clojure/data.json "0.2.5"]])
