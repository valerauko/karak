(ns karak.block-test
  (:require [clojure.test :refer :all]
            [clojure.string :refer [join]]
            [karak.helpers :refer [naive-flattener]]
            [karak.transformers :refer :all]))

(deftest code-block-test
  (testing "Blocks wrapped in triple backtick turn into codeblocks"
    (is (= "<code><pre>hoge</pre></code>"
           (naive-flattener (code-block "```\nhoge\n```"))))
    (is (= "foo<code><pre>hoge</pre></code>"
           (naive-flattener (code-block "foo\n```\nhoge\n```"))))
    (is (= "<code><pre>hoge</pre></code>bar"
           (naive-flattener (code-block "```\nhoge\n```\nbar"))))
    (is (= "foo<code><pre>hoge</pre></code>bar"
           (naive-flattener (code-block "foo\n```\nhoge\n```\nbar")))))
  (testing "Can handle multiline blocks"
    (is (= "<code><pre>hoge<br />fuga</pre></code>"
           (naive-flattener (code-block "```\nhoge\nfuga\n```")))))
  (testing "Language tagging is currently ignored"
    (is (= "<code><pre>hoge<br />fuga</pre></code>"
           (naive-flattener (code-block "```clojure\nhoge\nfuga\n```"))))))

(deftest paragraph-test
  (testing "Text with an empty line before or after is considered a paragraph"
    (is (= "<p>hoge</p>"
           (naive-flattener (paragraph "hoge"))))
    (is (= "<p>hoge</p><p>fuga</p>"
           (naive-flattener (paragraph "hoge\n\nfuga"))))
    (is (= "<p>hoge</p><p>fuga</p><p>piyo</p>"
           (naive-flattener (paragraph "hoge\n\nfuga\n\npiyo")))))
  (testing "Single newlines (eg no empty line) are treated as line breaks"
    (is (= "<p>ho<br />ge</p>"
           (naive-flattener (paragraph "ho\nge"))))
    (is (= "<p>ho<br />ge</p><p>fu<br />ga</p>"
           (naive-flattener (paragraph "ho\nge\n\nfu\nga"))))
    (is (= "<p>ho<br />ge</p><p>fu<br />ga</p><p>pi<br />yo</p>"
           (naive-flattener (paragraph "ho\nge\n\nfu\nga\n\npi\nyo"))))))
