(ns ludovico.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [ludovico.core-test]))

(doo-tests 'ludovico.core-test)
