(ns airplanes.gui
  (:gen-class)
  (:use [seesaw core color graphics behave]
        (seesaw [mouse :only (location)])
        airplanes.field
        airplanes.logic)
  (:import [airplanes.field Coords]
           [airplanes.logic Airplane]))

  (native!)
  (def f (frame :title "Airplanes"))
  (def size-of-cell 15)

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

  (move! f :to [200 200])
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

(config! f :content (make-panel))
(-> f show!)

(defn the-game [airplanes]
  (airport-building)
  (loop [length-of-level 10000 planes airplanes]
    (when (or (check-for-crash planes) (zero? length-of-level))
      (alert "Boooom! End of game :("))
    (when (empty? planes)
      (alert "Level complete! :)"))
    (when (and (pos? length-of-level) (not (empty? planes)) (not (check-for-crash planes)))
      ;(listen f :mouse-clicked (fn [e] (println (nth (location) 0) (nth (location) 1))))
     ; (listen f :mouse-clicked (fn [e] (println (quot (- (nth (location) 0) 207) size-of-cell) (quot (- (nth (location) 1) 229) size-of-cell))))
      ;(listen f :mouse-clicked (fn [e] (println (airport? (cell (Coords. (quot (- (nth (location) 0) 207) size-of-cell)
     ;                                                                    (quot (- (nth (location) 1) 229) size-of-cell)))))))
      (listen f :mouse-clicked (fn [e] (change-direction (location) airplanes)))
      (Thread/sleep speed)
      (fly-all planes)
      (config! f :content (make-panel))
      (recur (- length-of-level 100) (remove-landed (add-airplane planes))))))