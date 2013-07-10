(ns airplanes.logic
  (:gen-class)
  (:use airplanes.futures
        airplanes.field)
  (:import [airplanes.field Coords]))

(def speed 1000)
(defrecord Airplane [coords direction])

(defn new-pos-and-dir [coords direction]
  (let [new-coords (Coords. (+ (.x coords) (.x direction))
                            (+ (.y coords) (.y direction)))]
    (if (out-of-field? new-coords)
      (Airplane. coords (turn direction))
      (Airplane. new-coords direction))))

(defn check-state-airplane [airplane state]
  (= (.coords airplane) state))

(defn crashed? [airplane]
  (check-state-airplane airplane :crashed))

(defn landed? [airplane]
  (check-state-airplane airplane :landed))

(defn flying? [airplane]
  (and (not (crashed? airplane))
       (not (landed? airplane))))

(defn new-plane [coords direction]
  (dosync
    (alter (cell coords) assoc :state 1)
    (agent (Airplane. coords direction))))

(defn fly [airplane]
  (let [direction (.direction @airplane)
        coords (.coords @airplane)
        new-pos (.coords (new-pos-and-dir coords direction))
        new-dir (.direction (new-pos-and-dir coords direction))
        new-pos-ref (cell new-pos)]
          (cond
            (free? new-pos-ref)    (do (send airplane #(assoc % :coords new-pos))
                                       (send airplane #(assoc % :direction new-dir)))
            (busy? new-pos-ref)    (send airplane #(assoc % :coords :crashed))
            (airport? new-pos-ref) (send airplane #(assoc % :coords :landed)))))

(defn fly-update [plane]
  (when (flying? @plane)
    (dosync
      (alter (cell (.coords @plane)) #(update-in % [:state] (constantly 0))))
    (fly plane)
    (await plane)
    (if (flying? @plane)
      (let [new-cell (cell (.coords @plane))]
        (dosync
          (alter new-cell #(update-in % [:state] (constantly 1))))))))


(defn remove-landed [airplanes]
  (filterv (complement #(landed? (deref %))) airplanes))

(defn add-airplane [airplanes]
  (let [rand-coords (Coords. (rand-int dim) (rand-int dim))
        rand-direction (Coords. (rand-int 2) (rand-int 2))]
   ; (conj airplanes (new-plane rand-coords rand-direction))
    airplanes))

(defn check-for-crash [airplanes]
  (some #(crashed? (deref %)) airplanes))

(defn fly-all [airplanes]
  (doseq [airplane airplanes]
    (dofutures 1 #(fly-update airplane))))