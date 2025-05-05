(ns ludovico.frontend.player
  (:require
    [applied-science.js-interop :as j]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :as dommy :refer-macros [sel sel1]]
    [ludovico.frontend.interop :as in]
    [ludovico.frontend.midi :as midi]
    [ludovico.frontend.sketch :as sketch]
    [reagent.core :as r]
    ; https://github.com/danigb/smplr
    ["smplr" :refer [Soundfont]]
    ))

(js/console.log "Soundfont:")
(js/console.log Soundfont)

; https://github.com/ctford/cljs-bach
; https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API
; (defonce context (bach/audio-context))
(defonce context (js/AudioContext.))

; https://github.com/danigb/soundfont-player
; https://github.com/danigb/smplr?tab=readme-ov-file#play
(defn play-soundfont! [player midi-number current-time duration]
  "Play a note with Soundfont (via smplr)"
  ; convert the map to a JS object (clj->js)
  (j/call player :start #js {:note midi-number :time current-time :duration duration})
  )

; https://github.com/danigb/smplr?tab=readme-ov-file#play
(defn test-smplr! []
  (let [
        test-player (Soundfont. context #js {:instrument "marimba"})
        current-time (.-currentTime context)
        duration 3
        ]
    (fn []
      (js/console.log context)
      (js/console.log current-time)
      (js/console.log test-player)
      (play-soundfont! test-player "C4" current-time duration)
      )
    )
  )

;; TODO do a :src :track object with :notes inside
(def midi-player-atom (r/atom {:next "Play" :midi-src "" :tracks []}))

(defn update-player-label-next [state] (swap-vals! midi-player-atom assoc :next state))

(defn addSynthF [player note]
  (let [
        midi-note (j/get note :midi)
        duration (j/get note :duration)
        ]
    (js/console.log midi-note)
    (j/assoc! note :synthF
              (fn [context] (play-soundfont! player midi-note (j/get context :currentTime) duration))
              )
    )
  )

(defn parse-midi-track [track]
  (let [
        midi-instrument-number (j/get-in track [:instrument :number])
        ; https://github.com/danigb/soundfont-player/blob/master/INSTRUMENTS.md
        track-instrument (get midi/instruments midi-instrument-number)
        soundfont-player (Soundfont. context #js {:instrument track-instrument})
        update-notes (fn [notes] (j/call notes :map #(addSynthF soundfont-player %)))
        ]
    (js/console.log track-instrument)
    (j/update! track :notes update-notes)
    )
  )

(defn parse-midi-tracks [midi-js]
  "Return the main track (i.e. at channel 1) of the midi-js"
  (js/console.log "Parsing midi tracks:")
  (js/console.log midi-js)
  (->
    (j/get midi-js :tracks)
    (j/call :filter (fn [tracks] (not (empty? (j/get tracks :notes)))))
    (j/call :map parse-midi-track)
    )
  )

(defn update-midi-player-atom [midi-src midi-tracks]
  (swap-vals! midi-player-atom assoc :midi-src midi-src :tracks midi-tracks)
  (js/console.log "Loaded midi tracks:")
  (js/console.log midi-tracks)
  ; (callback midi-tracks)
  ) 

(defn with-midi-tracks [midi-src]
  "https://github.com/Tonejs/Midi"
  (.then
    (.then (in/get-midi-src midi-src)
           parse-midi-tracks
           ; (fn [res] (update-midi-player-atom midi-src res callback)
           ;)
           )
    (fn [res] 
      ; (js/console.log res)
      (update-midi-player-atom midi-src res)
      res
      )
    )
  
  ;(in/when-resolved (in/get-midi-src midi-src)
  ;                  (fn [res] (js/console.log res))
                    ;(fn [midi-js] (in/when-all-resolved (parse-midi-tracks midi-js)
                    ;                                    (fn [res] (update-midi-player-atom midi-src res callback))
                    ;                                    )))
                    ;  )
)

(defn srcF [f el] (f (dommy/attr el "src")))

(defn play []
  (let [first-track (first (get @midi-player-atom :tracks))]
    ; (js/console.log first-track)
    ;(synth/schedule-soundfont! (j/get-in first-track [:instrument :soundfont]))
    (sketch/start first-track)
    ; (print "TODO: START SKETCH - p5js issues with quil?")
    (update-player-label-next "Pause")
    ))

  (defn pause []
    (sketch/toggle)
    (update-player-label-next "Resume")
    )

  (defn resume []
    (sketch/toggle)
    (update-player-label-next "Pause")
    )

  (defn on-play-btn-click []
    "https://www.midijs.net/midijs_api.html"
    (match [(get @midi-player-atom :next)]
           ["Play"] (play)
           ["Pause"] (pause)
           ["Resume"] (resume)
           )
    )

  (defn on-stop-btn-click []
    (sketch/exit)
    (update-player-label-next "Play"))

  (defn on-midi-loaded [midi-src]
    "https://github.com/prasincs/web-audio-project/blob/master/src-cljs/web_audio_project/client.cljs"
    (with-midi-tracks midi-src)
    )
  