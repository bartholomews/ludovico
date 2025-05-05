(ns ludovico.frontend.interop
  (:require
    [applied-science.js-interop :as j]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :refer-macros [sel sel1]]
    [cljsjs.moment]
    ))

(defn with-fixed-delay [delay-ms f] (js/setTimeout f delay-ms))

; https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/all
(defn when-all-resolved [promises actionF]
  (let [promise (j/call js/Promise :all promises)]
    (j/call promise :then actionF)
    )
  )

;(defn when-resolved [promiseF actionF]
;  (.then promiseF actionF)
;  ;(.then (js/Promise.resolve promiseF)
;  ;       (fn [res] (actionF res))
;  ;  )
;  )

; (js/console.log midi-parser-js)

(defn hello-moment []
  (println (str "Hello there it's "
                (.format (js/moment) "dddd")))
  )
