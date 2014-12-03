(defproject org.pathirage.freshet/freshet-dsl "0.1.0-SNAPSHOT"
  :description "Freshet DSL: Clojure DSL based on CQL."
  :url "http://github.com/milinda/Freshet"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.pathirage.freshet/freshet-core "0.1.0-SNAPSHOT"]
                 [clojurewerkz/propertied "1.2.0"]
                 [org.apache.hadoop/hadoop-yarn-client "2.2.0"]
                 [org.apache.hadoop/hadoop-yarn-common "2.2.0"]
                 [org.apache.hadoop/hadoop-common "2.2.0"]
                 [commons-codec/commons-codec "1.4"]]
  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  :test-paths ["test/clojure" "test/java"])
