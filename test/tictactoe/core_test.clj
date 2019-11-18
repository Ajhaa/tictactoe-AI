(ns tictactoe.core-test
  (:require [clojure.test :refer :all]
            [tictactoe.core :refer :all]))

(deftest from-empty
  (testing "A tie happens from an empty board")
    (is (play-game) 0))

(deftest middle-game
  (testing "A tie happens from a certain ongoing game")
    (is (play-game "x??xo????" false) 0))

(deftest x-can-win
  (testing "x will win if it can")
    (is (play-game "xooxxo???" true) 1))

(deftest o-can-win
  (testing "o will win if it can")
    (is (play-game "xx??o????" false) -1))