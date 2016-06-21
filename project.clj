(defproject clojure-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [jline "2.12"]
                 [cljfmt "0.5.3"]
                 [org.clojure/core.async "0.2.385"]
                 [cheshire "5.6.1"]]
  :main ^:skip-aot clojure-server.core
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
