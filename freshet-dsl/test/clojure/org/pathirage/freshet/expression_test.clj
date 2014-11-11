(ns org.pathirage.freshet.expression-test
  (:require [org.pathirage.freshet.dsl.core :as fcore]
            [clojure.test :refer :all]))

(comment
  "Tests for where clause building in DSL")

(deftest expression-building-text
  (testing "Simple expression"
    (let [e (fcore/pred-= :delta 100)]
      (prn (str "expression: " e))
      (is (= (fcore/pred-= :delta 100) {:pred := :args [:delta 100]}))))
  (testing "Complex expression"
    (let [e (fcore/pred-and (fcore/pred-< :delta 100) (fcore/pred-> :beta 340))]
      (prn (str "expression" e))
      (is (= (fcore/pred-and (fcore/pred-< :delta 100) (fcore/pred-> :beta 340))
           {:pred :and :args [{:pred :< :args [:delta 100]} {:pred :> :args [:beta 340]}]})))))