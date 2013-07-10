(ns airplanes.gui
  (:gen-class)
  (:use [seesaw core color graphics behave]
        (seesaw [mouse :only (location)])
        airplanes.field
        airplanes.logic
        airplanes.constants)
  (:import [airplanes.constants Coords]
           [airplanes.constants Airplane]))


  (def f (frame :title "Airplanes"))

  (defn get-color [n]
    (condp = n
      0 :green
      1 :blue
      2 :red))

  ; (defn draw-rectangle [n m type]
  ;   (draw g
  ;         (rect (* size-of-cell n) (* size-of-cell m) size-of-cell size-of-cell)
  ;         (style :background (get-color type))))

  ; (defn draw-airport [n m]
  ;   (draw-rectangle n m 2))

  ; (defn draw-field [n m]
  ;   (draw-rectangle n m 0))

  ; (defn draw-airplane [n m]
  ;   (let [airplane ]))

  ; (defn render-cell [position]
  ;   (let [x (.x position)
  ;         y (.y position)
  ;         type (@(cell (Coords. n m)) :state)]
  ;     (condp = type
  ;       0 :))

  (defn find-airplane-by-coords [coords airplanes]
    (peek (filterv #(= coords (.coords @%)) airplanes)))

  (defn change-direction [location airplanes]
    (let [[x y] location
          cell-x (quot (- x 207) size-of-cell)
          cell-y (quot (- y 229) size-of-cell)
          cell-coords (Coords. cell-x cell-y)]
      (when-let [clicked-airplane (find-airplane-by-coords cell-coords airplanes)]
        (send clicked-airplane #(assoc % :direction (turn (.direction @clicked-airplane))))
        (await clicked-airplane))))

  (defn draw-field [c g]
    (doseq [n (range 0 dim)
            m (range 0 dim)]
     (draw g
           (rect (* size-of-cell n) (* size-of-cell m) size-of-cell size-of-cell)
           (style :background (get-color (@(cell (Coords. n m)) :state))))))

  (defn make-panel []
      (border-panel
        :center (canvas :paint draw-field
                        :background :black
                        :bounds [60 60 (* dim 15) (* dim 15)])))


(defn display []
  (native!)
	(config! f :content (make-panel))
  (move! f :to [200 200])
	(-> f pack! show!))

(defn the-game [airplanes]
  (airport-building)
  (display)
  (loop [remaining-time length-of-level planes airplanes]
    (when (or (check-for-crash planes) (zero? remaining-time))
      (alert "Boooom! End of game :("))
    (when (empty? planes)
      (alert "Level complete! :)"))
    (when (and (pos? remaining-time) (not (empty? planes)) (not (check-for-crash planes)))
      (listen f :mouse-clicked (fn [e] (change-direction (location) planes)))
      (Thread/sleep speed)
      (fly-all planes)
      (config! f :content (make-panel))
      (recur (dec remaining-time) (remove-landed (add-airplane planes remaining-time))))))