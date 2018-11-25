(ns karak.core-test
  (:require [clojure.test :refer :all]
            [karak.fixtures.complex :as fix]
            [karak.core :refer :all]))

(deftest process-test
  (testing "Works as advertised (see docstring and fixtures for detail)"
    (is (= (process fix/raw {:user-lookup fix/dummy-lookup
                             :hashtag-lookup fix/dummy-lookup})
           fix/expected))))
