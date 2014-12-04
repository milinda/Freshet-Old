(ns org.pathirage.freshet.samples.expressions
  (:import [org.pathirage.freshet.operators.select Expression ExpressionType PredicateType OperatorType]))

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

