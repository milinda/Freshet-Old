(ns org.pathirage.freshet.config-test
  (:import [org.apache.samza.config MapConfig])
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]))


(deftest properties-file-map-value-test
  (testing "Map as a value of a property in propeties file"
    (with-open [^java.io.Reader reader (-> "config-test.properties" io/resource io/file io/reader)]
    (let [props (java.util.Properties.)]
      (.load props reader)
      (let [map-config (MapConfig. props)
            stream-defs (.subset map-config "org.pathirage.kappaql.input")]
        (is (= "name=String,age=Integer" (.get stream-defs ".stream1")))
        (is (= "orderId=String,Quantity=Integer" (.get stream-defs ".stream2"))))))))