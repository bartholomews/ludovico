(ns ludovico.synth
  (:require
    [applied-science.js-interop :as j]
    [cljs-bach.synthesis :as bach]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :as dommy :refer-macros [sel sel1]]
    [ludovico.sketch :as sketch]
    [ludovico.player :as player]
    ))

; https://github.com/ctford/cljs-bach
; https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API
(defonce context (bach/audio-context))

;(defn getSource []
;  "https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API#Loading_sound"
;  (. context (createMediaElementSource (player/getAudioElement)))
;  )

(defn get-destination []
  "https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API#Controlling_sound"
  ;(bach/destination context nil nil) ; bach sink node with :output prop only: could be useful when using both synth and files
  (j/get context :destination)
  )

(defn to-frequency [midi-number]
  "Convert a midi number to frequency. See https://medium.com/swinginc/playing-with-midi-in-javascript-b6999f2913c3"
  "f = (2^(m-69)/12) * 440 Hz"
  (let [exp (/ (- midi-number 69) 12)] (* (Math/pow 2 exp) 440))
  )

(defn play! [synth duration]
  (bach/run-with synth context (bach/current-time context) duration)
  )

(defn note-duration [note-1 note-2]
  (< (j/get note-1 :duration)(j/get note-2 :duration))
  )

(defn ping [frequency]
  (bach/connect->
    (bach/triangle frequency)
    (bach/percussive 0.01 0.4)
    (bach/gain 0.1))
  )

(defn play-midi-note-f [midi-number duration]
  (let [synth (-> (ping (to-frequency midi-number)) (bach/connect-> bach/destination))]
    (fn [] (play! synth duration)))
  )

;(defn getConnection []
;  "TODO"
;  (. (getSource) (bach/connect (destination)))
;  )
(defn leipzig-wat []
  [{:time     0
    :pitch    67
    :duration 2
    :part     :melody}
   {:time     2
    :pitch    71
    :duration 2
    :part     :melody}]
  )

;(defn synth [note]
;  (bach/connect->
;    (bach/add (bach/square (* 1.01 (:pitch note))) (bach/sawtooth (:pitch note)))
;    (bach/low-pass 600)
;    (bach/adsr 0.001 0.4 0.5 0.1)
;    (bach/gain 0.15)))

;(def melody
;  ; The durations and pitches of the notes. Try changing them.
;  (->> (bach/phrase [1 1/2 1/2 1 1 2 2] [0 1 0 2 -3 1 -1])
;       (bach/all :instrument synth))) ; Here we choose the instrument.

;(defn ping2 [note]
;  (bach/connect->
;    (bach/triangle (:pitch note))                           ; Try a sawtooth wave.
;    (bach/percussive 0.01 0.4)                              ; Try varying the attack and decay.
;    (bach/gain 0.1))
;  )

; https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API#Controlling_sound
;(defn web-api-handler [evt]
;  (js/console.log context)
;  ; check if context is in suspended state (autoplay policy)
;  (if (= (. context -state) "suspended") (. context resume))
;  ;(if (= () false))
;  (. (getAudioElement) play)
;  )

(defn addSynthF [note]
  (let [
        midi-note (j/get note :midi)
        duration (j/get note :duration)
        ]
    (j/assoc! note :synthF (play-midi-note-f midi-note duration))
    )
  )