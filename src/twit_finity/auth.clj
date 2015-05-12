(ns twit-finity.auth
  (:require 
    [clojure.java.io :as io]))

(defn load-config 
  "Parses config file from text-file"
  [filename] 
  (with-open [r (io/reader filename)]
    (read (java.io.PushbackReader. r))))

(def credentials (load-config "config.clj"))

