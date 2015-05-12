(ns twit-finity.core
  (:gen-class)
  (:require
    [twit-finity.search :as search]))

(def init-num 2)
  
(defn -main
  "main"
  [& args] 
  (dorun
    (map #(println "\n\n" (:text (search/get-candidate %))) (drop init-num (range)))
    )
  )
