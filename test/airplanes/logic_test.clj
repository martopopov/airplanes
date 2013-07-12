(ns airplanes.logic-test
  (:use clojure.test
        airplanes.field
        airplanes.logic
        airplanes.constants)
  (:import [airplanes.constants Coords]
          [airplanes.constants Airplane]))

(def airplanes-for-test (vector (agent (Airplane. (Coords. 2 2) (Coords. 1 0)))
                                (agent (Airplane. :landed (Coords. 1 0)))
                                (agent (Airplane. (Coords. 5 4) (Coords. 1 -1)))))

(deftest test-logic
  (is (= (Airplane. (Coords. 6 4) (Coords. 1 -1))
         (new-pos-and-dir (Coords. 5 5) (Coords. 1 -1)))
      "they move :)")
  (is (= (Airplane. (Coords. 5 (inc dim)) (Coords. -1 0))
         (new-pos-and-dir (Coords. 5 (inc dim)) (Coords. 0 1)))
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
      "there is a new plane, yey")
  (let [airplane @(peek (add-airplane airplanes-for-test (* 50 new-plane-time)))
        direction (.direction airplane)
        x-coords (.x (.coords airplane))
        y-coords (.y (.coords airplane))]
    (is (not= direction
              (Coords. 0 0))
      "they don't stay static")
    (is (or (= x-coords (inc dim))
            (= x-coords 0)
            (= y-coords (inc dim))
            (= y-coords 0))
      "they are on the boundaries"))
  (let [airplane (peek airplanes-for-test)]
    (fly-update airplane)
    (is (and (= (Airplane. (Coords. 6 3) (Coords. 1 -1))
                @airplane)
             (busy? (cell (Coords. 6 3)))
             (free? (cell (Coords. 5 4))))
      "they update")))