(ns karak.helpers
  (:require [clojure.string :refer [join]]))

(defn naive-flattener
  [result-vec]
  (join (map second result-vec)))
