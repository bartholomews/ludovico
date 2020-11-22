(ns ludovico.sketch
  (:refer-clojure :exclude [update])
  (:require
    [applied-science.js-interop :as j]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :refer-macros [sel1]]
    [quil.core :as q :include-macros true]
    )
  )

; https://www.reddit.com/r/Clojure/comments/afazxb/repl_workflow_in_quil/
(def paused (atom false))

; tile width = canvas width / 88
(def tile-width 10)

(defn note-height [canvas-height note-duration-sec canvas-duration-sec]
  "note-height : canvas-height = note-duration-sec : canvas-duration-sec"
  (/ (* note-duration-sec canvas-height) canvas-duration-sec))

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

(defn get-elapsed-time []
  "Time (in seconds) since the playback started"
  ;(let
  ;  [time (q/state :player-time)]
  ;  (cond
  ;    (nil? time) (/ (- (q/millis) (q/state :start)) 1000)
  ;    :else time
  ;    )
  ;  )
  (/ (- (q/millis) (q/state :start)) 1000)
  )

(defn note-distance [note]
  "The distance in millis of note from elapsed time (i.e. how long until it should be played).
   If negative, it means that the note has already been played."
  (let [
        elapsed-time (get-elapsed-time)
        note-time (j/get note :time)
        ]
    (cond
      (< elapsed-time 0) (- note-time elapsed-time)
      :else (- note-time elapsed-time)
      )
    )
  )

(defn get-distance-from-note-on [elapsed-time note]
  "The distance in seconds of note from elapsed time (i.e. how long until it should be played).
   If negative, it means that the note has already been played and it's the distance from when it has been played."
  (let [note-time (j/get note :time)]
    (cond
      (< elapsed-time 0) (- note-time elapsed-time)
      :else (- note-time elapsed-time)
      )
    )
  )

(defn get-distance-from-note-off [note elapsed-time]
  (+ (get-distance-from-note-on elapsed-time note) (j/get note :duration))
  )

(defn play-midi-note [note] (j/call note :synthF))

(defn landing-in-sec [canvas-height current-height]
  "Time in seconds to reach the bottom (i.e. canvas-height)"
  (/ (- canvas-height current-height) (q/target-frame-rate)))

(defn height-for-distance [canvas-height sec]
  "Height value of a point in order to be at `sec` to reach the bottom (i.e. canvas-height)"
  (- canvas-height (* sec (q/target-frame-rate)))
  )

(defn display-note-rect [note]
  (let [
        elapsed-time (get-elapsed-time)
        distance-from-note-on-sec (get-distance-from-note-on elapsed-time note)
        pitch-midi-number (j/get note :midi)
        note-duration-sec (j/get note :duration)
        canvas-height 500
        ; the time in seconds to consume a whole canvas from top to bottom
        canvas-duration-sec (landing-in-sec canvas-height 0)
        tile-height (note-height canvas-height note-duration-sec canvas-duration-sec)
        tile-y (height-for-distance canvas-height distance-from-note-on-sec)
        ]
    ; use RGB with 42 max value and draw 75% transparent blue
    ;(q/color-mode :rgb 40)
    ;(js/console.log "tile-y")
    ;(js/console.log tile-y)
    ;(q/fill 0 0 40 30)
    (q/rect (tile-x pitch-midi-number) (- tile-y tile-height) tile-width tile-height)
    )
  )

(defn has-started-playing [note elapsed-time]
  "Whether a 'note on' time has passed (i.e. it has started rolling over canvas height)"
  (< (get-distance-from-note-on elapsed-time note) 0))

(defn has-finished-playing [note elapsed-time]
  "Whether a 'note off' time has passed (i.e. it has completely rolled over canvas height)"
  (< (get-distance-from-note-off note elapsed-time) 0))

(defn inspect-note [note elapsed-time]
  "Return true if note rect should be be in canvas, false otherwise.
   A note at height > 0 and < canvas-height"
  (let [
        distance-from-note-on-sec (get-distance-from-note-on elapsed-time note)
        pitch-midi-number (j/get note :midi)
        note-duration-sec (j/get note :duration)
        canvas-height 500
        ; the time in seconds to consume a whole canvas from top to bottom
        canvas-duration-sec (landing-in-sec canvas-height 0)
        tile-height (note-height canvas-height note-duration-sec canvas-duration-sec)
        tile-y (- (height-for-distance canvas-height distance-from-note-on-sec) tile-height)
        ]
    ; use RGB with 42 max value and draw 75% transparent blue
    ;(q/color-mode :rgb 40)
    ;(js/console.log "tile-y")
    ;(js/console.log tile-y)
    ;(q/fill 0 0 40 30)
    (js/console.log "tile-y")
    (js/console.log tile-y)
    (cond
      (> tile-y canvas-height) {:type "past" :display false}
      (< tile-y 0) {:type "future" :display false}
      :else {:type "present" :display true :x (tile-x pitch-midi-number) :y (- tile-y tile-height) :w tile-width :h tile-height}
      )
    )
  )

