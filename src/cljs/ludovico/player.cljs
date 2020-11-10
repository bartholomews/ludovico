(ns ludovico.player
  (:require
    [cljs-bach.synthesis :as bach]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :as dommy :refer-macros [sel sel1]]
    [goog.object :as g]
    [ludovico.sketch :as sketch]
    ))

; https://github.com/ctford/cljs-bach
; https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API
(defonce context (bach/audio-context))

(defn getAudioElement [] (sel1 :#audio-track))

(defn getSource []
  "https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API#Loading_sound"
  (. context (createMediaElementSource (getAudioElement)))
  )

(defn getDestination []
  "https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API#Controlling_sound"
  ;(bach/destination context nil nil) ; bach sink node with :output prop only: could be useful when using both synth and files
  (.-destination context)
  )

(defn getConnection []
  "TODO"
  (. (getSource) (connect (getDestination)))
  )

(defn debugThings []
  (js/console.log "SOURCE:")
  (js/console.log (getSource))
  (js/console.log "DESTINATION:")
  (js/console.log (getDestination))
  (js/console.log "CONNECTION:")
  (js/console.log (getConnection))
  )

; https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API#Controlling_sound
;(defn web-api-handler [evt]
;  (js/console.log context)
;  ; check if context is in suspended state (autoplay policy)
;  (if (= (. context -state) "suspended") (. context resume))
;  ;(if (= () false))
;  (. (getAudioElement) play)
;  )

(defn srcF [f el] (f (dommy/attr el "src")))

(defn play [el]
  (srcF js/MIDIjs.play el)
  (dommy/set-attr! el :status "playing"))

(defn pause [el]
  (srcF js/MIDIjs.pause el)
  (dommy/set-attr! el :status "paused"))

(defn resume [el]
  (srcF js/MIDIjs.resume el)
  (dommy/set-attr! el :status "playing"))

(defn onPlayerButtonClick []
  "https://www.midijs.net/midijs_api.html"
  (js/console.log "midi-js handler")
  (let [el (getAudioElement)]
    (match [(dommy/attr el :status)]
           ["stopped"] (play el)
           ["playing"] (pause el)
           ["paused"] (resume el)
           )
    )
  )

(defn midi-convert []
  "https://www.midijs.net/midijs_api.html"
  (let [src (. (getAudioElement) -src)]
    (js/console.log src)
    (js/MidiConvert.load src
                         (fn [song]
                           (js/console.log song)
                           ; TODO (analyze song)
                           ))
    )
  )

(defn init [playBtn]
  "https://github.com/prasincs/web-audio-project/blob/master/src-cljs/web_audio_project/client.cljs"
  (js/console.log "player/init")
  ; https://www.midijs.net/midijs_api.html
  (js/console.log (js/MIDIjs.get_audio_status))
  (midi-convert)
  (dommy/listen! playBtn :click onPlayerButtonClick)
  )