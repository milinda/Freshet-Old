(defproject org.pathirage.freshet/freshet-shell "0.1.0-SNAPSHOT"
  :description "Freshet Shell: REPL for querying and interacting with streams using Freshet."
  :url "http://github.com/milinda/Freshet"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.pathirage.freshet/freshet-core "0.1.0-SNAPSHOT"]
                 [org.pathirage.freshet/freshet-dsl "0.1.0-SNAPSHOT"]
                 [reply "0.3.5" :exclusions [org.clojure/clojure]]]
  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  :test-paths ["test/clojure" "test/java"])
