(ns airplanes.core
  (:gen-class)
  (:use airplanes.gui
        airplanes.constants)
  (:import [airplanes.constants Coords]
           [airplanes.constants Airplane]))

(defn -main
  "a simple airplane game"
  [& args]
  (the-game #'starting-planes)
  (shutdown-agents))