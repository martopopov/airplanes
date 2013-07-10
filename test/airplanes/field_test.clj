(ns airplanes.field-test
  (:use clojure.test
        airplanes.field)
  (:import [airplanes.field Coords]))

(deftest test-field
    (is (out-of-field? (Coords. dim 0))
        "Tests out-of-field")
    (is (not (out-of-field? (Coords. (dec dim) 0)))
        "Tests in-field")
    (is (= (Coords. -1 -1) (turn (Coords. 1 -1)))
        "Tests turning direction")
    (is (= 3
           (do
             (airport-building)
             (apply + (map #(count (filter airport? %)) field))))
        "Tests building the airports")
    (is (airport? (cell (nth airports 1)))
        "Tests check-state?"))
