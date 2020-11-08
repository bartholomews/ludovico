(ns ludovico.prod
  (:require [ludovico.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
