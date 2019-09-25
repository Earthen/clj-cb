(ns earthen.clj-cb.cluster-test
  (:require [clojure.test :refer :all]
            [earthen.clj-cb.cluster :as c]
            [earthen.clj-cb.utils :as u]
            [earthen.clj-cb.fixtures :as fx]))

(use-fixtures :once fx/init)

(defn- get-bucket
  [name]
  (->> (c/buckets (fx/manager)) (some #(if (= (:name %) name) %))))

(deftest create-cluster
  (testing "testing buckets"
    (is (= fx/bucket-name (->> (c/buckets (fx/manager)) (map :name) (some #{fx/bucket-name}))) "no bucket is created"))
  (testing "creating insert-bucket!"
    (let [created-bucket (get-bucket fx/bucket-name)]
      (is (= (dissoc fx/default-bucket-settings :password) (dissoc created-bucket :password))))))

(deftest disconnect
  (testing "disconnect"
    (is (= true (c/disconnect @fx/cluster)))))

