# clj-cb

DISCONTINUED AT THE MOMENT
If needed I can work on it again.

A Clojure java-client wrapper for Couchbase Server 4. Now updated to support 6.0 with Couchbase Java client version 2.7.9.

## Usage

```clojure
    (require '[earthen.clj-cb.cluster :as c]
                '[earthen.clj-cb.bucket :as b]
                '[earthen.clj-cb.bucket-manager :as bm])
    
    ;; we create the cluster
    (def cluster (c/create "localhost"))
    
    ;; we create a bucket
    (def bucket (c/open-bucket cluster "gamesim-sample"))

    ;; we authenticate with the bucket password, note: the password parameter on the c/bucket-open
    ;; no longer works and you will get the exception MixedAuthenticationException...
    ;; ...Mixed mode authentication not allowed, use Bucket credentials, User credentials (rbac) or Certificate auth


    (c/authenticate "gamesim-sample" "secret")
    
    ;; we get documents from bucket mapped
    (b/get bucket "Aaron1")
        => {:id "Aaron1", :cas 29856054464387, :expiry 0, :mutation-token nil, :content {:hitpoints 23832, :level 141, :loggedIn true, :name "Aaron1", :jsonType "player", :experience 248, :uuid "78edf902-7dd2-49a4-99b4-1c94ee286a33"}}
    
    ;; you can also get the json without mapping
     (b/get bucket "Aaron1" :raw)
         => "{\"hitpoints\":23832,\"level\":141,\"loggedIn\":true,\"name\":\"Aaron1\",\"jsonType\":\"player\",\"experience\":248,\"uuid\":\"78edf902-7dd2-49a4-99b4-1c94ee286a33\"}"
    
    ;; to create/update a json
    (b/replace! bucket "Aaron1" "{
        \"experience\": 248,
        \"hitpoints\": 23832,
        \"jsonType\": \"player\",
        \"level\": 141,
        \"loggedIn\": true,
        \"name\": \"Aaron1\",
        \"uuid\": \"78edf902-7dd2-49a4-99b4-1c94ee286a33\"
        \"sponsors\": [\"Nike\" \"Reebok\" \"Freddies\"]
        \"tues\": [{\"symptom\": {\"asthma\": \"Ventolin Inhler\"}}
                   {\"symptom\": {\"pain\": \"Asprin\"}}]  
        }")
        => {:id "Aaron1", :cas 1569768233881239552, :expiry 0, :mutation-token nil, :content {:name "Aaron1", :hitpoints 23832, :experience 248, :level 141, :loggedIn true, :tues [{:symptom {:asthma "Ventolin Inhler"}} {:symptom {:pain "Asprin"}}], :sponsors ["Nike" "Reebok" "Freddies"], :uuid "78edf902-7dd2-49a4-99b4-1c94ee286a33", :jsonType "player"}}
        
        (b/replace! bucket "Aaron1" {:name "Aaron1", :hitpoints 23832, :experience 248, :level 141, :loggedIn true, :tues [{:symptom {:asthma "Ventolin Inhler"}} {:symptom {:pain "Asprin"}}], :sponsors ["Nike" "Reebok" "Freddies"], :uuid "78edf902-7dd2-49a4-99b4-1c94ee286a33", :jsonType "player"})

        => {:id "Aaron1", :cas 1569768233881239552, :expiry 0, :mutation-token nil, :content {:name "Aaron1", :hitpoints 23832, :experience 248, :level 141, :loggedIn true, :tues [{:symptom {:asthma "Ventolin Inhler"}} {:symptom {:pain "Asprin"}}], :sponsors ["Nike" "Reebok" "Freddies"], :uuid "78edf902-7dd2-49a4-99b4-1c94ee286a33", :jsonType "player"}}

       ;; To lookup particular objects / values in a document, pass a list of paths. Vectors in
       ;; the response are converted from [n] to <n> as brackets are not valid in keywords.
       ;; The DocumentFragment used to execute the request also has an .exists method that returns
       ;; true or false for a given path. This has not been implemented. Instead you will get a
       ;; {:keyword nil} response for a noneixstent path.
	  
       (b/lookup-in bucket "Aaron1" "name" "sponsors" "tues[1]" "injuries")

       => {:name "Aaron1", :sponsors ["Nike" "Reebok" "Freddies"], :tues<1> {:symptom {:pain "Asprin"}}, :injuries nil}

```
## License

Copyright © 2019

Distributed under the Apache Public License either version 2.0.
