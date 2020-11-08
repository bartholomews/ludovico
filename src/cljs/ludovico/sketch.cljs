(ns ludovico.sketch
  (:refer-clojure :exclude [update])
  (:require
    [cljs.core.match :refer-macros [match]]
    [quil.core :as q :include-macros true]
    [quil.middleware :as m]
    )
  )

(defn init-state []
  {:score 0})

; Setup - returns initial state
(defn setup []
  ; (println "Available fonts:" (q/available-fonts))
  (q/frame-rate 10)
  (q/stroke 0xff3090a1)
  (q/stroke-weight 2)
  (q/fill 0xff7bcecc)
  (init-state))

(defn handle-keypress [state event]
  (match [(:key event)]
         [:space] (update-in state [:score] inc)
         :else state
         )
  )

(defn draw [state]
  (q/background 255)
  (q/fill 0)
  (q/text (str "Frame rate: " (q/target-frame-rate)) 10 20)
  (q/text (str "State: " (q/state)) 140 50)
  (q/text (str "State 2: " state) 140 80)
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
  (q/text (str "Frame count: " (q/frame-count)) 400 20)
  )

; https://github.com/quil/quil/wiki/ClojureScript
; https://github.com/quil/quil/wiki/Functional-mode-%28fun-mode%29
(defn start []
  (q/sketch
    :host "sketch"
    :size [500 500]
    :setup setup
    :draw draw
    :key-pressed handle-keypress
    :middleware [m/fun-mode]
    )
  )