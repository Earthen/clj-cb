(ns earthen.clj-cb.bucket-test
  (:require [clojure.test :refer :all]
            [earthen.clj-cb.bucket :as b]
            [earthen.clj-cb.utils :as u]
            [earthen.clj-cb.fixtures :as fx]))

(def book {:name "living-clojure"
           :year 2000
           :pages 12})

(def bigger-book {:name "biger-living-clojure"
                  :year 2000
                  :pages 12
                  :editions {:2000 "1" :2001 "2"}
                  :publishers ["foo" "bar"]
                  :references [{:item "ref-item-0"}
                               {:item {:label 99}}]})


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

(deftest crud-get-fail
  (fx/authenticate "earthen" "earthen")
  (is (= nil (b/get (fx/bucket) "nonexistent-book"))))

(deftest lookup-in
  (fx/authenticate "earthen" "earthen")
  (let [item (b/replace! (fx/bucket) (:name bigger-book) bigger-book)]
    (is (= bigger-book (:content item)) "insert/replace")
    (is (= item (b/get (fx/bucket) (:name bigger-book))))
    (is (= {} (b/lookup-in (fx/bucket) "missing-id" "xyz")))
    (is (= {:editions.2000 "1"} (b/lookup-in (fx/bucket) (:name bigger-book) "editions.2000")))
    (is (= {:editions.2000 "1" :pages 12} (b/lookup-in (fx/bucket) (:name bigger-book) "editions.2000" "pages")))
    (is (= {:editions.2001 "2" :publishers ["foo" "bar"]} (b/lookup-in (fx/bucket) (:name bigger-book) "editions.2001" "publishers")))
    (is (= {:editions.2001 "2" :publishers ["foo" "bar"] :references<1>.item.label 99} (b/lookup-in (fx/bucket) (:name bigger-book) "editions.2001" "publishers" "references[1].item.label")))
        (is (= {:exists nil} (b/lookup-in (fx/bucket) (:name bigger-book) "exists")))))

