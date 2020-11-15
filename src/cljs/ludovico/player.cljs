(ns ludovico.player
  (:require
    [applied-science.js-interop :as j]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :as dommy :refer-macros [sel sel1]]
    [ludovico.sketch :as sketch]
    [reagent.core :as r]
    ))

(def midi-player-atom (r/atom {:next "Play" :midi-src ""}))

(defn update-player-next [state] (swap-vals! midi-player-atom assoc :next state))

(defn get-sketch-canvas-element [] (sel1 :#sketch))
(defn get-audio-element [] (sel1 :#midi-track))

(defn merge-notes [arrays acc]
  (cond
    (empty? arrays) #js {:notes acc}
    :else (merge-notes (rest arrays) (j/call acc :concat (first arrays)))
    )
  )

(defn parse-midi [midi-js]
  "Return the main track (i.e. at channel 1) of the midi-js"
  (js/console.log "parse-midi")
  (js/console.log midi-js)
  (->
    (j/get midi-js :tracks)
    ; TODO: User should pick channel from available (i.e. with notes / instrument number etc)
    ;(j/call :find (fn [track] (= 1 (j/get track :channel))))
    (j/call :map (fn [track] (j/get track :notes)))
    (merge-notes #js [])
    ;(j/update-in! [:notes] addSynthAndSort)
    ;(j/update-in! [:notes] (fn [notes] (j/call notes :map addSynthF)))
    ;(j/update-in! [:notes] (fn [notes] (j/call notes :sort note-duration)))
    )
  )

(defn with-midi-track [callback]
  "https://github.com/Tonejs/Midi"
  (.then (js/Promise.resolve (js/Midi.fromUrl (. (get-audio-element) -src)))
         (fn [midi-js] (callback (parse-midi midi-js))))
  )

(defn with-fixed-delay [f] (js/setTimeout f 4250))
(defn srcF [f el] (f (dommy/attr el "src")))

(defn play [el]
  (with-midi-track (fn [midi-track]
                     ;(sketch/start midi-track)
                     ;(with-fixed-delay #(js/MIDIjs.play (dommy/attr el "src")))
                     (with-fixed-delay #(srcF js/MIDIjs.play el))
                     (sketch/start midi-track)
                     (update-player-next "Pause")
                     ))
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
    ;(sketch/exit)
    ;(srcF js/MIDIjs.stop el)
    (sketch/exit)
    (srcF js/MIDIjs.stop el)
    (update-player-next "Play")))

(defn on-midi-loaded []
  "https://github.com/prasincs/web-audio-project/blob/master/src-cljs/web_audio_project/client.cljs"
  ;(js/console.log "on_midi_loaded")
  ; https://www.midijs.net/midijs_api.html
  ;(with-midi-track js/console.log)
  )