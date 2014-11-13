(ns org.pathirage.freshet.dsl.compiler
  (:import (org.pathirage.freshet.operators.select Expression ExpressionType PredicateType)))

(def freshet-type-map
  {:integer "int"
   :string "string"
   :long "long"
   :double "double"
   :float "float"})

(defn- freshet-type-to-avro-type
  [t]
  (let [at (get freshet-type-map t)]
    (if at
      at
      (throw (Exception. (str "Invalid type " t))))))

(defn- freshet-fields-to-avro-fields
  [fields]
  (let [fields-seq (seq fields)]
    (vec (map (fn [e] {"name" (first e) "type" (freshet-type-to-avro-type (second e))}) fields-seq))))

(defn stream-to-avro-schema
  "Generate avro schema from stream definition. Avro schema needs a namespace. Default is 'freshet'."
  [stream]
  (let [fields (:fields stream)
        ns (:ns stream)
        name (str (:name stream))]
    {"namespace" ns
     "type" "record"
     "name" name
     "fields" (freshet-fields-to-avro-fields fields)}))

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
                   (keyword? lhs) (doto (Expression. ExpressionType/FIELD)
                                   (.setField (str lhs)))
                   (or (number? lhs) (string? lhs)) (doto (Expression. ExpressionType/VALUE)
                                                      (.setValue lhs))
                   (map? lhs) (compile-expression lhs)
                   :else (throw (Exception. (str "Unknown argument: " lhs " expression: " expr))))
        rhs-expr (if (not (= pred :not))
                   (cond
                     (keyword? rhs) (doto (Expression. ExpressionType/FIELD)
                                     (.setField (str rhs)))
                     (or (number? rhs) (string? rhs)) (doto (Expression. ExpressionType/VALUE)
                                                        (.setValue rhs))
                     (map? rhs) (compile-expression rhs)
                     :else (throw (Exception. (str "Unknown argument: " rhs)))))]
    (cond
      (and pred (not (= pred :not))) (doto (Expression. ExpressionType/PREDICATE)
                                       (.setPredicate (pred-to-pred-type pred))
                                       (.setLhs lhs-expr)
                                       (.setRhs rhs-expr))
      (and pred (= pred :not)) (doto (Expression. ExpressionType/PREDICATE)
                                 (.setPredicate (pred-to-pred-type pred))
                                 (.setLhs lhs-expr))
      operator (throw (Exception. "Operators are not yet supported at DSL level."))
      :else (throw (Exception. (str "Unsupported expression: " expr))))))

(defn create-raexp
  [id]
  {:id id
   :is-project false
   :is-select true
   :result-fields []
   :select nil
   :from nil})

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn- query-id
  [stream]
  (let [ns (str (:ns stream))
        name (str (:name stream))]
    (str "query-on-" ns "-" name "-" (uuid))))

(defn- is-*
  [fields-from-def fields-in-query]
  (every? true? (map (fn [f] (contains? fields-from-def f)) fields-in-query)))

(defn handle-fields
  [raexp query]
  "Handle projections.

  Limitations
    - Doesn't support renaming"
  (let [fields (:fields query)
        fields-from-def (:fields (:stream (first (:from query))))]
    (cond
      (= :* (first fields)) (let [r1 (assoc raexp :is-project false)
                                  r2 (assoc r1 :is-select true)]
                              (assoc r2 :result-fields (not-empty (keys fields-from-def))))
      (> (count (remove #(= % :*) fields)) 1) (let [eq-* (is-* fields-from-def (remove #(= % :*) fields))]
                                                               (if eq-*
                                                                 (let [r1 (assoc raexp :is-project false)
                                                                       r2 (assoc r1 :is-select true)]
                                                                   (assoc r2 :result-fields (not-empty (keys fields-from-def))))
                                                                 (let [r1 (assoc raexp :is-project true)
                                                                       r2 (assoc r1 :is-select false)]
                                                                   (assoc r2 :result-fields (remove #(= % :*) fields)))))
      :else (throw (Exception. (str "Unsupported projection/selection " fields))))))

(defn handle-where
  [raexp query]
  (let [where-exp (:where query)
        compiled-where (compile-expression where-exp)]
    (assoc raexp :select compiled-where)))

(defn handle-windows
  [raexp query]
  (let [window (:window query)]
    (assoc raexp :window window)))

(defn handle-from
  [raexp query]
  (let [stream (:stream (first (:from query)))]
    (assoc raexp :from stream)))

(defn sql-to-raexp
  "Converts SQL statement to relational algebra expression.

  Fundamental property:
   - Every operator in the algebra accepts (one or two) relation instances as arguments and returns a relation instance
   as the result."
  [query]
  (let [raexp (create-raexp (query-id (:stream (first (:from query)))))]
    (-> raexp (handle-from query) (handle-fields query) (handle-where query) (handle-windows query))))

