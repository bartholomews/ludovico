(ns ludovico.player
  (:require
    [applied-science.js-interop :as j]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :as dommy :refer-macros [sel sel1]]
    [ludovico.interop :as in]
    [ludovico.midi :as midi]
    [ludovico.sketch :as sketch]
    [ludovico.synth :as synth]
    [reagent.core :as r]
    ))

; TODO do a :src :track object with :notes inside
(def midi-player-atom (r/atom {:next "Play" :midi-src "" :tracks []}))

(defn update-player-label-next [state] (swap-vals! midi-player-atom assoc :next state))

(defn get-sketch-canvas-element [] (sel1 :#sketch))
(defn get-audio-element [] (sel1 :#midi-track))

; https://github.com/danigb/soundfont-player
(defn play-soundfont []
  (in/when-resolved (in/get-instrument "applause")
                    (fn [instrument]
                      (swap-vals! midi-player-atom assoc :instrument instrument)
                      (j/call instrument :play "C4")
                      ))
  )

(defn addSynthF [note instrument]
  (let [
        midi-note (j/get note :midi)
        duration (j/get note :duration)
        ]
    (j/assoc! note :synthF
              (fn [] (synth/play-bach! midi-note duration))
              ;(fn [context] (synth/play-soundfont! instrument midi-note (j/get context :currentTime) duration))
              )
    )
  )

(defn with-synth-f [instrument] (fn [notes] (j/call notes :map (fn [note] (addSynthF note instrument)))))

(defn parse-midi-track [track]
  (let [
        midi-instrument-number (j/get-in track [:instrument :number])
        track-instrument (get midi/instruments midi-instrument-number)]
    (in/when-resolved (in/get-instrument track-instrument)
                      (fn [instrument] (j/update! track :notes (with-synth-f instrument))))
    )
  )

(defn parse-midi-tracks [midi-js]
  "Return the main track (i.e. at channel 1) of the midi-js"
  (js/console.log "parse-midi")
  (js/console.log midi-js)
  (->
    (j/get midi-js :tracks)
    (j/call :filter (fn [tracks] (not (empty? (j/get tracks :notes)))))
    ; TODO: User should pick channel from available (i.e. with notes / instrument number etc)
    ; https://github.com/danigb/soundfont-player/blob/master/INSTRUMENTS.md
    ;(in/when-resolved (in/get-instrument "acoustic_grand_piano"))
    (j/call :map parse-midi-track)
    )
  )

(defn update-midi-player-atom [midi-src midi-tracks callback]
  (swap-vals! midi-player-atom assoc :midi-src midi-src :tracks midi-tracks)
  (callback midi-tracks)
  )

(defn with-midi-tracks [midi-src callback]
  "https://github.com/Tonejs/Midi"
  (in/when-resolved (in/get-midi-src midi-src)
                    (fn [midi-js] (in/when-all-resolved (parse-midi-tracks midi-js)
                                                        (fn [res] (update-midi-player-atom midi-src res callback))
                                                        )))
  )

(defn with-fixed-delay [f] (js/setTimeout f 4250))
(defn srcF [f el] (f (dommy/attr el "src")))

(defn play [el]
  (let [first-track (first (get @midi-player-atom :tracks))]
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; https://www.midijs.net/midijs_api.html
    (js/console.log (j/call js/MIDIjs :get_audio_status))
    ;(with-fixed-delay #(srcF js/MIDIjs.play el))
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    (js/console.log first-track)
    (synth/schedule-soundfont! (j/get-in first-track [:instrument :soundfont]))
                        ; (sketch/start first-track)    ; FIXME: user-selectable notes (rename to tracks)
                        (update-player-label-next "Pause")
    )
  )
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn pause [el]
  (sketch/toggle)
  (srcF js/MIDIjs.pause el)
  (update-player-label-next "Resume")
  )

(defn resume [el]
  (sketch/toggle)
  (srcF js/MIDIjs.resume el)
  (update-player-label-next "Pause")
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
    (update-player-label-next "Play")))

(defn current-time []
  (js/console.log synth/context)
  (j/get synth/context :currentTime)
  )

(defn on-midi-loaded [midi-src]
  "https://github.com/prasincs/web-audio-project/blob/master/src-cljs/web_audio_project/client.cljs"
  (with-midi-tracks midi-src js/console.log)
  )