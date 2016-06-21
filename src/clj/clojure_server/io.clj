(ns clojure-server.io
  (:require [clojure.string :as str]
            [cheshire.core :as json]
            [clojure.core.async :refer [go-loop >!! <! close! chan pub alts! sub unsub timeout]])
  (:import [jline.console ConsoleReader]))

(def out-chan (chan 20))

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

(defn add-content-length [resp]
  (let [length (count resp)]
    (str "Content-Length: " length "\r\n" "\r\n" resp)))

(defn dumper [data]
  (spit "out.log" data :append true)
  data)

(go-loop []
    (->>  (<! out-chan)
            json/generate-string
            add-content-length
            dumper
            print)
    (flush)
    (recur))
