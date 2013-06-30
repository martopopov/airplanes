(ns airplanes.core
  (:gen-class))
(use 'seesaw.core)
(use 'clj-time.periodic)
(use 'clj.time.core)


; (defn wait-futures
;   "Waits for a sequence of futures to complete."
;   [& futures]
;   (doseq [f futures]
;     @f))

; (defn dofutures
;   "Takes a number n and a function and spawns n future, each calling
;    the passed function. It waits for all the futures to get realized
;    and returns. Useful for testing concurent behaviors."
;   [n & funcs]
;   (let [futures (doall (for [_ (range n)
;                              func funcs]
;                          (future (func))))]
;     (apply wait-futures futures)))


; (def dim 100)
; (def speed 200)
; (defrecord Coords [x y])
; (defrecord Airplane [coords direction])
; (def airports [(Coords. 20 20) (Coords. 40 40) (Coords 50 70)])

; (def field
;   (mapv (fn [_]
;           (mapv (fn [_] (ref {:state 0})); 0 - free 1 - busy 2 - airport
;                 (range dim)))
;         (range dim)))

; (defn cell [position]
;   (-> field (nth (.x position) (nth (.y position)))))

; (defn airport-building []
;   (dosync
;     (for [place airports]
;       (alter (cell airports) assoc :state 2))))


; (defn new-position [coords direction]
;   (Coords. (+ (.x coords) (.x direction))
;        	   (+ (.y coords) (.y direction))))


; (defn check-state [cell state]
;   (= (@cell :state) state))

; (defn free? [cell]
;   (check-state cell 0))

; (defn busy? [cell]
;   (check-state cell 1))

; (defn airport? [cell]
;   (check-state cell 2))


; (defn check-state-airplane [airplane state]
;   (= (.coords @airplane) state))

; (defn crashed? [airplane]
;   (check-state-airplane airplane :crashed))

; (defn landed? [airplane]
;   (check-state-airplane airplane :landed))

; (defn flying? [airplane]
;   (vec? (.coords @airplane)))


; (defn new-plane [coords direction]
;   (dosync
;   	(alter (cell coords) assoc :state 1)
;    	(agent (Airplane. coords direction))))

; (defn fly [airplane]
;   (let [direction (.coords airplane)
;         coords (.coords airplane)
;         new-pos (cell (new-position direction coords))]
;     (cond
;       (free? new-pos)    (Airplane. new-pos direction)
;   		(busy? new-pos)    (Airplane. :crashed direction)
;       (airport? new-pos) (Airplane. :landed direction))))

; (defn fly-update [airplane]
;   (if (flying? airplane)
;     (dosync
;      ; (Thread/sleep speed)
;       (alter (cell coords) #(update-in % [:state] dec))
;       (send airplane fly)
;       (await airplane)
;       (if (flying? airplane)
;         (let [new-cell (.coords @airplane)]
;           (alter new-cell inc))))))


; (defn remove-landed [airplanes]
;   (filterv landed? airplanes))

; (defn add-airplane [airplanes]
;   (letfn [(rand-coords []
;             (Coords. (rand-int 100) (rand-int 100)))
;           (rand-direction []
;             (Coords. (rand-int 1) (rand-int 1)))]
;     (conj airplanes (Airplane. (rand-coords) (rand-direction)))))

; (defn check-for-crash [airplanes]
;   (some? crashed? airplanes))

; (defn fly-all [airplanes]
;   (doseq [airplane airplanes]
;     (dofutures 1 #(fly-update airplane))))

; (def the-game [airplanes]
;   (dosync
;     (loop [length-of-level 1000]
;       (when (> length-of-level 0)
;         (Thread/sleep speed)
;         (fly-all airplanes)
;         (pprint world)
;         (recur (- x 100))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;GUI;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  (native!)
  (def f (frame :title "Airplanes"))
  (-> f pack! show!)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello world"))
  ;(let [airplanes [(Airplane. [10 10] [1 1]) (Airplane. [50 50] [-1 -1])]]
   ;(the-game airplanes)))

