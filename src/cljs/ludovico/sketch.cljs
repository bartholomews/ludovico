(ns ludovico.sketch
  (:refer-clojure :exclude [update])
  (:require
    [cljs.core.match :refer-macros [match]]
    [dommy.core :refer-macros [sel1]]
    [quil.core :as q :include-macros true]
    [quil.middleware :as m]
    )
  )

; https://www.reddit.com/r/Clojure/comments/afazxb/repl_workflow_in_quil/
(def paused (atom false))

(defn getSketchId [counter] (str "sketch-" counter))

(defn getSketch [counter] (q/get-sketch-by-id (getSketchId counter)))

(defn toggleSketch [sketch]
  (q/with-sketch sketch
                 (cond
                   (= @paused true) (q/no-loop)
                   :else (q/start-loop))
                 )
  )

(defn exit [counter]
  (js/console.log "SKETCH EXIT")
  (q/with-sketch (getSketch counter)
                 (q/exit)
                 ; (q/start-loop)
                 )
  )

(defn toggle [counter]
  (swap! paused not)
  (cond
    (nil? (getSketch counter)) (js/console.error "Attempting to start sketch before being initialised")
    :else (toggleSketch (getSketch counter))
    )
  )

; Setup - returns initial state
(defn setup [midiJson]
  (fn []
    ; (println "Available fonts:" (q/available-fonts))
    (q/frame-rate 60)
    (q/stroke 0xff3090a1)
    (q/stroke-weight 2)
    (q/fill 0xff7bcecc)
    {:song midiJson}
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

(defn draw [state]
  (q/background 255)
  (q/fill 0)
  ;(q/text (str "State: " (q/state)) 140 50)
  (let [frame (q/frame-count)]
    (q/text (str "Tile position: " (rem frame (q/width))) 140 20)
    ; draw moving box
    (q/rect 50 (rem frame (q/width)) 10 100)
    ; every 10 frames change frame rate
    ; frame rate cycles through [1, 6, 11, 16, 21]
    ;(when (zero? (rem frame (q/width)))
    ;  (q/text (str "Frame rate: " (q/target-frame-rate)) 300 20)
    ;  (q/frame-rate 0)
    ;  )

    ;(when (q/mouse-pressed? (q/text (str "Frame rate: " (q/target-frame-rate)) 300 40)))
    ;  (q/frame-rate (inc (* 5 (rem (quot frame 10) 5)))))
    )
  (q/text (str "Frame rate: " (q/target-frame-rate)) 300 20)
  (q/text (str "Frame count: " (q/frame-count)) 300 40)
  (q/text (str "Millis: " (q/millis)) 300 60)
  )

; https://github.com/quil/quil/wiki/ClojureScript
; https://github.com/quil/quil/wiki/Functional-mode-%28fun-mode%29
;; TODO delay-frame
(defn start [counter midiJson]
  (js/console.log (getSketchId counter))
  (q/sketch
    :host (getSketchId counter)
    :size [500 500]
    :setup (setup midiJson)
    :draw draw
    :update update
    :key-pressed handle-keypress
    :middleware [m/fun-mode]
    )
  )