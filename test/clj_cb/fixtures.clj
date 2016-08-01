(ns clj-cb.fixtures
  (:require [clojure.test :refer :all]
            [earthen.clj-cb.cluster :as c]
            [earthen.clj-cb.cluster]))

(def cluster (c/create))
(def bucket-name "earthen_test")
(def default-bucket-settings {:name bucket-name
                              :type :COUCHBASE
                              :quota 100
                              :port 0
                              :password ""
                              :replicas 0
                              :index-replicas false
                              :flush? true})

(defn bucket
  []
  (c/open-bucket cluster bucket-name))

(defn manager
  []
  (c/manager cluster {:username "earthen" :password "earthen"}))

;; (defn init-bucket
;;   [f]
;;   (c/remove-bucket! manager bucket-name)
;;   (let [create-bucket (c/insert-bucket! manager default-bucket-settings)
;;         get-bucket (fn [name] (->> (c/buckets manager) (some #(if (= (:name %) name) %))))
;;         manager (c/manager cluster {:username "earthen" :password "earthen"})]
;;     (f)))

(defn init
  [f]
  (c/remove-bucket! manager bucket-name)
  (c/insert-bucket! manager default-bucket-settings)
  (f))
