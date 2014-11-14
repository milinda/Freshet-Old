# Freshet DSL Samples

## Defining streams

```clojure
(defstream market-feed
  (stream-fields [:symbol :string
                  :bid :float
                  :ask :float
                  :bid-size :float
                  :ask-size :float
                  :quote-time :time
                  :trade-time :time
                  :exchange :string
                  :volume :float]))
```

## Queries

### Project with renaming

```clojure
(select (rstream [:symbol [:bid :as :b] :volume])
  (from [(window market-feed (now))])
  (where (= :symbol "APPL")))
```

### Aggregates

```clojure
(select (istream [(avg :bid) :symbol])
  (from [(window market-feed (range 400))]))
```

### Joins

```clojure
(select (istream [:*])
  (from [(window s1 (rows 5)) (window s2 (rows 10))])
  (where (= s1.a s2.a)))
```


### Defaults

```clojure
(select [:*]
  (from [market-feed])
  (where (= :symbol "APPL")))
```