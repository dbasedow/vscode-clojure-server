(ns clojure-server.validate
  (:require [clojure.string :as str]
            [cljfmt.core :as fmt]
            [rewrite-clj.parser :as p]
            [clojure-server.diff :as diff]
            [clojure-server.io :as io]
            [cheshire.core :as json]))

(defn extract-location [msg]
    (let [[_ error l c] (re-find #"(.*) \[at line (\d+), column (\d+)\]" msg)
          character (dec (Integer. c))
          line (dec (Integer. l))]
        {:range {
            :start {:line line :character character}
            :end {:line line :character (inc character)}}
         :message error}))

(defn parse-error?
    "Try to parse source. Returns [line char] on parse error"
    [txt]
    (try
        (p/parse-string-all txt)
        nil
        (catch Exception e
            (extract-location (.getMessage e)))))
