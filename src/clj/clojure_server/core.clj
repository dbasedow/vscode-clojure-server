(ns clojure-server.core
  (:require [clojure.string :as str]
            [clojure-server.methods :as methods]
            [clojure-server.io :as io]
            [cheshire.core :as json]
            [clojure.core.async :refer [go-loop >!! <! close! chan pub alts! sub unsub timeout]])
  (:import [jline.console ConsoleReader])
  (:gen-class))

(defn handle-msg [enc]
  (let [msg (json/parse-string enc keyword)]
    (case (:method msg)
      "initialize" (methods/initialize-method msg)
      "textDocument/didChange" (methods/document-changed-method msg)
      "textDocument/didOpen" (methods/document-did-load-method msg)
      "textDocument/formatting" (methods/document-format msg)
      "textDocument/didSave" (methods/document-did-save msg)
      nil)))

(defn -main [& args]
  (loop []
    (let [headers (io/read-headers)
          payload (io/read-payload headers)]
          (some->> (handle-msg payload)
                   (>!! io/out-chan))
    (recur))))