(defn player-callback [event]
  (q/with-sketch (getSketch)
                 (swap! (q/state-atom) assoc-in [:player-time] (j/get event :time))
                 )
  )

(defn setup [frame-rate fixed-delay midi-track]
  (fn []
    (let [
          _ (js/console.log "Starting sketch")
          _ (js/console.log midi-track)
          notes (j/get midi-track :notes)
          ]
      (q/frame-rate frame-rate)
      ;(j/assoc! js/MIDIjs :player_callback player-callback)
      ;(q/set-state! :notes (j/get midi-track :notes) :start (+ (q/millis) fixed-delay)))
      ;(q/set-state! :player-time nil :notes midi-track-notes :start (+ (q/millis) fixed-delay)))
      (q/set-state! :notes [(first notes)] :start (+ (q/millis) fixed-delay)))
    )
  )

(defn is-not-future-note [note]
  (let [
        elapsed-time (get-elapsed-time)
        distance-from-note-on-sec (get-distance-from-note-on elapsed-time note)
        canvas-height 500
        tile-y (height-for-distance canvas-height distance-from-note-on-sec)
        ]
    (> tile-y 0)
    )
  )

(defn evaluate [notes]
  "Take from notes until above canvas (i.e. height 0)
   and appends to first item of the result (present-notes).
   If the note is past (height > canvas-height) discards it.
   Returns a vector of [(present-notes) (future-notes)]"
  (cond
    (empty? notes) nil
    :else (let [
                elapsed-time (get-elapsed-time)
                note (first notes)
                distance-from-note-on-sec (get-distance-from-note-on elapsed-time note)
                pitch-midi-number (j/get note :midi)
                note-duration-sec (j/get note :duration)
                canvas-height 500
                ; the time in seconds to consume a whole canvas from top to bottom
                canvas-duration-sec (landing-in-sec canvas-height 0)
                tile-height (note-height canvas-height note-duration-sec canvas-duration-sec)
                tile-y (height-for-distance canvas-height distance-from-note-on-sec)
                ]

            (cond
              (> tile-y canvas-height) (evaluate (rest notes))
              (< tile-y 0) nil
              :else (q/rect (tile-x pitch-midi-number) (- tile-y tile-height) tile-width tile-height)
              )
            )
    ))

(defn draw []
  (let [
        elapsed-time (get-elapsed-time)
        notes (q/state :notes)
        ; FIXME: Do some tailrec and map the note into a tile
        ;present-future (evaluate notes [] elapsed-time)
        ;present (first present-future)
        ;future (last present-future)
        notes-to-display (take-while (fn [note] (is-not-future-note note)) notes)
        ; FIXME do inline F:
        notes-to-play (take-while (fn [note] (has-started-playing note elapsed-time)) notes-to-display) ; notes-to-display)
        _ (js/console.log (str "PLAY?" (count notes-to-play)))
        to-remove-to-keep (split-with (fn [note] (has-finished-playing note elapsed-time)) notes)
        _ (js/console.log (str (count (first to-remove-to-keep)) " to remove"))
        _ (js/console.log (str (count (last to-remove-to-keep)) " to keep"))
        ]
    (q/background 255)
    (q/fill 0)
    ;(q/clear

    ;FIXME play all of them at once instead of map
    (dorun (map play-midi-note notes-to-play))

    ;(js/console.log (str "notes: " (count notes)))
    ;(js/console.log (str "notes-to-display: " (count notes-to-display)))
    ;(js/console.log (str "played: " (count (first played-not-played))))
    ;(js/console.log (str "not-played: " (count (last played-not-played))))

    ;(dorun (map display-note-rect notes))
    (dorun (map display-note-rect notes-to-display))

    (swap! (q/state-atom) assoc-in [:notes] (last to-remove-to-keep))
    ;(swap! (q/state-atom) assoc-in [:notes] (concat present future))
    ;(swap! (q/state-atom) assoc-in [:player-time] evt)

    ;(js/console.log (count notes))
    ;(js/console.log (count present))
    ;(swap! (q/state-atom) assoc-in [:notes] (concat present future))

    ;(q/text (str "Frame count: " (q/frame-count)) 600 40)
    ;(q/text (str "Frame rate: " (q/target-frame-rate)) 600 60)
    ;(q/text (str "Start time: " (get state :start)) 350 40)
    ;(q/text (str "Millis time: " (- (q/millis) (q/state :start))) 600 80)
    ;(q/text (str "Current time: " elapsed-time) 600 100)
    ;(q/text (str "Player time: " (q/state :player-time)) 600 120)
    )
  )

; https://github.com/quil/quil/wiki/ClojureScript
; https://github.com/quil/quil/wiki/Functional-mode-%28fun-mode%29
;; TODO proper delay-frame, non lagging timer
(defn start [midi-track]
  (q/sketch
    :host "sketch"
    :size [880 500]
    :setup (setup 45 0 midi-track)
    :draw draw
    )
  )
