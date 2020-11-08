(defproject ludovico "0.1.0-SNAPSHOT"
  :description "Ludovico"
  :url "https://github.com/bartholomews/ludovico"
  :license {:name "GNU General Public License version 3"
            :url  "http://opensource.org/licenses/GPL-3.0"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773" :scope "provided"]
                 [hiccup "1.0.5"]
                 [metosin/jsonista "0.2.6"]
                 [metosin/reitit "0.5.1"]
                 [pez/clerk "1.0.0"]
                 [quil "3.1.0"]
                 [reagent "0.10.0"]
                 [reagent-utils "0.3.3"]
                 [ring "1.8.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring-server "0.5.0"]
                 [venantius/accountant "0.2.5" :exclusions [org.clojure/tools.reader]]
                 [yogthos/config "1.1.7"]]

  :plugins [[lein-asset-minifier "0.4.6" :exclusions [org.clojure/clojure]]
            [lein-cljsbuild "1.1.7"]
            [lein-environ "1.1.0"]]

  :ring {:handler      ludovico.handler/app
         :uberwar-name "ludovico.war"}

  :min-lein-version "2.5.0"
  :uberjar-name "ludovico.jar"
  :main ludovico.server
  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  [[:css {:source "resources/public/css/site.css"
          :target "resources/public/css/site.min.css"}]]

  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
                           {:output-to     "target/cljsbuild/public/js/app.js"
                            :output-dir    "target/cljsbuild/public/js"
                            :source-map    "target/cljsbuild/public/js/app.js.map"
                            :optimizations :advanced
                            :infer-externs true
                            :pretty-print  false}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel     {:on-jsload "ludovico.core/mount-root"}
             :compiler
                           {:main          "ludovico.dev"
                            :asset-path    "/js/out"
                            :output-to     "target/cljsbuild/public/js/app.js"
                            :output-dir    "target/cljsbuild/public/js/out"
                            :source-map    true
                            :optimizations :none
                            :pretty-print  true}}
            :test
            {:source-paths ["src/cljs" "src/cljc" "test/cljs"]
             :compiler     {:main          ludovico.doo-runner
                            :asset-path    "/js/out"
                            :output-to     "target/test.js"
                            :output-dir    "target/cljstest/public/js/out"
                            :optimizations :whitespace
                            :pretty-print  true}}

            }
   }
  :doo {:build "test"
        :alias {:default [:chrome]}}

  :figwheel
  {:http-server-root "public"
   :server-port      3449
   :nrepl-port       7002
   :nrepl-middleware [cider.piggieback/wrap-cljs-repl
                      ]
   :css-dirs         ["resources/public/css"]
   :ring-handler     ludovico.handler/app}


  :sass {:source-paths ["src/sass"]
         :target-path  "resources/public/css"}

  :profiles {:dev     {:repl-options {:init-ns ludovico.repl}
                       :dependencies [[binaryage/devtools "1.0.2"]
                                      [cider/piggieback "0.5.1"]
                                      [figwheel-sidecar "0.5.20"]
                                      [nrepl "0.8.0"]
                                      ;; To silence warnings from sass4clj dependencies about missing logger implementation
                                      [org.slf4j/slf4j-nop "1.7.25"]
                                      [pjstadig/humane-test-output "0.10.0"]
                                      [prone "2020-01-17"]
                                      [ring/ring-mock "0.4.0"]
                                      [ring/ring-devel "1.8.1"]
                                      ]

                       :source-paths ["env/dev/clj"]
                       :plugins      [[deraen/lein-sass4clj "0.3.1"]
                                      [lein-doo "0.1.10"]
                                      [lein-figwheel "0.5.20"]
                                      ]

                       :injections   [(require 'pjstadig.humane-test-output)
                                      (pjstadig.humane-test-output/activate!)]

                       :env          {:dev true}}

             :uberjar {:hooks        [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks   ["compile" ["cljsbuild" "once" "min"]]
                       :env          {:production true}
                       :aot          :all
                       :omit-source  true}})
