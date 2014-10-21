(ns org.pathirage.freshet.dsl.core)

(comment
  "Defining streams"
  (defstream stream
             (fields [:name :string :address :string :age :integer :timestamp :long])
             (pk :id)
             (ts :timestamp))

  "Querying"
  (select stream
          (fields [:name :firstname] :address :age)
          (window (range 30))
          (where {:age (less-than 34)}))

  "Relation Algebric Expression"
  (def query {:stream stock-ticks :project [name, xx] :select condition})
  (def condition [:less-than :field-name value])
  (def complex-condition [:and [:less-than :field-name value] [:equal :field-name value]]))

(defn create-stream
  "Create a stream representing a topic in Kafka."
  [name]
  {:stream name
  :name name
  :pk :id
  :fields []
  :ts :timestamp})

(defn fields
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

(defmacro select
  "Build a select query, apply any modifiers specified in the body and then generate and submit DAG of Samza jobs
  which is the physical execution plan of the continuous query on stream specified by `stream`. `stream` is an stream
  created by `defstream`. Returns a job identifier which can used to monitor the query or error incase of a failure."
  [stream & body])

