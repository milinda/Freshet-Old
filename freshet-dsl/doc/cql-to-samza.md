# Sample CQL to Samza Job Graph Conversions

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



