(ns org.pathirage.freshet.expressioneval-test
  (:import (org.pathirage.freshet.operators.select Expression ExpressionType PredicateType ExpressionEvaluator)
           (org.pathirage.freshet.data StreamDefinition StreamDefinition$FieldType StreamElement))
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]))


;; Expression Evaluator Test
;;
;; Tasks
;;   - Collect some wikipedia activity streams and build in-memory stream
;;   - Define some where conditions for wikipdeia activities

(def where-diff-bytes->-100
  (let [lhs (doto (Expression. ExpressionType/FIELD)
              (.setField "diff-bytes"))
        rhs (doto (Expression. ExpressionType/VALUE)
              (.setValue 100))]
    (doto (Expression. ExpressionType/PREDICATE)
      (.setPredicate PredicateType/GREATER_THAN)
      (.setLhs lhs)
      (.setRhs rhs))))

(def where-diff-bytes-<-100
  (let [lhs (doto (Expression. ExpressionType/FIELD)
              (.setField "diff-bytes"))
        rhs (doto (Expression. ExpressionType/VALUE)
              (.setValue 100))]
    (doto (Expression. ExpressionType/PREDICATE)
      (.setPredicate PredicateType/LESS_THAN)
      (.setLhs lhs)
      (.setRhs rhs))))

(def where-is-new-edit
  (let [lhs (doto (Expression. ExpressionType/FIELD)
              (.setField "is-new"))
        rhs (doto (Expression. ExpressionType/VALUE)
              (.setValue true))]
    (doto (Expression. ExpressionType/PREDICATE)
      (.setPredicate PredicateType/EQUAL)
      (.setLhs lhs)
      (.setRhs rhs))))

(def new-edit-and->100-diff
  (doto (Expression. ExpressionType/PREDICATE)
    (.setPredicate PredicateType/AND)
    (.setLhs where-diff-bytes->-100)
    (.setRhs where-is-new-edit)))

(def wikipedia-activity-stream-definition
  (let [type-map (java.util.HashMap. {"channel" StreamDefinition$FieldType/STRING
                                     "source" StreamDefinition$FieldType/STRING
                                     "time" StreamDefinition$FieldType/LONG
                                     "title" StreamDefinition$FieldType/STRING
                                     "user" StreamDefinition$FieldType/STRING
                                     "diff-bytes" StreamDefinition$FieldType/INTEGER
                                     "diff-url" StreamDefinition$FieldType/STRING
                                     "summary" StreamDefinition$FieldType/STRING
                                     "is-minor" StreamDefinition$FieldType/BOOL
                                     "is-talk" StreamDefinition$FieldType/BOOL
                                     "is-bot-edit" StreamDefinition$FieldType/BOOL
                                     "is-new" StreamDefinition$FieldType/BOOL
                                     "is-unpatrolled" StreamDefinition$FieldType/BOOL
                                     "is-special" StreamDefinition$FieldType/BOOL
                                     "unparsed-flags" StreamDefinition$FieldType/STRING})
        stream-def (StreamDefinition. type-map)]
    stream-def))

(def test-stream-element-with-diff-bytes->-100
  (let [fields (java.util.HashMap. {"channel" "#en.wikipedia"
                                   "source" "rc-pmtpa"
                                   "time" 1415078790283
                                   "title" "Xu Wanquan"
                                   "user" "G503"
                                   "diff-bytes" (Integer. 3205)
                                   "diff-url" "http://en.wikipedia.org/w/index.php?oldid=632381032&rcid=690940421"
                                   "summary" "[[WP:AES|←]]Created page with '{{Infobox football biography | name= Xu Wanquan <br>   许万权 | birth_date  = {{birth date and age|1993|4|19}} | birth_place = [[Dalian]], [[Liaoning]], China...'"
                                   "is-minor" false
                                   "is-talk" false
                                   "is-bot-edit" false
                                   "is-new" true
                                   "is-unpatrolled" true
                                   "is-special" false
                                   "unparsed-flags" "!N"})
        stream-element (StreamElement. fields 1415078790283 1415078790283 "Xu Wanquan")]
    stream-element))

(deftest expression-evaluation-test
  (testing "Greater than predicate"
    (let [exp-evaluator (ExpressionEvaluator.)]
      (is (=
            true
            (.evalPredicate
              exp-evaluator
              test-stream-element-with-diff-bytes->-100
              wikipedia-activity-stream-definition
              where-diff-bytes->-100)))))
  (testing "Equal boolean"
    (let [exp-evaluator (ExpressionEvaluator.)]
      (is (=
            true
            (.evalPredicate
              exp-evaluator
              test-stream-element-with-diff-bytes->-100
              wikipedia-activity-stream-definition
              where-is-new-edit)))))
  (testing "AND operator"
    (let [exp-evaluator (ExpressionEvaluator.)]
      (is (=
            true
            (.evalPredicate
              exp-evaluator
              test-stream-element-with-diff-bytes->-100
              wikipedia-activity-stream-definition
              new-edit-and->100-diff)))))
  (testing "< operator"
    (let [exp-evaluator (ExpressionEvaluator.)]
      (is (=
            false
            (.evalPredicate
              exp-evaluator
              test-stream-element-with-diff-bytes->-100
              wikipedia-activity-stream-definition
              where-diff-bytes-<-100))))))






