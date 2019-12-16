(ns earthen.clj-cb.bucket-test
  (:require [clojure.test :refer :all]
            [earthen.clj-cb.bucket :as b]
            [earthen.clj-cb.utils :as u]
            [earthen.clj-cb.fixtures :as fx]))

(def book {:name "living-clojure"
           :year 2000
           :pages 12})

(def bigger-book {:name "bigger-living-clojure"
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
    (is (= {} (b/lookup-in (fx/bucket) "missing-id" ["xyz"])))
    (is (= {:editions.2000 "1"} (b/lookup-in (fx/bucket) (:name bigger-book) ["editions.2000"])))
    (is (= {:editions.2000 "1" :pages 12} (b/lookup-in (fx/bucket) (:name bigger-book) ["editions.2000" "pages"])))
    (is (= {:editions.2001 "2" :publishers ["foo" "bar"]} (b/lookup-in (fx/bucket) (:name bigger-book) ["editions.2001" "publishers"])))
    (is (= {:editions.2001 "2" :publishers ["foo" "bar"] :references<1>.item.label 99} (b/lookup-in (fx/bucket) (:name bigger-book) ["editions.2001" "publishers" "references[1].item.label"])))
    (is (= {:exists nil} (b/lookup-in (fx/bucket) (:name bigger-book) ["exists"])))
    (is (= {:exception "Path mismatch \"year.missing\" in bigger-living-clojure"} (b/lookup-in (fx/bucket) (:name bigger-book) ["year.missing"])))))

(deftest query
  (fx/authenticate "earthen" "earthen")
  (b/create-primary-index (fx/bucket))
  (dorun
   (map #(b/replace!
          (fx/bucket)
          (str (:name bigger-book) "-" %)
          (assoc bigger-book :name (str (:name bigger-book) "-" %)))
        (range 10)))
  (Thread/sleep 1000)
  (is (= 10 (count (:rows (b/query (fx/bucket) "SELECT meta().id, editions FROM `earthen_test`")))))
  (is (= 0 (count (:rows (b/query (fx/bucket) "SELECT * FROM `earthen_test` where name = \"not found\"")))))
  (is (= 1 (count (:rows (b/query (fx/bucket) "SELECT * FROM `earthen_test` where name = \"bigger-living-clojure-9\"")))))
  (let [result 
        (b/query (fx/bucket) "SELECT meta().id FROM `earthen_test` where name = \"bigger-living-clojure-7\"")]
    (is (= "success" (:status result)))
    (is (= 1 (count (:rows result))))
    (is (= "bigger-living-clojure-7" (:id (first (:rows result))))))
  (let [result 
        (b/query (fx/bucket) "SELECT meta().id FROM `earthen_test` LIMIT 3")]
    (is (= "success" (:status result)))
    (is (= 3 (count (:rows result))))
    (is (= "bigger-living-clojure-0" (:id (first (:rows result)))))
    (is (= "bigger-living-clojure-1" (:id (second (:rows result)))))
    (is (= "bigger-living-clojure-2" (:id (nth (:rows result) 2)))))
  (let [result 
        (b/query (fx/bucket) "SELECT meta().id FROM `earthen_test` OFFSET 3 LIMIT 3")]
    (is (= "success" (:status result)))
    (is (= 3 (count (:rows result))))
    (is (= "bigger-living-clojure-3" (:id (first (:rows result)))))
    (is (= "bigger-living-clojure-4" (:id (second (:rows result)))))
    (is (= "bigger-living-clojure-5" (:id (nth (:rows result) 2)))))
  (let [result 
        (b/query (fx/bucket) "SELECT meta().id FROM `earthen_test` OFFSET 9 LIMIT 1")]
    (is (= "success" (:status result)))
    (is (= 1 (count (:rows result))))
    (is (= "bigger-living-clojure-9" (:id (first (:rows result)))))
    )
  (let [result 
        (b/query (fx/bucket) "SELECT meta().id FROM `earthen_test` OFFSET 10 LIMIT 1")]
    (is (= "success" (:status result)))
    (is (= 0 (count (:rows result))))
    (is (= 0 (.resultCount (:n1ql-metrics result))))))

