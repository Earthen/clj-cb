(ns earthen.clj-cb.bucket-test
  (:require [clojure.test :refer :all]
            [earthen.clj-cb.bucket :as b]
            [earthen.clj-cb.utils :as u]
            [earthen.clj-cb.fixtures :as fx]))

(def book {:name "living-clojure"
           :year 2000
           :pages 12})

(use-fixtures :each fx/init)

(deftest crud
  (fx/authenticate "earthen" "earthen")
  (let [item (b/replace! (fx/bucket) (:name book) book)]
    (is (= book (:content item)) "insert/replace")
    (is (= item (b/get (fx/bucket) (:name book))))))

(deftest crud-authentication-fail
  (fx/authenticate "earthen" "notearthen")
  (is (thrown-with-msg?  com.couchbase.client.java.error.InvalidPasswordException
                         #"Passwords for bucket \"earthen_test\" do not match."
                         (b/replace! (fx/bucket) (:name book) book)))
  (is (thrown-with-msg?  com.couchbase.client.java.error.InvalidPasswordException
                         #"Passwords for bucket \"earthen_test\" do not match."
                         (b/get (fx/bucket) (:name book)))))
