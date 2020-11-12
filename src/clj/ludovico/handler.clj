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
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target (include-js "/js/app.js")
    ; FIXME: Try to use a clojure-only thing like Leipzig
    ; https://www.midijs.net/
    [:script {:type "text/javascript", :src (hiccup.util/to-uri "//www.midijs.net/lib/midi.js")}]
    ; https://www.jsdelivr.com/package/npm/midiconvert
    [:script {:type "text/javascript", :src (hiccup.util/to-uri "https://cdn.jsdelivr.net/npm/midiconvert@0.4.7/build/MidiConvert.min.js")}]
    ]
   ))


(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get {:handler index-handler}}]
     ["/songs"
      ["" {:get {:handler index-handler}}]
      ["/:song-id" {:get {:handler index-handler
                          :parameters {:path {:song-id int?}}}}]]
     ["/about" {:get {:handler index-handler}}]])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware middleware}))