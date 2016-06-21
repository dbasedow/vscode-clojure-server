(ns clojure-server.validate
  (:require [clojure.string :as str]
            [cljfmt.core :as fmt]
            [rewrite-clj.parser :as p]
            [clojure-server.diff :as diff]
            [clojure-server.io :as io]
            [cheshire.core :as json]))

(defn extract-location [msg]
    (let [[_ error line char] (re-find #"(.*) \[at line (\d+), column (\d+)\]" msg)]
        {:range {
            :start {:line line :character char}
            :end {:line line :character (inc (Integer. char))}}
         :message error}))

(defn parse-error?
    "Try to parse source. Returns [line char] on parse error"
    [txt]
    (try
        (p/parse-string-all txt)
        nil
        (catch Exception e
            (extract-location (.getMessage e)))))
