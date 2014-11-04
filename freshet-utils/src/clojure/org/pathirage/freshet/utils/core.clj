(ns org.pathirage.freshet.utils.core
  (:import (org.pathirage.freshet.utils WikipediaActivityFeed WikipediaActivityFeed$WikipediaActivitiesToCSV))
  (:require [irclj.core :as irc])
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(comment
  ({:text "14[[07Donacaula ignitalis14]]4 N10 02http://en.wikipedia.org/w/index.php?oldid=632362930&rcid=690900013 5* 03Wilhelmina Will 5* (+749) 10[[WP:AES|←]]Created page with '{{italic title}} {{Taxobox | image =  | image_caption = | regnum = [[Animal]]ia | phylum = [[Arthropod]]a | classis = [[Insect]]a | ordo = [[Lepidoptera]] | fami...'",
    :target "#en.wikipedia",
    :command "PRIVMSG",
    :params ["#en.wikipedia" "14[[07Donacaula ignitalis14]]4 N10 02http://en.wikipedia.org/w/index.php?oldid=632362930&rcid=690900013 5* 03Wilhelmina Will 5* (+749) 10[[WP:AES|←]]Created page with '{{italic title}} {{Taxobox | image =  | image_caption = | regnum = [[Animal]]ia | phylum = [[Arthropod]]a | classis = [[Insect]]a | ordo = [[Lepidoptera]] | fami...'"],
    :raw ":rc-pmtpa!~rc-pmtpa@special.user PRIVMSG #en.wikipedia :14[[07Donacaula ignitalis14]]4 N10 02http://en.wikipedia.org/w/index.php?oldid=632362930&rcid=690900013 5* 03Wilhelmina Will 5* (+749) 10[[WP:AES|←]]Created page with '{{italic title}} {{Taxobox | image =  | image_caption = | regnum = [[Animal]]ia | phylum = [[Arthropod]]a | classis = [[Insect]]a | ordo = [[Lepidoptera]] | fami...'",
    :host "special.user", :user "~rc-pmtpa", :nick "rc-pmtpa"})
  )

(defn print-irc-message [irc & other] (prn other))

(def raw-activity ":rc-pmtpa!~rc-pmtpa@special.user PRIVMSG #en.wikipedia :14[[07Donacaula ignitalis14]]4 N10 02http://en.wikipedia.org/w/index.php?oldid=632362930&rcid=690900013 5* 03Wilhelmina Will 5* (+749) 10[[WP:AES|←]]Created page with '{{italic title}} {{Taxobox | image =  | image_caption = | regnum = [[Animal]]ia | phylum = [[Arthropod]]a | classis = [[Insect]]a | ordo = [[Lepidoptera]] | fami...'")


(defn collect-wikipedia-activities
  [collection-time]
  (let [connection (irc/connect "irc.wikimedia.org" 6667 "hotbot" :callbacks {:privmsg print-irc-message})]
    (irc/join connection "#en.wikipedia")
    (Thread/sleep collection-time)
    (irc/kill connection)))

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


