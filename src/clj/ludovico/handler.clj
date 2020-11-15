(ns ludovico.handler
  (:require
    [reitit.ring :as reitit-ring]
    [ludovico.middleware :refer [middleware]]
    [hiccup.page :refer [include-js include-css html5]]
    [config.core :refer [env]]))

(def mount-target
  [:div#app
   [:h2 "Welcome to Ludovico"]
   [:p "please wait while Figwheel is waking up ..."]
   [:p "(Check the js console for hints if nothing exciting happens.)"]])

(defn head []
  [:head
   [:title "Ludovico"]
   [:meta {:charset "utf-8"}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    ; https://github.com/danigb/soundfont-player
    mount-target (include-js "/js/app.js" "/js/soundfont-player.js")
    ; FIXME: Try to use a clojure-only thing like Leipzig
    ; https://github.com/Tonejs/Midi
    [:script {:type "text/javascript" :src (hiccup.util/to-uri "https://unpkg.com/@tonejs/midi")}]
    ; https://www.midijs.net/
    [:script {:type "text/javascript", :src (hiccup.util/to-uri "//www.midijs.net/lib/midi.js")}]
    ]
   ))


(defn index-handler
  [_request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (loading-page)})

(def app
  (reitit-ring/ring-handler
    (reitit-ring/router
      [["/" {:get {:handler index-handler}}]
       ["/songs"
        ["" {:get {:handler index-handler}}]
        ["/:song-id" {:get {:handler    index-handler
                            :parameters {:path {:song-id int?}}}}]]
       ["/about" {:get {:handler index-handler}}]])
    (reitit-ring/routes
      (reitit-ring/create-resource-handler {:path "/" :root "/public"})
      (reitit-ring/create-default-handler))
    {:middleware middleware}))