(ns karak.inline-test
  (:require [clojure.test :refer :all]
            [karak.helpers :refer [naive-flattener]]
            [karak.transformers :refer :all]))

(defn user-finder [acct]
  (let [[[_ name domain]] (re-seq #"@(?:([a-z0-9][a-z0-9_.-]+)(?:@((?:[a-z0-9-_]+\.)*[a-z0-9]+))?)"
                                  acct)]
    {:uri (str "https://" (or domain "example.com") "/users/" name)}))

(defn hashtag-finder [tag]
  {:uri (str "https://example.com/hashtags/" tag)})

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
        "Doesn't allow nesting"))
  (testing "Links with a protocol get linkified"
    (is (= "<a href=\"ftp://example.com\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">example.com</a>"
           (naive-flattener (plain-link "ftp://example.com"))))
    (is (= "foo <a href=\"ftp://example.com\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">example.com</a> bar"
           (naive-flattener (plain-link "foo ftp://example.com bar"))))
    (is (= "foo <a href=\"ftp://example.com\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">example.com</a>"
           (naive-flattener (plain-link "foo ftp://example.com"))))
    (is (= "<a href=\"ftp://example.com\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">example.com</a> foo"
           (naive-flattener (plain-link "ftp://example.com foo"))))))

(deftest mention-test
  (testing "Converts @-mentions to links"
    (is (= "<a href=\"https://fuga.jp/users/hoge\" rel=\"noopener\" target=\"_blank\" class=\"status-link mention\"><span>@hoge@fuga.jp</span</a>"
           (naive-flattener (mention "@hoge@fuga.jp" {:user-lookup user-finder}))))
    (is (= "foo <a href=\"https://fuga.jp/users/hoge\" rel=\"noopener\" target=\"_blank\" class=\"status-link mention\"><span>@hoge@fuga.jp</span</a>"
           (naive-flattener (mention "foo @hoge@fuga.jp" {:user-lookup user-finder}))))
    (is (= "<a href=\"https://fuga.jp/users/hoge\" rel=\"noopener\" target=\"_blank\" class=\"status-link mention\"><span>@hoge@fuga.jp</span</a> bar"
           (naive-flattener (mention "@hoge@fuga.jp bar" {:user-lookup user-finder}))))
    (is (= "foo <a href=\"https://fuga.jp/users/hoge\" rel=\"noopener\" target=\"_blank\" class=\"status-link mention\"><span>@hoge@fuga.jp</span</a> bar"
           (naive-flattener (mention "foo @hoge@fuga.jp bar" {:user-lookup user-finder})))))
  (testing "Can handle domain-less mentions too"
    (is (= "<a href=\"https://example.com/users/hoge\" rel=\"noopener\" target=\"_blank\" class=\"status-link mention\"><span>@hoge</span</a>"
           (naive-flattener (mention "@hoge" {:user-lookup user-finder})))))
  (testing "The URI lookup function used can be passed in"
    (let [lookup-user (constantly {:uri "https://example.com"})]
      (is (= "<a href=\"https://example.com\" rel=\"noopener\" target=\"_blank\" class=\"status-link mention\"><span>@hoge</span</a>"
             (naive-flattener (mention "@hoge" {:user-lookup lookup-user}))))
      (is (= "<a href=\"https://example.com\" rel=\"noopener\" target=\"_blank\" class=\"status-link mention\"><span>@hoge@fuga.jp</span</a>"
             (naive-flattener (mention "@hoge@fuga.jp" {:user-lookup lookup-user})))))))

(deftest hashtag-test
  (testing "Converts #-tags to links"
    (is (= "<a href=\"https://example.com/hashtags/hoge\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">#hoge</a>"
           (naive-flattener (hashtag "#hoge" {:hashtag-lookup hashtag-finder}))))
    (is (= "foo <a href=\"https://example.com/hashtags/hoge\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">#hoge</a>"
           (naive-flattener (hashtag "foo #hoge" {:hashtag-lookup hashtag-finder}))))
    (is (= "<a href=\"https://example.com/hashtags/hoge\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">#hoge</a> bar"
           (naive-flattener (hashtag "#hoge bar" {:hashtag-lookup hashtag-finder}))))
    (is (= "foo <a href=\"https://example.com/hashtags/hoge\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">#hoge</a> bar"
           (naive-flattener (hashtag "foo #hoge bar" {:hashtag-lookup hashtag-finder}))))))
