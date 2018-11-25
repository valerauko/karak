(ns karak.transformers-test
  (:require [clojure.test :refer :all]
            [clojure.string :refer [join]]
            [karak.transformers :refer :all]))

(defn naive-flattener
  [result-vec]
  (join (map second result-vec)))

(deftest basics-test
  (testing "One asterisk means italic"
    (is (= "hoge <em>fuga</em> piyo"
           (naive-flattener (italic "hoge *fuga* piyo"))))
    (is (= "<em>hoge fuga</em> piyo"
           (naive-flattener (italic "*hoge fuga* piyo"))))
    (is (= "hoge <em>fuga piyo</em>"
           (naive-flattener (italic "hoge *fuga piyo*"))))
    (is (= "<em>hoge fuga piyo</em>"
           (naive-flattener (italic "*hoge fuga piyo*")))))
  (testing "Two asterisks means bold"
    (is (= "hoge <strong>fuga</strong> piyo"
           (naive-flattener (bold "hoge **fuga** piyo"))))
    (is (= "<strong>hoge fuga</strong> piyo"
           (naive-flattener (bold "**hoge fuga** piyo"))))
    (is (= "hoge <strong>fuga piyo</strong>"
           (naive-flattener (bold "hoge **fuga piyo**"))))
    (is (= "<strong>hoge fuga piyo</strong>"
           (naive-flattener (bold "**hoge fuga piyo**"))))))
