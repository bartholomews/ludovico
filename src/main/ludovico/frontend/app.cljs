(ns ludovico.frontend.app
  (:require
    [cljsjs.moment]
    [ludovico.frontend.views :as views]
    [reagent.core :as r]
    [reagent.dom :as rd]
    [reitit.coercion.spec :as rss]
    [reitit.frontend :as rf]
    [reitit.frontend.easy :as rfe]
    [fipp.edn :as fedn]
    [goog.dom :as gdom]
    )
  )

(.addEventListener
  js/window
  "DOMContentLoaded"
  (fn [] (js/console.log "Page loaded."))
  )

(defn init []
  (println "This is the frontend, it hot reloads when using 'watch'"))

(def routes
  [["/"
    {:name ::home
     :view views/home-page}]

   ["/about"
    {:name ::about
     :view views/about-page}]

   ["/songs"
    {:name ::songs
     :view views/item-page}]
   
   ["/item/:id"
    {:name ::item
     :view views/item-page
     :parameters {:path {:id int?}}
     }
    ]
   ]
  )

(defonce match (r/atom nil))

(defn current-page []
  [:div
   [:ul
    [:li [:a {:href (rfe/href ::home)} "Home"]]
    [:li [:a {:href (rfe/href ::about)} "About"]]
    ;[:li [:a {:href (rfe/href ::songs)} "Songs"]]
    ;[:li [:a {:href (rfe/href ::item {:id 1})} "Item 1"]]
    ;[:li [:a {:href (rfe/href ::item {:id 2} {:foo "bar"})} "Item 2"]]
    ]
   (if @match
     (let [view (:view (:data @match))]
       [view @match]))
   ;; TODO[FB] what is this a debug string ?
   [ :pre (with-out-str (fedn/pprint @match))]
   ])

;; https://github.com/metosin/reitit/blob/master/examples/frontend/src/frontend/core.cljs
(defn init! []
  (let [current-time (.format (js/moment) "dddd")]
    (js/console.log (str "Today is " current-time))
    (rfe/start!
      (rf/router routes {:data {:coercion rss/coercion}})
      (fn [m] (reset! match m))
      ;; set to false to enable HistoryAPI
      {:use-fragment true})
    (rd/render [current-page] (gdom/getElement "app"))
    )
  )

(init!)
