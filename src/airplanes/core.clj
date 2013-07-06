(ns airplanes.core
  (:gen-class))
(use 'clojure.pprint)
(use 'seesaw.core)
;(use 'clj-time.periodic)
;(use 'clj.time.core)

(defn wait-futures
  "Waits for a sequence of futures to complete."
  [& futures]
  (doseq [f futures]
    @f))

(defn dofutures
  "Takes a number n and a function and spawns n future, each calling
   the passed function. It waits for all the futures to get realized
   and returns. Useful for testing concurent behaviors."
  [n & funcs]
  (let [futures (doall (for [_ (range n)
                             func funcs]
                         (future (func))))]
    (apply wait-futures futures)))


(def dim 10)
(def speed 200)
(defrecord Coords [x y])
(defrecord Airplane [coords direction])
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


(defn new-pos-and-dir [coords direction]
  (let [new-coords (Coords. (+ (.x coords) (.x direction))
                            (+ (.y coords) (.y direction)))]
    (if (out-of-field? new-coords)
      (Airplane. coords (turn direction))
      (Airplane. new-coords direction))))

(defn check-state [cell state]
  (= (@cell :state) state))

(defn free? [place]
  (check-state place 0))

(defn busy? [place]
  (check-state place 1))

(defn airport? [place]
  (check-state place 2))

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
    (println "baba")
    (conj airplanes (new-plane rand-coords rand-direction))))

(defn check-for-crash [airplanes]
  (some #(crashed? (deref %)) airplanes))

(defn fly-all [airplanes]
  (doseq [airplane airplanes]
    (dofutures 1 #(fly-update airplane))))

(defn the-game [airplanes]
  (airport-building)
  (loop [length-of-level 1000 planes airplanes]
    (when (and (> length-of-level 0) (not (check-for-crash planes)))
      (Thread/sleep speed)
      (fly-all planes)
      (doseq [n (range 0 dim)
              m (range 0 dim)
              :when (not= (@(cell (Coords. n m)) :state) 0)]
        (print n m @(cell (Coords. n m))))
      (recur (- length-of-level 100) (remove-landed (add-airplane planes))))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;GUI;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  ;(native!)
 ; (def f (frame :title "Airplanes"))
 ; (-> f pack! show!)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;(println "Hello world"))
  (let [airplanes (vector (agent (Airplane. (Coords. 8 8) (Coords. 1 1))) (agent (Airplane. (Coords. 7 9) (Coords. 1 1))))]
    (the-game airplanes)))
