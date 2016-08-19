(ns clj-cb.bucket-test
  (:require [clojure.test :refer :all]
            [earthen.clj-cb.bucket :as b]
            [earthen.clj-cb.utils :as u]
            [clj-cb.fixtures :as fx]))

(def book {:name "living-clojure"
           :year 2000
           :pages 12})

(use-fixtures :once fx/init)

(deftest crud
  (let [item (b/replace! (fx/bucket) (:name book) book)]
    (is (= book (:content item)) "insert/replace")
    (is (= item (b/get (fx/bucket) (:name book))))))

