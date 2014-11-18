(defproject org.pathirage.freshet/freshet-helpers "0.1.0-SNAPSHOT"
            :description "Freshet Utils: Tools and Utilities of Freshet project."
            :url "http://github.com/milinda/Freshet"
            :license {:name "Apache License, Version 2.0"
                      :url  "http://www.apache.org/licenses/LICENSE-2.0.html"}
            :repositories [["codehaus" "http://repository.codehaus.org/org/codehaus"]]
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [org.schwering/irclib "1.10"]
                           [org.slf4j/slf4j-api "1.6.2"]
                           [org.slf4j/slf4j-log4j12 "1.6.2"]
                           [com.fasterxml.jackson.core/jackson-core "2.4.0"]
                           [com.fasterxml.jackson.core/jackson-databind "2.4.0"]
                           [net.sf.opencsv/opencsv "2.0"]
                           [org.clojure/tools.cli "0.3.1"]
                           [org.apache.samza/samza-api "0.7.0"]
                           [org.apache.samza/samza-serializers_2.10 "0.7.0"]
                           [org.apache.samza/samza-core_2.10 "0.7.0"]
                           [org.apache.samza/samza-yarn_2.10 "0.7.0"]
                           [org.apache.samza/samza-kv_2.10 "0.7.0"]
                           [org.apache.samza/samza-kafka_2.10 "0.7.0"]
                           [org.pathirage.freshet/freshet-core "0.1.0-SNAPSHOT"]]
            :main org.pathirage.freshet.utils.core
            :source-paths ["src/clojure"]
            :java-source-paths ["src/java"]
            :test-paths ["test/clojure" "test/java"])
