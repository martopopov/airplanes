(ns airplanes.field)

(def dim 20)
(defrecord Coords [x y])
(def airports [(Coords. 0 0) (Coords. 4 4) (Coords. 5 7)])

(def field
  (mapv (fn [_]
          (mapv (fn [_] (ref {:state 0})); 0 - free 1 - busy 2 - airport
                (range dim)))
        (range dim)))

(defn cell [position]
  (-> field (nth (.x position)) (nth (.y position))))

(defn out-of-field? [position]
  (or (>= (.x position) dim)
      (< (.x position) 0)
      (>= (.y position) dim)
      (< (.y position) 0)))

(defn turn [direction]
  (Coords. (- (.x direction)) (- (.y direction))))

(defn airport-building []
  (dosync
    (doseq [place airports]
      (alter (cell place) assoc :state 2))))

(defn check-state [cell state]
  (= (@cell :state) state))

(defn free? [place]
  (check-state place 0))

(defn busy? [place]
  (check-state place 1))

(defn airport? [place]
  (check-state place 2))