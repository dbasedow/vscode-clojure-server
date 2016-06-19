(ns clojure-server.core
  (:require [clojure.string :as str]
            [clojure-server.methods :as methods]
            [cheshire.core :as json])
  (:import [jline.console ConsoleReader])
  (:gen-class))

(defn parse-header-line [line]
  (str/split line #": "))

(defn read-line-unbuf []
  (let [cr (ConsoleReader.)]
    (loop [content ""]
      (let [c (.readCharacter cr)]
        (if (= c 10)
          (str/trimr content)
          (recur (str content (char c))))))))

(defn read-headers []
  (loop [headers {}]
    (let [line (read-line-unbuf)]
      (if (empty? line)
        headers
        (let [[header value] (parse-header-line line)]
          (recur (assoc headers header value)))))))

(defn read-payload [headers]
  (let [length (Integer. (get headers "Content-Length" 0))
        cr (ConsoleReader.)]
    (loop [content "" left length]
      (if (= left 0)
        content
        (let [c (char (.readCharacter cr))]
          (recur (str content c) (dec left)))))))

(defn handle-msg [enc]
  (let [msg (json/parse-string enc keyword)]
    (case (:method msg)
      "initialize" (methods/initialize-method msg)
      "textDocument/didChange" (methods/document-changed-method msg)
      "textDocument/didOpen" (methods/document-did-load-method msg)
      "textDocument/formatting" (methods/document-format msg)
      nil)))

(defn add-content-length [resp]
  (let [length (count resp)]
    (str "Content-Length: " length "\r\n" "\r\n" resp)))

(defn -main [& args]
  (loop []
    (let [headers (read-headers)
          payload (read-payload headers)]
          (some->> (handle-msg payload)
                   json/generate-string
                   add-content-length
                   print)
          (flush)
    (recur))))
