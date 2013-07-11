(ns airplanes.logic-test
  (:use clojure.test
        airplanes.field
        airplanes.logic
        airplanes.constants)
  (:import [airplanes.constants Coords]
          [airplanes.constants Airplane]))

(def airplanes-for-test (vector (agent (Airplane. (Coords. 2 2) (Coords. 1 0)))
                                (agent (Airplane. :landed (Coords. 1 0)))
                                (agent (Airplane. (Coords. 1 0) (Coords. 1 -1)))))

(deftest test-logic
  (is (= (Airplane. (Coords. 6 4) (Coords. 1 -1))
         (new-pos-and-dir (Coords. 5 5) (Coords. 1 -1)))
      "they move :)")
  (is (= (Airplane. (Coords. 5 (inc dim)) (Coords. -1 1))
         (new-pos-and-dir (Coords. 5 (inc dim)) (Coords. -1 -1)))
      "they change their directions")
  (is (crashed? (Airplane. :crashed (Coords. 1 -1)))
      "crashed indeed")
  (is (not (crashed? (Airplane. (Coords. 2 2) (Coords. 1 0))))
      "not crashed")
  (is (= 2
         (count (remove-landed airplanes-for-test)))
      "remove landed airplanes")
  (is (= 4
         (count (add-airplane airplanes-for-test (* 100 new-plane-time))))
      "there is a new plane, yey"))

