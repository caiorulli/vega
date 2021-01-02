(defproject vega "0.1.0-SNAPSHOT"
  :description "My personal telegram bot"
  :url "https://github.com/crthomaz/vega"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [environ             "1.2.0"]
                 [morse               "0.4.3"]]

  :plugins [[lein-environ "1.2.0"]]

  :main ^:skip-aot vega.core
  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}})
