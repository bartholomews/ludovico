(ns ludovico.server
  (:require
    [ring.adapter.jetty :as jetty]
    [ring.middleware.file :as ring-file]
    [ring.middleware.file-info :as ring-file-info])
  )

(defn my-handler [req]
  (tap> req)                                                ;; http://localhost:9630/inspect
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Hello World!"})


(def handler
  (-> my-handler
      (ring-file/wrap-file "public")
      (ring-file-info/wrap-file-info)))

(defn -main [& args]
  (jetty/run-jetty handler {:port 3000}))
