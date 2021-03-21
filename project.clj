(defproject ludovico "0.1.0-SNAPSHOT"
  :description "Ludovico"
  :url "https://github.com/bartholomews/ludovico"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.10.1"]             ; https://github.com/clojure/clojure
                 [org.clojure/clojurescript "1.10.773"]     ; https://github.com/clojure/clojurescript
                 [org.clojure/core.async "0.4.500"]
                 [org.clojure/core.match "1.0.0"]           ; https://github.com/clojure/core.match
                 [applied-science/js-interop "0.2.7"]       ; https://github.com/applied-science/js-interop
                 [arttuka/reagent-material-ui "4.11.0-3"]   ; https://github.com/arttuka/reagent-material-ui
                 [cljs-bach "0.3.0"]                        ; https://github.com/ctford/cljs-bach
                 [metosin/reitit "0.5.12"]                  ; https://github.com/metosin/reitit
                 [pez/clerk "1.0.0"]                        ; https://github.com/PEZ/clerk
                 [prismatic/dommy "1.1.0"]                  ; https://github.com/plumatic/dommy
                 [quil "3.1.0"]                             ; https://github.com/quil/quil
                 [reagent "0.10.0"                          ; https://github.com/reagent-project/reagent
                  :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                 [reagent-utils "0.3.3"]                    ; https://github.com/reagent-project/reagent-utils
                 [venantius/accountant "0.2.5"]             ; https://github.com/venantius/accountant
                 ]

  :source-paths ["src"]

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" "ludovico.test-runner"]}

  :profiles {:dev {:dependencies   [[com.bhauman/figwheel-main "0.2.12"]
                                    [com.bhauman/rebel-readline-cljs "0.1.4"]]

                   :resource-paths ["target"]
                   ;; need to add the compiled assets to the :clean-targets
                   :clean-targets  ^{:protect false} ["target"]}})

