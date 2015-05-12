(ns twit-finity.search
  (:use
    [twitter.oauth :only [make-oauth-creds]]
    [twitter.api.restful :only [search-tweets]])
  (:require 
    [clojure.data.json :as json]
    [http.async.client :as ac]
    [twit-finity.auth :as auth])
  (:import
    (twitter.callbacks.protocols SyncSingleCallback)))

(def my-creds (make-oauth-creds (:consumer-key auth/credentials)
                                (:consumer-secret auth/credentials)
                                (:access-token auth/credentials)
                                (:access-token-secret auth/credentials)))

(def ^:const results-per-search 100)
(def ^:const time-between-searches 20000)
(def ^:const result-type "recent")
(def ^:const lang "en")
(def ^:const initial-since-id "0")


(defn search  
  "Sends GET request to search/tweets"
  [query count lang since-id result-type]
  (-> (search-tweets :oauth-creds my-creds :params {:q query 
                                                 :count count
                                                 :lang lang
                                                 :result-type result-type})
      (get-in [:body :statuses])))

(defn filter-tweets 
  "Given a list of tweets, it will return the best candidate tweet, if one exists"
  [results query]
  (let [expr (re-pattern (str " " query " "))]
    (->> results 
         ; filters out all results with more than one number in tweet
         (filter #(= 1 (count (re-seq #"\d+" (get % :text ""))))) 
         ; filters out all results that don't contain query number with whitespace on each side
         (filter #(= 1 (count (re-seq expr (get % :text "")))))
         ; sort tweets by shortest tweet length
         (sort-by #(count (get % :text)))
         ; sort tweets by fewest line breaks 
         (sort-by #(count (re-seq #"\n" (get % :text ""))))
         ))
  )

(defn get-max-id 
  "Returns maximum tweet :id given output from search."
  [results]
  (reduce #(max % (:id %2)) 0 results))

(defn get-candidate 
  "Returns tweet that best fits tweet criteria. 
  Searches for candidate tweet starting from earliest available since-id.
  If no candidate found, searches again with since-id set to the max tweet id 
  found in search results. Function is blocked from hitting search API more 
  than once every 20 seconds"
  [query]
  (loop [since-id initial-since-id search-count 0]
    (let [results (search query results-per-search lang since-id result-type) 
          max-id (get-max-id results)
          tweets (filter-tweets results query)
          block (future (Thread/sleep time-between-searches))] 
      (if (do @block (seq tweets)) ; blocks function from hitting API until after 20 seconds
        (first tweets)
        (do (print "no result for search # " search-count "\n") 
            (recur max-id (inc search-count)))))
    ))
