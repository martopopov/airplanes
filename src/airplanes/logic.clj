(ns airplanes.logic
  (:gen-class)
  (:use airplanes.futures
        airplanes.field
        airplanes.constants)
  (:import [airplanes.constants Coords]
           [airplanes.constants Airplane]))

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
    (alter (cell coords) assoc :state 1 :direction direction)
    (agent (Airplane. coords direction))))

(defn fly [airplane]
  (let [direction (.direction @airplane)
        coords (.coords @airplane)
        new-pos (.coords (new-pos-and-dir coords direction))
        new-dir (.direction (new-pos-and-dir coords direction))
        new-pos-ref (cell new-pos)]
    (cond
      (free? new-pos-ref)    (do (send-off airplane #(assoc % :coords new-pos))
                                 (send-off airplane #(assoc % :direction new-dir)))
      (busy? new-pos-ref)    (send-off airplane #(assoc % :coords :crashed))
      (airport? new-pos-ref) (send-off airplane #(assoc % :coords :landed)))))

(defn fly-update [plane]
  (when (flying? @plane)
    (dosync
      (let [old-cell (cell (.coords @plane))]
        (alter old-cell #(update-in % [:state] (constantly 0)))
        (alter old-cell #(dissoc % :direction))))
    (fly plane)
    (await plane)
    (if (flying? @plane)
      (let [new-cell (cell (.coords @plane))]
        (dosync
          (alter new-cell #(update-in % [:state] (constantly 1)))
          (alter new-cell #(assoc % :direction (.direction @plane))))))))


(defn remove-landed [airplanes]
  (filterv (complement #(landed? (deref %))) airplanes))

(defn add-airplane [airplanes remaining-time]
  (if (and (zero? (mod remaining-time new-plane-time)) (> remaining-time stop-new-planes))
    (let [possible-directions (for [i [-1 0 1]
                                    j [-1 0 1]
                                    :when (and (not= [i j] [0 0]) (zero? (* i j)))]
                                (Coords. i j))
          possible-starts (for [i (range dim)
                                j (range dim)
                                :when (or (zero? (* i j)) (= (inc dim) i) (= (inc dim) j))]
                            (Coords. i j))
          rand-direction (nth possible-directions (rand-int 4))
          rand-coords (nth possible-starts (rand-int (count possible-starts)))]
      (conj airplanes (new-plane rand-coords rand-direction)))
    airplanes))

(defn check-for-crash [airplanes]
  (some #(crashed? (deref %)) airplanes))

(defn fly-all [airplanes]
  (doseq [airplane airplanes]
    (dofutures 1 #(fly-update airplane))))