(deftest prepared-statement
  (testing "SELECT FROM"
    (is (= "SELECT foo FROM `bucket`"
           (.toString (b/statement {:select ["foo"]
                                    :from [{:i "bucket"}]})))))
  (testing "SELECT FROM WHERE"
    (is (= "SELECT foo FROM `bucket` WHERE foo = $val"
           (.toString (b/statement {:select ["foo"]
                                    :from [{:i "bucket"}]
                                    :where [{:eq ["foo" "$val"]}]})))))
  (testing "SELECT FROM WHERE EQ OR NE"
    (is (= "SELECT foo FROM bucket WHERE foo = $val1 OR foo != $val2"
           (.toString (b/statement {:select ["foo"]
                                    :from ["bucket"]
                                    :where [{:eq ["foo" "$val1"]}
                                            {:or {:ne ["foo" "$val2"]}}]})))))
  (testing "SELECT FROM WHERE OR LIMIT OFFSET"
    (is (= "SELECT foo FROM `bucket` WHERE foo = $val1 OR foo != $val2 LIMIT 10 OFFSET 10"
           (.toString (b/statement {:select ["foo"]
                                    :from [{:i "bucket"}]
                                    :where [{:eq ["foo" "$val1"]}
                                            {:or {:ne ["foo" "$val2"]}}]
                                    :limit 10
                                    :offset 10})))))
  (testing "SELECT FROM WHERE OR AND IS NULL LIMIT OFFSET"
    (is (= "SELECT foo FROM `bucket` WHERE foo = $val1 OR foo != $val2 AND `bar` IS NULL LIMIT 10 OFFSET 10"
           (.toString (b/statement {:select ["foo"]
                                    :from [{:i "bucket"}]
                                    :where [{:eq ["foo" "$val1"]}
                                            {:or {:ne ["foo" "$val2"]}}
                                            {:and {:is-null ["bar"]}}]
                                    :limit 10
                                    :offset 10})))))
  (testing "SELECT AS FROM WHERE GTE AND LT AND LTE LIMIT OFFSET"
    (is (= "SELECT `x` AS foo FROM `bucket` WHERE foo >= $val1 AND foo < 10 AND bar <= 200 LIMIT 10 OFFSET 10"
           (.toString (b/statement {:select [{:as ["foo" "x"]}]
                                    :from [{:i "bucket"}]
                                    :where [{:gte ["foo" "$val1"]}
                                            {:and {:lt ["foo" 10]}}
                                            {:and {:lte ["bar" 200]}}]
                                    :limit 10
                                    :offset 10})))))
  (testing "SELECT strings and `` AS FROM WHERE IS NOT OR IS NULL"
    (is (= "SELECT x, y, `z` AS foo FROM `bucket` WHERE `foo` IS NOT NULL OR `bar[0].status` IS NULL"
           (.toString (b/statement {:select ["x" "y" {:as ["foo" "z"]}]
                                    :from [{:i "bucket"}]
                                    :where [{:is-not-null ["foo"]}
                                            {:or {:is-null ["bar[0].status"]}}]})))))
  (testing "SELECT ALL FROM AS"
    (is (= "SELECT ALL foo FROM `another-bucket` AS bucket"
           (.toString (b/statement {:select-all ["foo"]
                                    :from [{:as ["bucket" "another-bucket"]}]})))))
  (testing "SELECT DISTINCT FROM GROUP BY ORDER BY ASC"
    (is (= "SELECT DISTINCT foo FROM `bucket` GROUP BY foo, bar ORDER BY meta().id ASC"
           (.toString (b/statement {:select-distinct ["foo"]
                                    :from [{:i "bucket"}]
                                    :group-by ["foo" "bar"]
                                    :order-by {:asc "meta().id"}})))))
  (testing "SELECT DISTINCT FROM (SELECT FROM) AS GROUP BY ORDER BY ASC"
    (is (= "SELECT DISTINCT foo FROM (SELECT foo, bar FROM bucket) AS t1 GROUP BY foo, bar ORDER BY meta().id ASC"
           (.toString (b/statement {:select-distinct ["foo"]
                                    :from [{:sub {:select ["foo" "bar"]
                                                  :from ["bucket"]}}
                                           {:as "t1"}]
                                    :group-by ["foo" "bar"]
                                    :order-by {:asc "meta().id"}})))))
  (testing "SELECT string AS FROM"
    (is (= "SELECT meta().id AS a, `pages` AS b FROM `earthen_test`"
           (.toString (b/statement {:select [{:s-as ["a" "meta().id"]} {:as ["b" "pages"]}]
                                    :from [{:i "earthen_test"}]}))))))

