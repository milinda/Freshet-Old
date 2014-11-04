(ns org.pathirage.freshet.utils.core
  (:import (org.pathirage.freshet.utils WikipediaActivityFeed WikipediaActivityFeed$WikipediaActivitiesToCSV))
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def cli-options
  [["-t" "--time SECONDS" "Data Collection Time"
    :default 60
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 1 %) "Must be grater than 1 second"]]])

(defn -main
  [& args]
  (let [opts (parse-opts args cli-options)
        feed (WikipediaActivityFeed. "irc.wikimedia.org" 6667)]
    (prn (:time (:options opts)))
    (.start feed)
    (.listen feed "#en.wikipedia" (WikipediaActivityFeed$WikipediaActivitiesToCSV.))
    (Thread/sleep (* 1000 (:time (:options opts))))
    (.stop feed)))


