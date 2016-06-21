(ns clojure-server.methods
  (:require [clojure.string :as str]
            [cljfmt.core :as fmt]
            [rewrite-clj.parser :as p]
            [clojure-server.diff :as diff]
            [clojure-server.io :as io]
            [clojure-server.validate :as vali]
            [cheshire.core :as json]
            [clojure.core.async :refer [go >!! <!! close! chan pub alts! sub unsub timeout]]))

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

(defn publish-diagnostics [uri]
    (let [content (get @documents uri)
          parse-diags (vali/parse-error? content)]
        (>!! io/out-chan
        {:method "textDocument/publishDiagnostics"
         :params {
            :uri uri
            :diagnostics (if parse-diags [parse-diags] [])
          }})))

(defn document-did-save [msg]
    (let [uri (get-in msg [:params :textDocument :uri])
          content (get @documents uri)]
        (go
            (publish-diagnostics uri)))
    nil)
