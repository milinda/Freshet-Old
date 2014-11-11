(ns org.pathirage.freshet.dsl.compiler
  (:import (org.pathirage.freshet.operators.select Expression ExpressionType PredicateType)))


(defn- pred-to-pred-type
  [pred]
  (case pred
    :and PredicateType/AND
    :or PredicateType/OR
    := PredicateType/EQUAL
    :not= PredicateType/NOT_EQUAL
    :not PredicateType/NOT
    :< PredicateType/LESS_THAN
    :<= PredicateType/LESS_THAN_OR_EQUAL
    :> PredicateType/GREATER_THAN
    :>= PredicateType/GREATER_THAN_OR_EQUAL
    (throw (Exception. (str "Unknown predicate type " pred)))))

(defn compile-expression
  [expr]
  (let [pred (:pred expr)
        operator (:op expr)
        lhs (first (:args expr))
        rhs (second (:args expr))
        lhs-expr (cond
                   (symbol? lhs) (doto (Expression. ExpressionType/FIELD)
                                   (.setField (str lhs)))
                   (or (number? lhs) (string? lhs)) (doto (Expression. ExpressionType/VALUE)
                                                      (.setValue lhs))
                   (map? lhs) (compile-expression lhs)
                   :else (throw (Exception. (str "Unknown argument: " lhs))))
        rhs-expr (if (not (= pred :not))
                   (cond
                     (symbol? rhs) (doto (Expression. ExpressionType/FIELD)
                                     (.setField (str rhs)))
                     (or (number? rhs) (string? rhs)) (doto (Expression. ExpressionType/VALUE)
                                                        (.setValue rhs))
                     (map? rhs) (compile-expression rhs)
                     :else (throw (Exception. (str "Unknown argument: " rhs)))))]
    (cond
      (and pred (not (= pred :not))) (doto (Expression. ExpressionType/PREDICATE)
                                       (.setPredicate (pred-to-pred-type pred))
                                       (.setLhs (compile-expression lhs))
                                       (.setRhs (compile-expression rhs)))
      (and pred (= pred :not)) (doto (Expression. ExpressionType/PREDICATE)
                                 (.setPredicate (pred-to-pred-type pred))
                                 (.setLhs (compile-expression lhs)))
      operator (throw (Exception. "Operators are not yet supported at DSL level."))
      :else (throw (Exception. (str "Unsupported expression: " expr))))))

(defn sql-to-raexp
  "Converts SQL statement to relational algebra expression"
  [query])
