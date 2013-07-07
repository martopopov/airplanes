(ns airplanes.core-test
  (:use clojure.test
        airplanes.core))

(deftest test-field
    (is (= false (out-of-field? (Coords. dim 0)))
        "Test out-of-field")
    (is (= (Coords. -1 -1) (turn (Coords. 1 1)))
        "Test turn direction"))
