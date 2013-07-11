(ns airplanes.field
  (:use airplanes.constants)
  (:import [airplanes.constants Coords]))

(def field
  (mapv (fn [_]
          (mapv (fn [_] (ref {:state 0})); 0 - free 1 - busy 2 - airport
                (range dim)))
        (range dim)))

(defn cell [position]
  (-> field (nth (.x position)) (nth (.y position))))

(defn restart-field []
  (dosync
    (doseq [i (range dim)
            j (range dim)]
      (alter (cell (Coords. i j)) assoc :state 0))))


(defn out-of-field? [position]
  (or (>= (.x position) dim)
      (< (.x position) 0)
      (>= (.y position) dim)
      (< (.y position) 0)))

(defn turn [direction]
  "Turns the direction by 90 degress counter-clock-wise"
  (Coords. (.y direction) (- (.x direction))))

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