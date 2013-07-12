(ns airplanes.core
  (:gen-class)
  (:use airplanes.gui
        airplanes.constants)
  (:import [airplanes.constants Coords]
           [airplanes.constants Airplane]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (the-game #'starting-planes)
  (shutdown-agents))