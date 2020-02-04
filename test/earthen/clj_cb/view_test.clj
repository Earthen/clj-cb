(ns earthen.clj-cb.view-test
  (:require [clojure.test :refer :all]
            [earthen.clj-cb.bucket :as b]
            [earthen.clj-cb.bucket-manager :as bm]
            [earthen.clj-cb.fixtures :as fx]))

(def books [{:name "living-clojure0"
             :year 2000
             :pages 12}
            {:name "living-clojure1"
             :pages 12}
            {:name "living-clojure2"
             :year 2020
             :pages 2}])

(defn init-bucket
  [f]
  (fx/re-authenticate "earthen" "earthen")
  (let [bucket (fx/bucket "earthen_test2")]
    (bm/flush! (b/manager bucket))
    (dorun (map #(b/replace! bucket (:name %) %) books)))
  (f))

(use-fixtures :once init-bucket)

(deftest read-views
  (testing "should read views"
    (let [manager (b/manager  (fx/bucket "earthen_test2"))]
      (is (= 1 (count (bm/designed-documents manager))))
      (is (= "test_designdoc" (:name (first (bm/designed-documents manager)))))
      (is (= "by_year" (:name (first (:views (first (bm/designed-documents manager))))))))))

(deftest query-view-update
  (testing "should query by view with stale false"
    (let [bucket (fx/bucket "earthen_test2")]
      (is (= 2 (count (b/v-query bucket "test_designdoc" "by_year" {:stale :false})))))))

(deftest query-view-limit
  (testing "should query by view with limit"
    (let [bucket (fx/bucket "earthen_test2")]
      (is (= 1 (count (b/v-query bucket "test_designdoc" "by_year" {:stale :false :limit 1})))))))

(deftest query-view-key
  (testing "should query by view with key"
    (let [bucket (fx/bucket "earthen_test2")]
      (is (= 1 (count (b/v-query bucket "test_designdoc" "by_year" {:stale :false :key 2020}))))
      (is (= "living-clojure2" (:name (first (b/v-query bucket "test_designdoc" "by_year" {:stale :false :key 2020}))))))))
