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
    (j/call :filter (fn [tracks] (not (empty? tracks)))))
  ;(merge-notes #js [])
  ;(j/update-in! [:notes] addSynthAndSort)
  ;(j/update-in! [:notes] (fn [notes] (j/call notes :map addSynthF)))
  ;(j/update-in! [:notes] (fn [notes] (j/call notes :sort note-duration)))
  )

(defn with-midi-track [midi-src callback]
  "https://github.com/Tonejs/Midi"
  (.then (js/Promise.resolve (js/Midi.fromUrl midi-src))
         (fn [midi-js] (callback (parse-midi midi-js))))
  )

(defn with-fixed-delay [f] (js/setTimeout f 4250))
(defn srcF [f el] (f (dommy/attr el "src")))

(defn play [el]
  (js/console.log (j/call js/MIDIjs :get_audio_status))
  ;(j/assoc! js/MIDIjs :player_callback (fn [e] (js/console.log e)))
  ; https://www.midijs.net/midijs_api.html
  ; (with-fixed-delay #(srcF js/MIDIjs.play el))
  (srcF js/MIDIjs.play el)
  (sketch/start (first (get @midi-player-atom :notes)))     ; FIXME: user-selectable notes (rename to tracks)
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
    ;(sketch/exit)
    ;(srcF js/MIDIjs.stop el)
    (sketch/exit)
    (srcF js/MIDIjs.stop el)
    (update-player-next "Play")))

(defn on-midi-loaded [midi-src]
  "https://github.com/prasincs/web-audio-project/blob/master/src-cljs/web_audio_project/client.cljs"
  (with-midi-track midi-src (fn [midi-notes]
                              (swap-vals! midi-player-atom assoc :midi-src midi-src :notes midi-notes)
                              (js/console.log (get @midi-player-atom :notes))
                              ))
  )