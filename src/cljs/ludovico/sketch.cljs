(ns ludovico.sketch
  (:refer-clojure :exclude [update])
  (:require
    [applied-science.js-interop :as j]
    [cljs.core.match :refer-macros [match]]
    [dommy.core :refer-macros [sel1]]
    [quil.core :as q :include-macros true]
    [quil.middleware :as m]
    )
  )

; https://www.reddit.com/r/Clojure/comments/afazxb/repl_workflow_in_quil/
(def paused (atom false))

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

; Setup - returns initial state
(defn setup [frame-rate fixed-delay midi-track]
  (fn []
    (js/console.log "Starting sketch")
    (js/console.log midi-track)
    (js/console.log (j/get midi-track :notes))
    ; (println "Available fonts:" (q/available-fonts))
    (q/frame-rate frame-rate)
    (q/stroke 0xff3090a1)
    (q/stroke-weight 2)
    (q/fill 0xff7bcecc)
    {:notes      (j/get midi-track :notes)
     :frame-rate frame-rate
     :start      (+ (q/millis) fixed-delay)}
    )
  )

(defn handle-keypress [state event]
  (js/console.log (:key event))
  ;(match [(:key event)]
  ;       [:space] (assoc-in state [:paused] true)
  ;       [:a] (assoc-in state [:paused] false)
  ;       :else state
  ;       )
  state
  )

(defn update [state]
  ;(cond
  ;  (= (:paused state) true) (q/no-loop)
  ;  :else (q/start-loop))
  ;(js/console.log my-state)
  ;(cond
  ;  (= my-state true) (js/console.log "TRUE")
  ;  :else (js/console.log "FALSE"))
  state
  )

(defn should-display [distance]
  "A note which is not already played and less than 5s away from current-time should be displayed in canvas"
  ; fixme do abs
  (and (> distance -1) (< distance 5))
  )

(defn calc [note current-time]
  "The matching y point is at (frame - height) "
  (let [
        canvas-height (q/height)
        note-height 20
        note-time (j/get note :time)
        distance (cond
                   (< note-time 0) (- current-time note-time)
                   :else (- note-time current-time)
                   )

        ;note-touch (- canvas-height note-height)
        ]

    ; 0.0123
    ;(js/console.log "CURRENT_TIME?:")
    ;(js/console.log current-time)
    ;(js/console.log "NOTE_TIME?:")
    ;(js/console.log note-time)
    ;(js/console.log "DIFF?:")
    ;(js/console.log distance)
    ;(js/console.log "AT HEIGHT:")
    ;(js/console.log (- note-touch distance))
    ;(js/console.log "~~~~~~~~~~~~~~~~~~~~")
    ;(js/console.log percentage)
    ;(js/console.log reverse-percentage)
    ;(js/console.log res)

    (cond
      ; FIXME: Not -, should be a proportion based on frame height to start from top
      ; distance is %
      ; 0 => 100%
      ; 5 => 0 %
      (should-display distance) (let [percentage (* (/ distance 5.0) 100)
                                      reverse-percentage (- 100 percentage)
                                      res (* 500 (/ reverse-percentage 100))
                                      ]
                                  (q/rect 50 (- res 50) 20 25)
                                  )
      ; (- note-touch distance)
      :else nil
      )
    ;(q/rect 50 479 20 25)
    )
  )

(defn draw [state]
  (q/background 255)
  (q/fill 0)
  ;(q/text (str "State: " (q/state)) 140 50)
  (let [
        ;frame (q/frame-count)
        ;fps (/ (q/frame-count) (get state :frame-rate))
        current-time (/ (- (q/millis) (get state :start)) 1000)
        ;first-note (get (get state :notes) 0)
        ]
    ;(q/text (str "Tile position: " (rem frame (q/width))) 140 20)
    ; draw notes
    ;(q/clear)

    ; (calc first-note current-time)
    ;(js/console.log (get state :notes))
    ;(map (fn [note] (calc note current-time)) (get state :notes))
    (calc (get (get state :notes) 0) current-time)

    ; every 10 frames change frame rate
    ; frame rate cycles through [1, 6, 11, 16, 21]
    ;(when (zero? (rem frame (q/width)))
    ;  (q/text (str "Frame rate: " (q/target-frame-rate)) 300 20)
    ;  (q/frame-rate 0)
    ;  )

    ;(when (q/mouse-pressed? (q/text (str "Frame rate: " (q/target-frame-rate)) 300 40)))
    ;  (q/frame-rate (inc (* 5 (rem (quot frame 10) 5)))))

    ;(q/text (str "Frame rate: " (q/target-frame-rate)) 350 20)
    ;(q/text (str "Frame count: " (/ fps 100)) 350 40)
    (q/text (str "Current time: " current-time) 350 60)
    ;(q/text (str "Millis: " (q/millis)) 350 80)
    )
  )

; https://github.com/quil/quil/wiki/ClojureScript
; https://github.com/quil/quil/wiki/Functional-mode-%28fun-mode%29
;; TODO proper delay-frame, non lagging timer
(defn start [midi-track]
  (q/sketch
    :host "sketch"
    :size [500 500]
    :setup (setup 30 5000 midi-track)
    :draw draw
    :update update
    :key-pressed handle-keypress
    :middleware [m/fun-mode]
    )
  )