(ns org.pathirage.freshet.expresssionserde-test
  (:import (org.pathirage.freshet.utils ExpressionSerde))
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [org.pathirage.freshet.utils.expressions :as expressions]))

(deftest expression-serde-test
  (testing "Expression serializing and deserializing"
    (let [serialized-expr (ExpressionSerde/serialize expressions/where-diff-bytes->-100)
          expr (ExpressionSerde/deserialize serialized-expr)]
      (is (= serialized-expr (ExpressionSerde/serialize expr))))))


