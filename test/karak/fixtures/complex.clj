(ns karak.fixtures.complex)

(def raw
  "Hello, @hoge@fuga.jp, this is @piyo talking.
Lorem ipsum **dolor** sit amet `consectetuer` adipiscing elit.

```clojure
(dummy code
  \"including `dummy strings`
    and newlines too\")
```
Lorem *ipsum* dolor [sit](amet) [consectetuer](http://hoge.jp) adipiscing elit.

Lorem ipsum http://fuga.jp#fragment dolor sit
amet #consectetuer adipiscing elit.
```html
<p>This shouldn't be unescaped</p>
```

<div onclick=\"window.close()\">This shouldn't be unescaped either.</div>

Goodbye")

(def expected
  {:text
   "<p>Hello, <a href=\"https://example.com\" rel=\"noopener\" target=\"_blank\" class=\"status-link mention\"><span>@hoge@fuga.jp</span</a>, this is <a href=\"https://example.com\" rel=\"noopener\" target=\"_blank\" class=\"status-link mention\"><span>@piyo</span</a> talking.</p><p>Lorem ipsum <strong>dolor</strong> sit amet <code>consectetuer</code> adipiscing elit.</p><code><pre>(dummy code<br />  &quot;including `dummy strings`<br />    and newlines too&quot;)</pre></code><p>Lorem <em>ipsum</em> dolor [sit](amet) <a href=\"http://hoge.jp\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">consectetuer</a> adipiscing elit.</p><p>Lorem ipsum <a href=\"http://fuga.jp#fragment\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">fuga.jp#fragment</a> dolor sit</p><p>amet <a href=\"https://example.com\" class=\"status-link\" rel=\"noopener\" target=\"_blank\">#consectetuer</a> adipiscing elit.</p><code><pre>&lt;p&gt;This shouldn&#39;t be unescaped&lt;/p&gt;</pre></code><p>&lt;div onclick=&quot;window.close()&quot;&gt;This shouldn&#39;t be unescaped either.&lt;/div&gt;</p><p>Goodbye</p>",
   :length 493,
   :links #{"http://fuga.jp#fragment" "http://hoge.jp"},
   :mentions #{{:uri "https://example.com", :name "@piyo"}
               {:uri "https://example.com", :name "@hoge@fuga.jp"}},
   :hashtags #{{:uri "https://example.com", :name "consectetuer"}}})

(defn dummy-lookup [x] {:uri "https://example.com" :name x})
