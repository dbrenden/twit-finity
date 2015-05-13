(ns twit-finity.auth
  (:require
    [clojure.edn :as edn]))

(defn load-config
  "Parses config file from text-file"
  [filename]
  (-> (slurp filename)
      edn/read-string))

(def credentials (load-config "config.clj"))
