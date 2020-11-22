(ns ludovico.interop
  (:require
    [applied-science.js-interop :as j]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :refer-macros [sel sel1]]
    [ludovico.midi :as midi]
    [ludovico.synth :as synth]
    ))

; https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/all
(defn when-all-resolved [promises actionF]
  (let [promise (j/call js/Promise :all promises)]
    (j/call promise :then actionF)
    )
  )

(defn when-resolved [promiseF actionF]
  (let [promise (j/call js/Promise :resolve promiseF)]
    (j/call promise :then actionF)
    )
  )

(defn get-instrument [instrument] (j/call js/Soundfont :instrument synth/context instrument))

(defn get-midi-src [midi-src] (j/call js/Midi :fromUrl midi-src))