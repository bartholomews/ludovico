(ns ludovico.frontend.interop
  (:require
    ; https://github.com/danigb/soundfont-player
    ; ["soundfont-player" :as soundfont]
    ; https://github.com/Tonejs/Midi
    ["@tonejs/midi" :refer [Midi]]
    [applied-science.js-interop :as j]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :refer-macros [sel sel1]]
    [cljsjs.moment]
    ))

(println Midi)
(println (Midi))

(defn with-fixed-delay [delay-ms f] (js/setTimeout f delay-ms))

; https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/all
(defn when-all-resolved [promises actionF]
  (let [promise (j/call js/Promise :all promises)]
    (j/call promise :then actionF)
    )
  )

(defn when-resolved [promiseF actionF]
  (let [promise (j/call "js/Promise" :resolve promiseF)]
    (j/call promise :then actionF)
    )
  )

;(js/console.log midi-parser-js)

(defn hello-moment []
  (println (str "Hello there it's "
                (.format (js/moment) "dddd")))
  )

;; (defn get-instrument [context instrument] (j/call soundfont :instrument context instrument))

(defn get-midi-src [midi-src] (j/call Midi :fromUrl midi-src))