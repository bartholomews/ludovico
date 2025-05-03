;(ns ludovico.frontend.sketch
;  (:refer-clojure :exclude [update])
;  (:require
;    [applied-science.js-interop :as j]
;    [cljs.core.match :refer-macros [match]]
;    [dommy.core :refer-macros [sel1]]
;    [quil.core :as q :include-macros true]
;    )
;  )
;
;; https://www.reddit.com/r/Clojure/comments/afazxb/repl_workflow_in_quil/
;(def paused (atom false))
;
;; tile width = canvas width / 88
;(def tile-width 10)
;
;(defn note-height [canvas-height note-duration-sec canvas-duration-sec]
;  "note-height : canvas-height = note-duration-sec : canvas-duration-sec"
;  (/ (* note-duration-sec canvas-height) canvas-duration-sec))
;
;; https://syntheway.com/MIDI_Keyboards_Middle_C_MIDI_Note_Number_60_C4.htm
;(defn tile-x [midi-key]
;  "The x-position of the note tile, supporting 88 keys from 21 (A0) to 108 (C8)"
;  (* tile-width (- midi-key 21))
;  )
;
;(defn getSketch [] (q/get-sketch-by-id "sketch"))
;
;(defn toggleSketch [sketch]
;  (q/with-sketch sketch
;                 (cond
;                   (= @paused true) (q/no-loop)
;                   :else (q/start-loop))
;                 )
;  )
;
;(defn exit []
;  (js/console.log "SKETCH EXIT")
;  (q/with-sketch (getSketch)
;                 (q/exit)
;                 ; (q/start-loop)
;                 )
;  )
;
;(defn toggle []
;  (swap! paused not)
;  (cond
;    (nil? (getSketch)) (js/console.error "Attempting to start sketch before being initialised")
;    :else (toggleSketch (getSketch))
;    )
;  )
;
;(defn get-elapsed-time []
;  "Time (in seconds) since the playback started"
;  (/ (- (q/millis) (q/state :start)) 1000))
;
;(defn get-distance-from-note-on [elapsed-time note]
;  "The distance in seconds of note from elapsed time (i.e. how long until it should be played)."
;  (- (j/get note :time) elapsed-time))
;
;(defn get-distance-from-note-off [note elapsed-time]
;  (+ (get-distance-from-note-on elapsed-time note) (j/get note :duration))
;  )
;
;(defn play-midi-note [note] (j/call note :synthF))
;
;(defn landing-in-sec [canvas-height current-height]
;  "Time in seconds to reach the bottom (i.e. canvas-height)"
;  (/ (- canvas-height current-height) (q/target-frame-rate)))
;
;(defn height-for-distance [canvas-height sec]
;  "Height value of a point in order to be at `sec` to reach the bottom (i.e. canvas-height)"
;  (- canvas-height (* sec (q/target-frame-rate)))
;  )
;
;(defn display-note-rect [note]
;  (let [
;        elapsed-time (get-elapsed-time)
;        distance-from-note-on-sec (get-distance-from-note-on elapsed-time note)
;        pitch-midi-number (j/get note :midi)
;        note-duration-sec (j/get note :duration)
;        canvas-height 500
;        ; the time in seconds to consume a whole canvas from top to bottom
;        canvas-duration-sec (landing-in-sec canvas-height 0)
;        tile-height (note-height canvas-height note-duration-sec canvas-duration-sec)
;        tile-y (height-for-distance canvas-height distance-from-note-on-sec)
;        ]
;    ; use RGB with 42 max value and draw 75% transparent blue
;    ;(q/color-mode :rgb 40)
;    ;(js/console.log "tile-y")
;    ;(js/console.log tile-y)
;    ;(q/fill 0 0 40 30)
;    (q/rect (tile-x pitch-midi-number) (- tile-y tile-height) tile-width tile-height)
;    )
;  )
;
;(defn has-started-playing [note elapsed-time]
;  "Whether a 'note on' time has passed (i.e. it has started rolling over canvas height)"
;  (< (get-distance-from-note-on elapsed-time note) 0))
;
;(defn has-finished-playing [note elapsed-time]
;  "Whether a 'note off' time has passed (i.e. it has completely rolled over canvas height)"
;  (< (get-distance-from-note-off note elapsed-time) 0))
;
;(defn setup [frame-rate fixed-delay midi-track]
;  (fn []
;    (let [
;          _ (js/console.log "Starting sketch")
;          _ (js/console.log midi-track)
;          notes (j/get midi-track :notes)
;          ]
;      (q/frame-rate frame-rate)
;      ;(j/assoc! js/MIDIjs :player_callback player-callback)
;      ;(q/set-state! :notes (j/get midi-track :notes) :start (+ (q/millis) fixed-delay)))
;      ;(q/set-state! :player-time nil :notes midi-track-notes :start (+ (q/millis) fixed-delay)))
;      (q/set-state! :notes notes :notes-playing [] :start (+ (q/millis) fixed-delay)))
;    )
;  )
;
;(defn is-not-future-note [note]
;  (let [
;        elapsed-time (get-elapsed-time)
;        distance-from-note-on-sec (get-distance-from-note-on elapsed-time note)
;        canvas-height 500
;        tile-y (height-for-distance canvas-height distance-from-note-on-sec)
;        ]
;    (> tile-y 0)
;    )
;  )
;
;(defn draw []
;  (let [
;        elapsed-time (get-elapsed-time)
;        notes-not-playing (q/state :notes)
;        notes-playing (filter (fn [note] (not (has-finished-playing note elapsed-time))) (q/state :notes-playing))
;        [in-canvas not-in-canvas] (split-with (fn [note] (is-not-future-note note)) notes-not-playing)
;        [new-notes-playing in-canvas-not-playing] (split-with (fn [note] (has-started-playing note elapsed-time)) in-canvas)
;        ]
;    (q/background 255)
;    (q/fill 0)
;    ;(js/console.log "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
;    ;FIXME play all of them at once instead of map
;    (dorun (map play-midi-note new-notes-playing))
;    ;(dorun (map display-note-rect notes))
;    (dorun (map display-note-rect (concat notes-playing in-canvas)))
;    ;(js/console.log "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
;    ; TODO: https://stackoverflow.com/questions/45484308/multiple-assoc-ins-in-one-swap-operation-eg-to-update-x-y-of-point-at-the-sa
;    (swap! (q/state-atom) assoc-in [:notes] (doall (concat in-canvas-not-playing not-in-canvas)))
;    (swap! (q/state-atom) assoc-in [:notes-playing] (concat new-notes-playing notes-playing))
;    ;(js/console.log "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
;    ;(q/text (str "Frame count: " (q/frame-count)) 600 40)
;    ;(q/text (str "Frame rate: " (q/target-frame-rate)) 600 60)
;    ;(q/text (str "Start time: " (get state :start)) 350 40)
;    ;(q/text (str "Millis time: " (- (q/millis) (q/state :start))) 600 80)
;    ;(q/text (str "Current time: " elapsed-time) 600 100)
;    ;(q/text (str "Player time: " (q/state :player-time)) 600 120)
;    )
;  )
;
;; https://github.com/quil/quil/wiki/ClojureScript
;; https://github.com/quil/quil/wiki/Functional-mode-%28fun-mode%29
;;; TODO proper delay-frame, non lagging timer
;(defn start [midi-track]
;  (q/sketch
;    :host "sketch"
;    :size [880 500]
;    :setup (setup 45 0 midi-track)
;    :draw draw
;    )
;  )
