(ns airplanes.field-test
  (:use clojure.test
        airplanes.field
        airplanes.constants)
  (:import [airplanes.constants Coords]))

(deftest test-field
  (is (out-of-field? (Coords. dim 0))
      "Tests out-of-field")
  (is (not (out-of-field? (Coords. (dec dim) 0)))
      "Tests in-field")
  (are [x y] (= x y)
       (Coords. 1 0) (turn (Coords. 0 -1))
       (Coords. 0 1) (turn (Coords. 1 0))
       (Coords. 0 -1)  (turn (Coords. -1 0))
       (Coords. -1 0)  (turn (Coords. 0 1)))
  (is (= 3
        (do
          (airport-building)
          (apply + (map #(count (filter airport? %)) field))))
      "Tests building the airports")
  (is (airport? (cell (nth airports 1)))
      "Tests check-state?")
  (is (do
        (airport-building)
        (restart-field)
        (not (airport? (cell (nth airports 1)))))
      "checking restarting field"))