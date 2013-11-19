(defproject bing-ads-api "0.1.0"
  :description "Clojure access to bing api version 9"
  :url "http://mateoconfeugo.github.io/bing-ads-api/"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[axis "1.4"]
                 [clj-http "0.7.7"]
                 [com.datomic/datomic-free "0.8.4218"]
                 [com.microsoft/customermanagement "0.0.9"]
                 [com.microsoft/optimizer "0.0.9"]
                 [com.microsoft/reporting "0.0.9"]
                 [com.microsoft/adintelligence "0.0.9"]
                 [com.microsoft/bulk "0.0.9"]
                 [com.microsoft/campaignmanagement "0.0.9"]
                 [com.microsoft/customerbilling "0.0.9"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [into-edn "1.0.2"]
                 [me.raynes/fs "1.4.4"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/core.match "0.2.0"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [prismatic/plumbing "0.1.1"]]
  :plugins [[lein-marginalia "0.7.1"]
            [lein-expectations "0.0.8"]
            [lein-autoexpect "0.2.5"]])
