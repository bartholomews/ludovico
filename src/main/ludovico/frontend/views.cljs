(ns ludovico.frontend.views
  (:require
    [applied-science.js-interop :as j]
    [ludovico.frontend.player :as player]
    [ludovico.frontend.synth :as synth]
    [reitit.frontend.easy :as rfe]
    ))

(defn on-midi-file-selected [input]
  (let [
        fr (js/FileReader.) 
        files (j/get-in input [:target :files]) 
        file (first files)
        ]
     (j/assoc! fr :onload (fn [e] (player/on-midi-loaded (j/get-in e [:target :result])))) 
     (j/call fr :readAsDataURL file)
    )
  )

(defn home-page []
  [:span.main
   [:h1 "Ludovico - Player"]
   ; https://material-ui.com/components/selects/
   [:div
    [:h5 {:class "section-label"} "Load Midi"]
    ; https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API
    ; [:audio#midi-track {:src (get @player/midi-player-atom :midi-src) :status "stopped"}]
    [:input {:type "file" :on-change on-midi-file-selected}]
    [:div
      [:button {:on-click #(player/on-play-btn-click)} [:span (get @player/midi-player-atom :next)]]
     ; [:button {:on-click #(player/on-stop-btn-click)} [:span "Stop"]]
     ]
    ]
   ;[:div [:button {:aria-checked "false" :on-click (synth/test-bach! 74 0.1)} [:span "Test Bach"]]]
   ;[:div [:button {:aria-checked "false" :on-click (synth/test-soundfont!)} [:span "Test Soundfont"]]]
   [:div [:button {:aria-checked "false" :on-click (player/test-smplr!)} [:span "Test Smplr"]]]
   ; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   [:div#sketch]
   ;[:ul [:li [:a {:href (path-for :songs)} "Songs list"]]]
   ]
  )

(defn item-page []
  [:div [:h1 "test"]]
  )

(defn about-page []
  [:div
   [:h2 "About ludovico"]
   [:ul
    [:li [:a {:href "https://bartholomews.io"} "external link"]]
    ;;[:li [:a {:href (rfe/href ::frontpage)} "Back to frontpage"]]
    ]

   [:div
    {:content-editable true
     :suppressContentEditableWarning true}
    [:p "Link inside contentEditable element is ignored."]
    ;;[:a {:href (rfe/href ::frontpage)} "Link"]
    ]
   ]
  )


;(defn songs-page []
;  (fn []
;    [:span.main
;     [:h1 "Songs"]
;     [:ul (map (fn [song-id]
;                 [:li {:name (str "song-" song-id) :key (str "song-" song-id)}
;                  [:a {:href (path-for :song {:song-id song-id})} "Song: " song-id]])
;               (range 1 60))]]))
;
;
;(defn song-page []
;  (fn []
;    (let [routing-data (session/get :route)
;          song (get-in routing-data [:route-params :song-id])]
;      [:span.main
;       [:h1 (str "Song " song "")]
;       [:p [:a {:href (path-for :songs)} "Back to songs list"]]])))
