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

(def sketch-play-toggle-btn-label (r/atom "Play"))
(def midijs-play-toggle-btn-label (r/atom "Play"))
(defn get-sketch-canvas-element [] (sel1 :#sketch))

(defn getSketchAudioElement [] (sel1 :#sketch-audio-track))
(defn getMidijsAudioElement [] (sel1 :#midijs-audio-track))

(defn getSource []
  "https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API#Loading_sound"
  (. context (createMediaElementSource (getSketchAudioElement)))
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

(defn to-frequency [midi-number]
  "Convert a midi number to frequency. See https://medium.com/swinginc/playing-with-midi-in-javascript-b6999f2913c3"
  "f = (2^(m-69)/12) * 440 Hz"
  (let [exp (/ (- midi-number 69) 12)] (* (Math/pow 2 exp) 440))
  )

(defn ping [frequency]
  (bach/connect->
    (bach/triangle frequency)
    (bach/percussive 0.01 0.4)
    (bach/gain 0.1))
  )

(defn play! [synth duration]
  (bach/run-with synth context (bach/current-time context) duration)
  )

(defn play-midi-note-f [midi-number duration]
  (let [synth (-> (ping (to-frequency midi-number)) (bach/connect-> bach/destination))]
    (fn [] (play! synth duration)))
  )

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

(defn parse-midi [midi-js]
  "Return the main track (i.e. at channel 1) of the midi-js"
  (js/console.log "parse-midi")
  (js/console.log midi-js)
  (->
    (j/get midi-js :tracks)
    ; TODO: User should pick channel from available (i.e. with notes)
    (j/call :find (fn [track] (= 0 (j/get track :channel))))
    (j/update-in! [:notes] (fn [notes] (j/call notes :map addSynthF)))
    )
  )

(defn with-midi-track [callback]
  "https://github.com/Tonejs/Midi"
  (.then (js/Promise.resolve (js/Midi.fromUrl (. (getSketchAudioElement) -src)))
         (fn [midi-js] (callback (parse-midi midi-js))))
  )
  ;(js/Midi.fromUrl (. (getSketchAudioElement) -src)
  ;                     (fn [midi-js] (callback (parse-midi midi-js)))))

; https://www.npmjs.com/package/midiconvert
(defn with-midi-track-legacy [callback]
  "https://www.midijs.net/midijs_api.html"
  (js/MidiConvert.load (. (getSketchAudioElement) -src)
                       (fn [midi-js] (callback (parse-midi midi-js)))))

(defn with-fixed-delay [f] (js/setTimeout f 4000))
(defn srcF [f el] (f (dommy/attr el "src")))

(defn play [el player-type atom-label]
  (dommy/set-attr! el :status "playing")
  (with-midi-track (fn [midi-track]
                     ;(sketch/start midi-track)
                     ;(with-fixed-delay #(js/MIDIjs.play (dommy/attr el "src")))
                     (cond (= player-type "sketch") (sketch/start midi-track) :else (srcF js/MIDIjs.play el))
                     ))
  (swap! atom-label (fn [] "Pause"))
  )

(defn pause [el player-type atom-label]
  (dommy/set-attr! el :status "paused")
  (cond (= player-type "sketch") (sketch/toggle) :else (srcF js/MIDIjs.pause el))
  (swap! atom-label (fn [] "Resume"))
  )

(defn resume [el player-type atom-label]
  (dommy/set-attr! el :status "playing")
  (cond (= player-type "sketch") (sketch/toggle) :else (srcF js/MIDIjs.resume el))
  (swap! atom-label (fn [] "Pause"))
  )

(defn get-audio-element-data [player-type]
  (cond
    (= player-type "sketch") {:element (getSketchAudioElement) :atom-label sketch-play-toggle-btn-label}
    :else {:element (getMidijsAudioElement) :atom-label midijs-play-toggle-btn-label}
    )
  )

(defn stop [player-type]
  (let [
        data (get-audio-element-data player-type)
        el (get data :element)
        atom-player-label (get data :atom-label)
        ]
    (dommy/set-attr! (get data :element) :status "stopped")
    ;(sketch/exit)
    ;(srcF js/MIDIjs.stop el)
    (cond (= player-type "sketch") (sketch/exit) :else (srcF js/MIDIjs.stop el))
    (swap! atom-player-label (fn [] "Play"))
    )
  )

(defn on-player-btn-click [player-type]
  "https://www.midijs.net/midijs_api.html"
  (let [
        el-data (get-audio-element-data player-type)
        el (get el-data :element)
        atom-player-label (get el-data :atom-label)
        ]
    (match [(dommy/attr el :status)]
           ["stopped"] (play el player-type atom-player-label)
           ["playing"] (pause el player-type atom-player-label)
           ["paused"] (resume el player-type atom-player-label)
           )
    )
  )

(defn on-sketch-play-btn-click [] (on-player-btn-click "sketch"))
(defn on-midijs-play-btn-click [] (on-player-btn-click "midijs"))

(defn on-sketch-stop-btn-click [] (stop "sketch"))
(defn on-midijs-stop-btn-click [] (stop "midijs"))

(defn on-midi-loaded []
  "https://github.com/prasincs/web-audio-project/blob/master/src-cljs/web_audio_project/client.cljs"
  (js/console.log "on_midi_loaded")
  ; https://www.midijs.net/midijs_api.html
  (with-midi-track js/console.log)
  )