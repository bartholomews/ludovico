(ns ludovico.player
  (:require
    [applied-science.js-interop :as j]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :as dommy :refer-macros [sel sel1]]
    [ludovico.sketch :as sketch]
    [reagent.core :as r]
    ))

; TODO do a :src :track object with :notes inside
(def midi-player-atom (r/atom {:next "Play" :midi-src "" :notes nil}))

(defn update-player-next [state] (swap-vals! midi-player-atom assoc :next state))

(defn get-sketch-canvas-element [] (sel1 :#sketch))
(defn get-audio-element [] (sel1 :#midi-track))

(defn with-synth-f [notes] (j/call notes :map (fn [note] (synth/addSynthF note))))

(defn parse-midi [midi-js]
  "Return the main track (i.e. at channel 1) of the midi-js"
  (js/console.log "parse-midi")
  (js/console.log midi-js)
  (->
    (j/get midi-js :tracks)
    (j/call :filter (fn [tracks] (not (empty? (j/get tracks :notes)))))
    ; TODO: User should pick channel from available (i.e. with notes / instrument number etc)
    (j/call :map (fn [track] (j/update! track :notes with-synth-f)))
    )
  )

(defn with-midi-track [midi-src callback]
  "https://github.com/Tonejs/Midi"
  (.then (js/Promise.resolve (js/Midi.fromUrl midi-src))
         (fn [midi-js] (callback (parse-midi midi-js))))
  )

(defn with-fixed-delay [f] (js/setTimeout f 4250))
(defn srcF [f el] (f (dommy/attr el "src")))

(defn play [el]
  ; https://www.midijs.net/midijs_api.html
  ;(with-fixed-delay #(srcF js/MIDIjs.play el))
  (sketch/start (first (get @midi-player-atom :tracks)))    ; FIXME: user-selectable notes (rename to tracks)
  (update-player-next "Pause")
  )

(defn pause [el]
  (sketch/toggle)
  (srcF js/MIDIjs.pause el)
  (update-player-next "Resume")
  )

(defn resume [el]
  (sketch/toggle)
  (srcF js/MIDIjs.resume el)
  (update-player-next "Pause")
  )

(defn on-play-btn-click []
  "https://www.midijs.net/midijs_api.html"
  (let [el (get-audio-element)]
    (match [(get @midi-player-atom :next)]
           ["Play"] (play el)
           ["Pause"] (pause el)
           ["Resume"] (resume el)
           )
    )
  )

(defn on-stop-btn-click []
  (let [el (get-audio-element)]
    (sketch/exit)
    (srcF js/MIDIjs.stop el)
    (update-player-next "Play")))

(defn update-midi-player-atom [midi-src midi-tracks]
  (swap-vals! midi-player-atom assoc :midi-src midi-src :tracks midi-tracks)
  )

; TODO http://grimmdude.com/MidiPlayerJS/
(defn on-midi-loaded [midi-src]
  "https://github.com/danigb/soundfont-player"
  (.then (js/Promise.resolve (j/call js/Soundfont :instrument synth/context "clavinet"))
         (fn [instr]
           (
            (swap-vals! midi-player-atom assoc :instrument instr)
            (with-midi-track midi-src (update-midi-player-atom midi-src midi-tracks))
            )
           )
         )
  )