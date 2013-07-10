(ns airplanes.core
  (:gen-class)
  (:use airplanes.gui)
  (:import [airplanes.field Coords]
           [airplanes.logic Airplane]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [airplanes (vector (agent (Airplane. (Coords. 8 8) (Coords. 1 1))) (agent (Airplane. (Coords. 7 9) (Coords. 1 1))))]
    (the-game airplanes)
    (shutdown-agents)))
