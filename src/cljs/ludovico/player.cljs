(ns ludovico.player
  (:require
    [applied-science.js-interop :as j]
    [cljs-bach.synthesis :as bach]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :as dommy :refer-macros [sel sel1]]
    [ludovico.sketch :as sketch]
    [reagent.core :as r]
    ))

; https://github.com/ctford/cljs-bach
; https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API
(defonce context (bach/audio-context))

(def play-toggle-btn-label (r/atom "Play"))
(defn get-sketch-canvas-element [] (sel1 :#sketch))

(defn getAudioElement [] (sel1 :#audio-track))

(defn getSource []
  "https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API#Loading_sound"
  (. context (createMediaElementSource (getAudioElement)))
  )

(defn get-destination []
  "https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API#Controlling_sound"
  ;(bach/destination context nil nil) ; bach sink node with :output prop only: could be useful when using both synth and files
  (j/get context :destination)
  )

;(defn getConnection []
;  "TODO"
;  (. (getSource) (bach/connect (destination)))
;  )

(defn ping [freq]
  (bach/connect->
    (bach/square freq)         ; Try a sawtooth wave.
    (bach/percussive 0.01 0.4) ; Try varying the attack and decay.
    (bach/gain 0.1)))          ; Try a bigger gain.

(defn play-midi-note []
  (js/console.log "SOURCE:")
  (js/console.log (getSource))
  ;(js/console.log "DESTINATION:")
  ;(js/console.log (destination))
  ;(js/console.log "CONNECTION:")
  ;(js/console.log (getConnection))
  (-> (ping 440)
      (bach/connect-> bach/destination)
      (bach/run-with context (bach/current-time context) 1.0)))

; https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API#Controlling_sound
;(defn web-api-handler [evt]
;  (js/console.log context)
;  ; check if context is in suspended state (autoplay policy)
;  (if (= (. context -state) "suspended") (. context resume))
;  ;(if (= () false))
;  (. (getAudioElement) play)
;  )

(defn parse-midi [midi-js]
  "Return the main track (i.e. at channel 1) of the midi-js"
  (let [tracks (j/get midi-js :tracks)]
    (j/call tracks :find (fn [track] (= 1 (j/get track :channelNumber))))
    )
  )

(defn with-midi-track [callback]
  "https://www.midijs.net/midijs_api.html"
  (js/MidiConvert.load (. (getAudioElement) -src)
                       (fn [midi-js] (callback (parse-midi midi-js)))))

(defn with-fixed-delay [f] (js/setTimeout f 5000))

(defn srcF [f el] (f (dommy/attr el "src")))

(defn play [el]
    (with-fixed-delay #(js/MIDIjs.play (dommy/attr el "src")))
    ;(js/setTimeout (js/MIDIjs.play (dommy/attr el "src")) 50000)
    ; #(with-delay (js/MIDIjs.play (dommy/attr el "src")))
    (with-midi-track (fn [midi-track] (sketch/start midi-track)))
    (dommy/set-attr! el :status "playing")
    (swap! play-toggle-btn-label (fn [] "Pause"))
  )

(defn pause [el]
    (dommy/set-attr! el :status "paused")
    (srcF js/MIDIjs.pause el)
    (sketch/toggle)
    (swap! play-toggle-btn-label (fn [] "Play"))
  )

(defn resume [el]
    (dommy/set-attr! el :status "playing")
    (srcF js/MIDIjs.resume el)
    (sketch/toggle)
    (swap! play-toggle-btn-label (fn [] "Pause"))
  )

(defn stop [el]
    (dommy/set-attr! el :status "stopped")
    (srcF js/MIDIjs.stop el)
    (sketch/exit)
    (swap! play-toggle-btn-label (fn [] "Play"))
  )

(defn on-player-btn-click []
  "https://www.midijs.net/midijs_api.html"
  (let [el (getAudioElement)]
    (match [(dommy/attr el :status)]
           ["stopped"] (play el)
           ["playing"] (pause el)
           ["paused"] (resume el)
           )
    )
  )

(defn on-midi-loaded []
  "https://github.com/prasincs/web-audio-project/blob/master/src-cljs/web_audio_project/client.cljs"
  (js/console.log "on_midi_loaded")
  ; https://www.midijs.net/midijs_api.html
  (js/console.log (js/MIDIjs.get_audio_status))
  (with-midi-track js/console.log)
  )