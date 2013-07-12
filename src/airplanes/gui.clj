(ns airplanes.gui
  (:use [seesaw core color graphics behave]
        (seesaw [mouse :only (location)])
        airplanes.field
        airplanes.logic
        airplanes.constants)
  (:import [airplanes.constants Coords]
           [airplanes.constants Airplane]))

(def f (frame :title "Airplanes"
              :height (+ 38 (* dim 15))
              :width (+ 16 (* dim 15))
              :on-close :exit
              :resizable? false))

(defn get-color [n]
  (condp = n
    0 :green
    1 :blue
    2 :red))

(defn draw-triangle [n m direction]
  (let [scaling (fn [p] (* size-of-cell p))
        n-scaled (+ (scaling n) 1)
        m-scaled (+ (scaling m) 1)
        nplus-scaled (- (+ size-of-cell n-scaled) 2)
        mplus-scaled (- (+ size-of-cell m-scaled) 2)
        half-size (quot size-of-cell 2)]
    (condp = direction
      (Coords. 0 1) (polygon [n-scaled m-scaled]
                             [(+ n-scaled half-size) mplus-scaled]
                             [nplus-scaled m-scaled])

      (Coords. 1 0) (polygon [n-scaled m-scaled]
                             [n-scaled mplus-scaled]
                             [nplus-scaled (+ half-size m-scaled)])

      (Coords. -1 0) (polygon [nplus-scaled mplus-scaled]
                              [nplus-scaled m-scaled]
                              [n-scaled (+ m-scaled half-size)])

      (Coords. 0 -1) (polygon [nplus-scaled mplus-scaled]
                              [n-scaled mplus-scaled]
                              [(+ n-scaled half-size) m-scaled]))))

(defn find-airplane-by-coords [coords airplanes]
  (peek (filterv #(= coords (.coords @%)) airplanes)))

(defn change-direction [location airplanes]
  (let [[x y] location
        cell-x (quot (- x 208) size-of-cell)
        cell-y (quot (- y 230) size-of-cell)
        cell-coords (Coords. cell-x cell-y)]
    (when-let [clicked-airplane (find-airplane-by-coords cell-coords airplanes)]
      (send-off clicked-airplane
                #(assoc % :direction (turn (.direction @clicked-airplane))))
      (await clicked-airplane)
      (dosync
        (alter (cell cell-coords)
               #(assoc % :direction (.direction @clicked-airplane)))))))
      (send-off clicked-airplane #(assoc % :direction (turn (.direction @clicked-airplane))))
      (await clicked-airplane)
      (dosync
        (alter (cell cell-coords) #(assoc % :direction (.direction @clicked-airplane)))))))

(defn draw-field [c g]
  (doseq [n (range 0 dim)
          m (range 0 dim)]
    (let [current-cell @(cell (Coords. n m))
         current-state (current-cell :state)]
      (if-let [current-direction (current-cell :direction)]
        (draw g
          (draw-triangle n m current-direction)
          (style :foreground :blue :background :green))
        (draw g
          (rect (* size-of-cell n) (* size-of-cell m) size-of-cell size-of-cell)
          (style :background (get-color current-state)))))))

(defn make-panel []
    (border-panel
      :center (canvas :paint draw-field
                      :background :green
                      :bounds [60 60 (* dim 15) (* dim 15)])))

(defn make-panel []
    (border-panel
      :center (canvas :paint draw-field
                      :background :green
                      :bounds [60 60 (* dim 15) (* dim 15)])))

(defn display []
  (native!)
  (config! f :content (make-panel))
  (move! f :to [200 200])
  (show! f))

(listen f :mouse-clicked (fn [e] (change-direction (location) starting-planes)))

(defn the-game [airplanes]
  (airport-building)
  (display)
  (loop [remaining-time length-of-level planes airplanes]
    (when (or (check-for-crash @planes) (zero? remaining-time))
      (alert "Boooom! Game over :("))
    (when (empty? @planes)
      (alert "Level complete! :)"))
    (when (and (pos? remaining-time)
               (not (empty? @planes))
               (not (check-for-crash @planes)))
      (Thread/sleep speed)
      (fly-all @planes)
      (config! f :content (make-panel))
      (recur (dec remaining-time)
             (do
               (alter-var-root #'starting-planes
                               #(add-airplane % remaining-time))
               (alter-var-root #'starting-planes
                               remove-landed)
               #'starting-planes)))))