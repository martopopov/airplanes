(ns airplanes.constants)

(def dim 20)
(defrecord Coords [x y])
(def airports [(Coords. 0 0) (Coords. 8 2) (Coords. 6 10)])

(def speed 2000)
(def length-of-level 100)
(def new-plane-time 15)
(def size-of-cell 15)
(def stop-new-planes (quot length-of-level 5))
(defrecord Airplane [coords direction])
(def starting-planes (vector (agent (Airplane. (Coords. 8 8) (Coords. 1 0)))
                             (agent (Airplane. (Coords. 3 9) (Coords. -1 0)))))