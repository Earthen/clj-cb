(defproject earthen/clj-cb "0.2.4-SNAPSHOT"
  :description "A Clojure java-client wrapper for Couchbase Server 4.x"
  :url "https://github.com/Earthen/clj-cb"
  :license {:name "Apache Public License"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.couchbase.client/java-client "2.7.10"]]
  :profiles {:dev {:dependencies [[ch.qos.logback/logback-classic "1.1.2"]]}}
  :plugins [[lein-codox "0.9.4"]]
  :test-selectors {:default  #(not (:query %))
                   :query :query
                   :all (constantly true)})
