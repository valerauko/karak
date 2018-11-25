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
           (naive-flattener (bold "**hoge fuga piyo**")))))
  (testing "Backtick means code"
    (is (= "hoge <code>f&lt;ug&gt;a</code> piyo"
           (naive-flattener (code "hoge `f<ug>a` piyo"))))
    (is (= "<code>hoge f&lt;ug&gt;a</code> piyo"
           (naive-flattener (code "`hoge f<ug>a` piyo"))))
    (is (= "hoge <code>f&lt;ug&gt;a piyo</code>"
           (naive-flattener (code "hoge `f<ug>a piyo`"))))
    (is (= "<code>hoge f&lt;ug&gt;a piyo</code>"
           (naive-flattener (code "`hoge f<ug>a piyo`"))))))

(deftest link-test
  (testing "[title](link) gets converted to a named link"
    (is (= "hoge <a href=\"https://piyo\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">fuga</a> puyo"
           (naive-flattener (named-link "hoge [fuga](https://piyo) puyo"))))
    (is (= "<a href=\"https://piyo\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">hoge fuga</a> puyo"
           (naive-flattener (named-link "[hoge fuga](https://piyo) puyo"))))
    (is (= "hoge <a href=\"https://piyo\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">fuga puyo</a>"
           (naive-flattener (named-link "hoge [fuga puyo](https://piyo)"))))
    (is (= "<a href=\"https://piyo\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">hoge fuga puyo</a>"
           (naive-flattener (named-link "[hoge fuga puyo](https://piyo)"))))
    (is (= "hoge [fuga](piyo) puyo <a href=\"ftp://bar\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">foo</a> baz"
           (naive-flattener (named-link "hoge [fuga](piyo) puyo [foo](ftp://bar) baz")))
        "Requries the presence of a protocol")
    (is (= "<a href=\"ftp://bar\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">http&#58;//foo.com</a> asd"
           (naive-flattener (named-link "[http://foo.com](ftp://bar) asd")))
        "Doesn't allow nesting")))
