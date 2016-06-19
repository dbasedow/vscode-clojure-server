(ns clojure-server.methods
  (:require [clojure.string :as str]
            [cljfmt.core :as fmt]
            [clojure-server.diff :as diff]
            [cheshire.core :as json]))

(def documents (atom {}))

(defn initialize-method [msg]
{
    :id (:id msg)
    :result {
        :capabilities {
            :textDocumentSync 1 ;Full
            :documentFormattingProvider true
        }
    }
})

(defn document-did-load-method [msg]
    (let [uri (get-in msg [:params :textDocument :uri])
          content (get-in msg [:params :textDocument :text])]
        (swap! documents assoc uri content))
    nil)

(defn document-changed-method [msg]
    (let [uri (get-in msg [:params :textDocument :uri])
          content (get-in msg [:params :contentChanges 0 :text])]
        (swap! documents assoc uri content))
    nil)

(defn document-format [msg]
    (let [uri (get-in msg [:params :textDocument :uri])
          content (get @documents uri)
          formatted (fmt/reformat-string content)
          changes (diff/diff content formatted)]
        {:id (:id msg) :result changes}))
