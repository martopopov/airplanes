(ns airplanes.core
  (:gen-class)
  (:use clojure.pprint
        [seesaw core color graphics behave keymap]
        airplanes.futures
        airplanes.field)
  (:import [airplanes.field Coords]))

(def speed 2000)
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
    (println "baba")
    (conj airplanes (new-plane rand-coords rand-direction))))

(defn check-for-crash [airplanes]
  (some #(crashed? (deref %)) airplanes))

(defn fly-all [airplanes]
  (doseq [airplane airplanes]
    (dofutures 1 #(fly-update airplane))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;GUI;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

  (defn draw-field [c g]
    (doseq [n (range 0 dim)
            m (range 0 dim)]
     (draw g
           (rect (* size-of-cell n) (* size-of-cell m) size-of-cell size-of-cell)
           (style :background (get-color (@(cell (Coords. n m)) :state))))))

  (defn make-panel []
    (border-panel
      :center (canvas :paint draw-field
                      :background :black)))

  (-> f pack! show!)


(defn the-game [airplanes]
  (airport-building)
  (loop [length-of-level 1000 planes airplanes]
    (when (check-for-crash planes)
      (alert "Boooom! End of game :("))
    (when (zero? length-of-level)
      (alert "Level complete! :)"))
    (when (and (pos? length-of-level) (not (check-for-crash planes)))
      (Thread/sleep speed)
      (fly-all planes)
      (doseq [n (range 0 dim)
              m (range 0 dim)
              :when (not= (@(cell (Coords. n m)) :state) 0)]
        (print n m @(cell (Coords. n m))))
      (config! f :content (make-panel))
      (recur (- length-of-level 100) (remove-landed (add-airplane planes))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;(println "Hello world"))
  (let [airplanes (vector (agent (Airplane. (Coords. 8 8) (Coords. 1 1))) (agent (Airplane. (Coords. 7 9) (Coords. 1 1))))]
    (the-game airplanes)
    (shutdown-agents)))
