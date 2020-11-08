(ns ludovico.sketch
  (:require [quil.core :as q :include-macros true]
    ;          [quil.middleware :as m]
            )
  )

(defn draw []
  (q/background 255)
  (q/fill 0)
  (q/text (str "Frame rate: " (q/target-frame-rate)) 10 20)
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
(defn start []
  (q/sketch
    :draw draw
    :host "sketch"
    :size [500 500]))