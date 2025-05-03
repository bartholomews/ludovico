(ns ludovico.frontend.views
  (:require
    [applied-science.js-interop :as j]
    [reitit.frontend.easy :as rfe]
    ))

(defn home-page []
  [:div
   [:h2 "Welcome to frontend"]

   ;[:button
   ; {:type "button"
   ;  :on-click #(rfe/push-state ::item {:id 3})}
   ; "Item 3"]

   ;[:button
   ; {:type "button"
   ;  :on-click #(rfe/replace-state ::item {:id 4})}
   ; "Replace State Item 4"]
   ]
  )

(defn on-midi-file-selected [input]
  (let
    [fr (js/FileReader.)
     files (j/get-in input [:target :files])
     file (first files)
     ]
    ; (j/assoc! fr :onload (fn [e] (player/on-midi-loaded (j/get-in e [:target :result]))))
    (j/call fr :readAsDataURL file)
    )
  )

(defn midi-page []
  [:span.main
   [:h1 "Ludovico"]
   ; https://material-ui.com/components/selects/
   [:div
    [:h5 {:class "section-label"} "Load Midi"]
    ; https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API
    ; [:audio#midi-track {:src (get @player/midi-player-atom :midi-src) :status "stopped"}]
    [:input {:type "file" :on-change on-midi-file-selected}]
    [:div
     ; [:button {:on-click #(player/on-play-btn-click)} [:span (get @player/midi-player-atom :next)]]
     ; [:button {:on-click #(player/on-stop-btn-click)} [:span "Stop"]]
     ]
    ]
   ;[:div [:button {:aria-checked "false" :on-click (synth/test-bach! 74 0.1)} [:span "Test Bach"]]]
   ;[:div [:button {:aria-checked "false" :on-click (synth/test-soundfont!)} [:span "Test Soundfont"]]]
   ; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   [:div#sketch]
   ;[:ul [:li [:a {:href (path-for :songs)} "Songs list"]]]
   ]
  )

(defn about-page []
  [:div
   [:h2 "About frontend"]
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
