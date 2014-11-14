# Freshet DSL Compilation

## Freshet DSL defaults (from CQL)

- When a stream is referenced in a Freshet query where relation is expected, an *Unbounded* window is applied to the stream by default.
- *Istream* operator is added by default whenever the query produces a *monotonic* relation. Static monotonicity test, is
used ~ base relation is monotonic if relation is append only, like ```(window (unbounded))``` and join of two monotonic relations also is monotonic.
- If we can't determine the monotonicity, we depends on the query author.
- For inner subquery, we add an *Istream* operator by default whenever the subquery is monotonic. Other case is still ambiguous.
- *Istream-Unbounded* is default when window specification is omitted.

## Common Patterns

- **Filters** are implemented using *Istream-Unbounded* window combination or an *Rstream-Now* window combination.
- When a stream is joined with a relation, it is usually most meaningful to apply a *Now* window over the stream and *Rstream* operator over the join result.

## Market feed stream definition

```clojure
(defstream market-feed
  (stream-fields [:symbol :string
                  :bid :float
                  :bid-size :float
                  :exchange :string
                  :volume :float]))

```

## Queries over market feed stream

### Select tuples with symbol "APPL"

```clojure
(select market-feed
  (where (= :symbol "APPL")))
```

As per the CQL defaults this query get transformed into query like below:

```clojure
(select market-feed
  (modifiers :istream)
  (window (unbounded))
  (where (= :symbol "APPL")))
```

After optimizations, this above will transformed into:

```clojure
(select market-feed
  (modifiers :rstream)
  (window (now))
  (where (= :symbol "APPL")))
```

Finally to Samza job graph which looks like following:

```clojure
{:operator :window :tuplebased true :range 1 :query-id "some-id" :input-stream "topic-from-stream-def" :output-stream "qid-window-out-market-feed" :input-streams [{:stream "market-feed" ...}] :output-streams [{:stream "qid-window-out-market-feed" ..}]}
 -> {:operator :select :where-exp "<JSON Encoded Expression>" :input-stream "qid-window-out-market-feed" :output-stream "qid-select-out-market-feed" :input-streams [{:stream "qid-window-out-market-feed"}] :output-streams [{:stream "qid-select-out-rstream-market-feed" ..}]}
 -> {:operator :rstream :input-stream "qid-select-out-market-feed"}
```

