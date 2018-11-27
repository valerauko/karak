(ns karak.transformers
  (:require [clojure.string :as string]))

; TODO: the css classes could be passed as parameters too.

(defn escape-raw
  [input]
  (string/escape input {\< "&lt;" \> "&gt;" \& "&amp;" \' "&#39;" \" "&quot;"}))

(defn wrap
  [formatter matches]
  (reduce
    (fn [aggr [_ pre & stuck]]
      (let [hit (butlast stuck)
            post (last stuck)]
        (concat
          aggr
          (if (empty? pre) [] [[:text pre]])
          (if (first hit) (formatter hit) [])
          (if (empty? post) [] [[:text post]]))))
    []
    matches))

(defn inline
  ([text delimiter element]
    (inline text delimiter element :text))
  ([text delimiter element tag]
    (let [rex (re-pattern
                (str "(.*?\\pZ?)(?:(?<=^|\\pZ)"
                     delimiter
                     "(.+?)"
                     delimiter
                     "(?=\\pZ|$))?(.*?(?=\\pZ"
                     delimiter
                     "|$))"))
          matches (re-seq rex text)]
      (wrap (fn [[hit]]
              [[:meta (str "<" element ">")]
               [tag (if (= tag :raw) (escape-raw hit) hit)]
               [:meta (str "</" element ">")]])
            matches))))

(defn italic
  [text & _]
  (inline text #"\*" "em"))

(defn bold
  [text & _]
  (inline text #"\*\*" "strong"))

(defn code
  [text & _]
  (inline text #"`" "code" :raw))

(defn named-link
  [text & _]
  (let [matches (re-seq #"(.*?)(?:\[([^\]]+)\]\(([a-z]+://[^\pZ)\"]+)\))?(\[?.*?(?=\[|$))" text)]
    (wrap (fn [[title url]]
            [[:link (str "<a href=\"" url "\" "
                         "class=\"status-link\" rel=\"noopener\" target=\"_blank\">")
                    url]
             ; doing this to prevent nested links
             [:text (string/replace title #"://" "&#58;//")]
             [:meta "</a>"]])
          matches)))

(defn plain-link
  [text & _]
  (let [matches (re-seq #"(.*?\pZ?)((?<=^|\pZ)(?:[a-z]+://)([^\pZ\"]+)(?=[\pZ\"]|$))?(.*?(?=\pZ(?:https?|ftp)|$))" text)]
    (wrap (fn [[full no-scheme]]
            [[:link (str "<a href=\"" full "\" "
                         "class=\"status-link\" rel=\"noopener\" target=\"_blank\">")
                    full]
             [:raw (escape-raw (if (> (count no-scheme) 20)
                                 (str (subs no-scheme 0 18) "…")
                                 no-scheme))]
             [:meta "</a>"]])
          matches)))

(defn mention
  [text {:keys [user-lookup]}]
  (let [matches (re-seq #"(.*?\pZ?)(?:(?<=^|\pZ)@(?:([a-z0-9][a-z0-9_.-]+)(?:@((?:[a-z0-9-& _]+\.)*[a-z0-9]+))?))?(.*?(?=\pZ@|$))" text)]
    (wrap (fn [[name host]]
            (let [acct (str "@" name (if host (str "@" host)))]
              (if-let [user (user-lookup acct)]
                [[:mention (str "<a href=\"" (:uri user) "\" "
                                "rel=\"noopener\" target=\"_blank\" "
                                "class=\"status-link mention\">")
                           user]
                 ; FIXME: the @ is underlined
                 [:raw (str "<span>" (escape-raw acct) "</span")]
                 [:meta "</a>"]]
                [[:raw acct]])))
          matches)))

(defn hashtag
  [text {:keys [hashtag-lookup]}]
  (let [matches (re-seq #"(.*?\pZ?)(?:(?<=^|\pZ)#([\pL\pN_]+))?(.*?(?=\pZ#|$))" text)]
    (wrap (fn [[tag]]
            (let [hashtag (hashtag-lookup tag)]
              [[:hashtag (str "<a href=\"" (:uri hashtag) "\" "
                              "class=\"status-link\" rel=\"noopener\" target=\"_blank\""
                              ">") hashtag]
               [:raw (str "#" (escape-raw tag))]
               [:meta (str "</a>")]]))
          matches)))

(defn code-block
  [text & _]
  (let [matches (re-seq #"(?ms)(.*?)(?:(?:^```\w*$)(.+?)(?:^```$))?()$"
                        (string/replace text #"\r" ""))]
    (wrap (fn [[multiline-code]]
            [[:meta "<code><pre>"]
             [:raw (-> multiline-code
                       string/trim
                       escape-raw
                       (string/replace #"\n" "<br />"))]
             [:meta "</pre></code>"]])
          matches)))

(defn paragraph
  [text & _]
  (let [matches (re-seq #"(?ms)(?<=\A|\n\n)(?:()(.+?)())(?=\z|\n\n)"
                        (string/replace text #"\r" ""))] ; get rid of \r jic
    (wrap (fn [[paragraph-text]]
            [[:meta "<p>"]
             [:text (-> paragraph-text
                        string/trim
                        escape-raw
                        (string/replace #"\n" "<br />"))]
             [:meta "</p>"]])
          matches)))

(def defaults
  "Block-level transformers should come before inline ones.
   Also ones that produce :raw before :text producers."
  [code-block
   paragraph
   code
   named-link
   plain-link
   mention
   hashtag
   bold
   italic])
