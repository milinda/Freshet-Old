;;
;;
;;  Copyright 2014 Milinda Pathirage<milinda.pathirage@gmail.com>
;;
;;     Licensed under the Apache License, Version 2.0 (the "License");
;;     you may not use this file except in compliance with the License.
;;     You may obtain a copy of the License at
;;
;;         http://www.apache.org/licenses/LICENSE-2.0
;;
;;     Unless required by applicable law or agreed to in writing, software
;;     distributed under the License is distributed on an "AS IS" BASIS,
;;     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;     See the License for the specific language governing permissions and
;;     limitations under the License.
;;
;;

(ns org.pathirage.freshet.core)

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

(defmacro defstream
  "Define a stream representing a topic in Kafka, applying functions in the body which changes the stream definition."
  [stream & body])

(defmacro select
  "Build a select query, apply any modifiers specified in the body and then generate and submit DAG of Samza jobs
  which is the physical execution plan of the continuous query on stream specified by `stream`. `stream` is an stream
  created by `defstream`. Returns a job identifier which can used to monitor the query or error incase of a failure."
  [stream & body])

(defmacro do-until
  [& clauses]
  (when clauses
    (list 'clojure.core/when (first clauses)
          (if (next clauses)
            (second clauses)
            (throw (IllegalArgumentException. "do-until requires even number of forms.")))
          (cons 'do-until (nnext clauses)))))

(defmacro unless
  [condition & body]
  `(if (not ~condition)
     (do ~@body)))

(declare handle-things)

(defmacro domain
  [name & body]
  `{:tag     :domain
    :attrs   {:name (str '~name)}
    :content [~@body]})

(defmacro grouping
  [name & body]
  `{:tag     :grouping
    :attrs   {:name (str '~name)}
    :content [~@(handle-things body)]})

(declare grok-attr grok-props)

(defn handle-things [things]
  (for [t things]
    {:tag     :thing
     :attr    (grok-attrs (take-while (comp not vector?) t))
     :content (if-let [c (grok-props (drop-while (comp not vector?) t))]
                [c]
                [])}))

(defn grok-attrs [attrs]
  (into {:name (str (first attrs))}
        (for [a (rest attrs)]
          (cond
            (list? a) [:isa (str (second a))]
            (string? a) [:comment a]))))

(defn grok-props [props]
  (when props
    {:tag     :properties, :attrs nil,
     :content (apply vector (for [p props]
                              {:tag     :property,
                               :attrs   {:name (str (first p))},
                               :content nil}))}))

