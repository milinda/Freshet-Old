(ns org.pathirage.freshet.dsl.core
  (:refer-clojure :exclude [range]))

(comment
  "Defining streams"
  (defstream stream
             (stream-fields [:symbol :string
                             :bid :float
                             :ask :float
                             :bid-size :float
                             :ask-size :float
                             :quote-time :time
                             :trade-time :time
                             :exchange :string
                             :volume :float])
             (ts (now)))

  "Querying"
  (select stream
          (fields :symbol :bid :bid-size)
          (where {:age (less-than 34)})

  "Sliding Windows"
  (select stream
          (fields :symbol :bid :ask :exchange)
          (window )))

  "Relation Algebric Expression"
  (def query {:stream stock-ticks :project [name, xx] :select condition})
  (def condition [:less-than :field-name value])
  (def complex-condition [:and [:less-than :field-name value] [:equal :field-name value]]))

(comment "Most of the DSL constructs are inspired by SQLKorma(http://sqlkorma.com) library by Chris Ganger.")

(defn create-stream
  "Create a stream representing a topic in Kafka."
  [name]
  {:stream name
   :name   name
   :pk     :id
   :fields []
   :ts     :timestamp})

(defn stream-fields
  "Fields in a stream. These will get retrieved by default in select query if there aren't any projections."
  [stream fields]
  (assoc stream :fields (apply array-map fields)))

(defn pk
  [stream k]
  (assoc stream :pk (keyword k)))

(defn ts
  [stream s]
  (assoc stream :ts (keyword s)))

(defmacro defstream
  "Define a stream representing a topic in Kafka, applying functions in the body which changes the stream definition."
  [stream & body]
  `(let [s# (-> (create-stream ~(name stream)) ~@body)]
     (def ~stream s#)))

(defn select*
  "Creates the base query configuration for the given stream."
  [stream]
  (let [stream (if (keyword? stream)
                 (name stream)
                 stream)
        stream-name (:name stream)
        fields-with-types (:fields stream)
        field-names (not-empty (keys fields-with-types))]
    {:type      :select
     :fields    (or field-names [::*])
     :from      [(:stream stream-name)]
     :modifiers []
     :window    #{}
     :where     []
     :aliases   #{}
     :group     []
     :aggregate []
     :joins     []}))

(defn- update-fields
  [query fields]
  (let [[first-in-current] (:fields query)]
    (if (= first-in-current ::*)
      (assoc query :fields fields)
      (update-in query [:fields] (fn [v1 v2] (vec (concat v1 v2))) fields))))

(defn fields
  "Set fields which should be selected by the query. Fields can be a keyword
  or pair of keywords in a vector [field alias]

  ex: (fields [:name :username] :address :age)"
  [query & fields]
  (let [aliases (set (map second (filter vector? fields)))]
    (-> query
        (update-in [:aliases] clojure.set/union aliases)
        (update-fields fields))))

;; TODO: use named parameters for configuring sliding windows.
(defn range
  [window seconds]
  (let [window (assoc window :window-type :range)]
    (assoc window :range seconds)))

(defn rows
  [window count]
  (let [window (assoc window :window-type :rows)]
    (assoc window :rows count)))

(defn now
  [window]
  (assoc window :window-type :now))

(defn unbounded
  [window]
  (assoc window :window-type :unbounded))

(defn window*
  []
  {:type :window})

;; TODO: How to handle multiple stream situation. We need to change how from clause is specified in DSL.
(defmacro window
  "Set windowing method for stream-to-relational mapping.
  ex: (window (range 30))"
  [query & wm]
  `(let [window# (-> (window*) ~@wm)]
     (update-in ~query [:window] clojure.set/union window#)))

(defn modifiers
  "Set modifier to the select query to filter which results are returned.

  ex: (select wikipedia-stream
        (modifier :distinct)
        (window (range 60)))"
  [query & m]
  (update-in query [:modifiers] conj m))

(comment
  "How where clauses should be transformed"

  (or (> :delta 100) (= :newPage "True"))

  {::pred or ::args [{::pred > ::args [:delta 100]} {::pred = ::args [:newPage "True"]}]})

(defn where
  )

(defn execute-query
  "Execute a continuous query. Query will first get converted to extension of relation algebra, then
  to physical query plan before getting deployed in to the stream processing engine."
  [query]
  (prn query))

(defmacro select
  "Build a select query, apply any modifiers specified in the body and then generate and submit DAG of Samza jobs
  which is the physical execution plan of the continuous query on stream specified by `stream`. `stream` is an stream
  created by `defstream`. Returns a job identifier which can used to monitor the query or error incase of a failure.

  ex: (select stock-ticks
        (fields :symbol :bid :ask)
        (where {:symbol 'APPL'}))"
  [stream & body]
  `(let [query# (-> (select* ~(name stream)) ~@body)]
     (execute-query query#)))