(deftest p-query
  (fx/authenticate "earthen" "earthen")
  (b/create-primary-index (fx/bucket))
  (let [item (b/replace! (fx/bucket) (:name bigger-book) bigger-book)]
    (dorun
     (map #(b/replace!
            (fx/bucket)
            (str (:name bigger-book) "-" %)
            (assoc bigger-book :name (str (:name bigger-book) "-" %)))
          (range 10)))
    (Thread/sleep 2000)
    (testing "Basic select an limit and not ad-hoc"
      (is (= 1 (count (:rows (b/query (fx/bucket) {:select ["*"]
                                                   :from [{:i "earthen_test"}]
                                                   :limit 1})))))
      (is (= 1 (count (:rows (b/query (fx/bucket) {:select ["*"]
                                                   :from [{:i "earthen_test"}]
                                                   :limit 1}
                                      (b/ad-hoc)))))))
    (testing "Parameter substitution of $title"
      (is (= 1 (count (:rows (b/p-query (fx/bucket) {:select ["pages"]
                                                     :from ["earthen_test"]
                                                     :where [{:eq ["name" "$title"]}]}
                                        {"title" "bigger-living-clojure"}))))))
    (testing "More complex queries"
      (is (= 11 (count (:rows (b/p-query (fx/bucket) {:select ["meta().id" "pages"]
                                                      :from [{:i "earthen_test"}]
                                                      :where [{:like ["meta().id" ["bigger-%"]]}]}
                                         {"id" "bigger-living-clojure-0"})))))
      (is (= 1 (count (:rows (b/p-query (fx/bucket) {:select ["meta().id" "pages"]
                                                     :from [{:i "earthen_test"}]
                                                     :where [{:like ["meta().id" ["bigger-%"]]}
                                                             {:and {:gt ["meta().id" "$id"]}}]}
                                        {"id" "bigger-living-clojure-8"})))))
      (is (= 2 (count (:rows (b/p-query (fx/bucket) {:select ["meta().id" "pages"]
                                                     :from [{:i "earthen_test"}]
                                                     :where [{:like ["meta().id" ["bigger-%"]]}
                                                             {:and {:gt ["meta().id" "$id"]}}]
                                                     :order-by {:asc "meta().id"}}
                                      {"id" "bigger-living-clojure-7"})))))
      (is (= 2 (count (:rows (b/p-query (fx/bucket) {:select ["meta().id" "pages"]
                                                     :from [{:i "earthen_test"}]
                                                     :use-index ["#primary"]
                                                     :where [{:like ["meta().id" ["bigger-%"]]}
                                                             {:and {:gt ["meta().id" "$id"]}}]
                                                     :order-by {:desc "meta().id"}
                                                     :limit 2}
                                        {"id" "bigger-living-clojure-7"})))))
      (is (= [{:a "bigger-living-clojure-9", :b 12} {:a "bigger-living-clojure-8", :b 12}]
             (:rows (b/p-query (fx/bucket) {:select [{:s-as ["a" "meta().id"]} {:as ["b" "pages"]}]
                                            :from [{:i "earthen_test"}]
                                            :use-index ["#primary"]
                                            :where [{:like ["meta().id" ["bigger-%"]]}
                                                    {:and {:gt ["meta().id" "$id"]}}]
                                            :order-by {:desc "meta().id"}
                                            :limit 2}
                               {"id" "bigger-living-clojure-7"})))))))


