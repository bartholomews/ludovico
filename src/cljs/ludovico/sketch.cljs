(ns ludovico.sketch
  (:refer-clojure :exclude [update])
  (:require
    [applied-science.js-interop :as j]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :refer-macros [sel1]]
    [ludovico.player :as player]
    [quil.core :as q :include-macros true]
    [quil.middleware :as m]
    )
  )

; https://www.reddit.com/r/Clojure/comments/afazxb/repl_workflow_in_quil/
(def paused (atom false))

; tile width = canvas width / 88
(def tile-width 10)
(defn tile-height [note-duration-ms] (* 50 note-duration-ms))

; https://syntheway.com/MIDI_Keyboards_Middle_C_MIDI_Note_Number_60_C4.htm
(defn tile-x [midi-key]
  "The x-position of the note tile, supporting 88 keys from 21 (A0) to 108 (C8)"
  (* tile-width (- midi-key 21))
  )

(defn getSketch [] (q/get-sketch-by-id "sketch"))

(defn toggleSketch [sketch]
  (q/with-sketch sketch
                 (cond
                   (= @paused true) (q/no-loop)
                   :else (q/start-loop))
                 )
  )

(defn exit []
  (js/console.log "SKETCH EXIT")
  (q/with-sketch (getSketch)
                 (q/exit)
                 ; (q/start-loop)
                 )
  )

(defn toggle []
  (swap! paused not)
  (cond
    (nil? (getSketch)) (js/console.error "Attempting to start sketch before being initialised")
    :else (toggleSketch (getSketch))
    )
  )

(defn get-elapsed-time [state]
  (/ (- (q/millis) (get state :start)) 1000)
  )

(defn note-distance [note state]
  "The distance in millis of note from elapsed time (i.e. how long until it should be played).
   If negative, it means that the note has already been played."
  (let [
        elapsed-time (get-elapsed-time state)
        note-time (j/get note :time)
        ]
    (cond
      (< elapsed-time 0) (- note-time elapsed-time)
      :else (- note-time elapsed-time)
      )
    )
  )

(defn display-note-rect [note state]
  (let [
        distance (note-distance note state)
        percentage (* (/ distance 5.0) 100)
        pitch-midi-number (j/get note :midi)
        reverse-percentage (- 100 percentage)
        tile-y (* 500 (/ reverse-percentage 100))
        note-duration-ms (j/get note :duration)
        ]
    (q/rect (tile-x pitch-midi-number) tile-y tile-width (tile-height note-duration-ms))
    )
  )

(defn should-display-note [note state]
  "Return true if note rect should be be in canvas, false otherwise.
   A note which is not already played and less than 5s away from current-time should be displayed in canvas."
  (let [distance (note-distance note state)] (and (> distance -1) (< distance 5)))
  )

(defn setup [frame-rate fixed-delay midi-track]
  (fn []
    (js/console.log "Starting sketch")
    (js/console.log midi-track)
    (q/frame-rate frame-rate)
    (q/stroke 0xff3090a1)
    (q/stroke-weight 2)
    (q/fill 0xff7bcecc)
    {:notes (j/get midi-track :notes) :start (+ (q/millis) fixed-delay)}
    )
  )

(defn not-played [note state]
  "Not has not been played yet, should still be evaluated in sketch"
  (let [has-been-played (> (get-elapsed-time state) (j/get note :time))]
    (if (= has-been-played true) (player/play-midi-note (j/get note :midi)))
    (not has-been-played)
    )
  )

(defn update [state]
  (let [
        ; FIXME: `take-while` they are played
        filtered-notes (filter (fn [note] (not-played note state)) (get state :notes))
        new-state (assoc state :notes filtered-notes)
        ]
    new-state
    )
  )

(defn draw [state]
  (let [
        notes (get state :notes)
        notes-to-display (take-while (fn [note] (should-display-note note state)) notes)
        ]
    (q/background 255)
    (q/fill 0)
    ;(q/clear)
    (dorun (map (fn [note] (display-note-rect note state)) notes-to-display))
    ;(q/text (str "Frame rate: " (q/target-frame-rate)) 350 20)
    ;(q/text (str "Frame count: " (/ fps 100)) 350 40)
    ;(q/text (str "Start time: " (get state :start)) 350 40)
    ;(q/text (str "Current time: " current-time) 350 60)
    )
  )

; https://github.com/quil/quil/wiki/ClojureScript
; https://github.com/quil/quil/wiki/Functional-mode-%28fun-mode%29
;; TODO proper delay-frame, non lagging timer
(defn start [midi-track]
  (q/sketch
    :host "sketch"
    ; piano setup: (21 A0) => (108 C8) = 88 cols.
    ; tile width = canvas width / 88
    :size [880 500]
    :setup (setup 240 5000 midi-track)
    :draw draw
    :update update
    :middleware [m/fun-mode]
    )
  )
