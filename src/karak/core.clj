(ns karak.core
  (:require [karak.transformers :refer [defaults]]))

(defn one-transform
  [arr trf lookups]
  (reduce
    (fn [aggr [tag text :as match]]
      (concat aggr
              (if (= tag :text)
                (trf text lookups)
                [match])))
    []
    arr))

(defn to-tagged-vec
  [text {:keys [transformers] :or {transformers defaults} :as options}]
  (let [lookups (dissoc options :transformers)]
    (reduce
      (fn [arr trf] (one-transform arr trf lookups))
      [[:text text]]
      transformers)))

(defn process
  "Converts the markdown in `input` (string) using the given options:
   |     option     | description
   |----------------|-------------
   | user-lookup    | Function to search users by @name(@domain). Should return
                        a map with :uri in it.
   | hashtag-lookup | Function to search hashtags. Should return a map with :uri
                        in it.
   | transformers   | (optional) Vector of transformer functions (presumably
                        from karak.transformers). Defaults to k.t/defaults

   Returns a map of the following format:
   :text     The actual processed text
   :length   The total length of the visible bits of the text (excludes tags etc)
   :mentions Collection of mentions
   :hashtags Collection of hashtags
   :links    Collection of other links"
  [input options]
  (reduce
    (fn [{:keys [length text links mentions hashtags]} [type match meta]]
      {:text (str text match)
       :length (case type
                 (:raw :text) (+ length (count match))
                 length)
       :links (case type
                :link (conj links meta)
                links)
       :mentions (case type
                   :mention (conj mentions meta)
                   mentions)
       :hashtags (case type
                   :hashtag (conj hashtags meta)
                   hashtags)})
    {:text "" :length 0 :links #{} :mentions #{} :hashtags #{}}
    (to-tagged-vec input options)))
