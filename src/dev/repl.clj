(ns repl
  (:require 
    [shadow.cljs.devtools.api :as shadow] 
    [ludovico.server :as srv] 
    [ring.adapter.jetty :as jetty]
    ))

(defonce jetty-ref (atom nil))

;; https://github.com/thheller/shadow-cljs/blob/master/src/dev/repl.clj
(defn start []
  (shadow/watch :frontend)

  (reset! jetty-ref
          (jetty/run-jetty #'srv/handler
                           {:port 18081
                            :join? false}))
  ::started)

(defn stop []
  (when-some [jetty @jetty-ref]
    (reset! jetty-ref nil)
    (.stop jetty))
  ::stopped)

(defn restart []
  (stop)
  (start))