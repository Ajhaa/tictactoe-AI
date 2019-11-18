(ns tictactoe.core
  (:gen-class))

(require '[clojure.edn :as edn])

(defn test-turn [turn c]
  (if (= c \?)
    turn
    nil))

(defn display-board [state n]
  (loop [state state]
    (if (empty? state)
      nil
      (do 
        (println (take n state))
        (recur (drop n state))))))

; dynamically generate winning states
(defn normal-states [n]
  (loop [c 0 states []]
    (if (= c n)
      states
      (let [row (* c n)]
        (recur 
          (+ c 1)
            (conj
              (conj states (range row (+ row n)))
              (range c (+ (+ c 1) (* (- n 1) n)) n)))))))

(defn all-winning-states [n]
  (conj 
    (conj 
      (normal-states n ) 
      (map + (range 0 (* n n) n) (range n))
    (map - (range 2 (* n n) n) (range n)))))

(def winning-states (all-winning-states 3))

(defn winning-lines [board]
  (map #(map (vec board) %) winning-states))

(defn is-winning-line [n] 
  (and (apply = n) (not= \? (first n))))

(defn get-victor [board]
  (first (some #(if (is-winning-line %) %) (winning-lines board))))

(defn end-state? [state]
  (boolean
    (or 
      (get-victor state) 
      (not-any? (partial = \?) state))))

(defn value [state]
  (case (get-victor state)
    \x 1
    \o -1
    0))

(defn generate-children [state turn]
  (loop [s state 
         head [] 
         res []]
    (if (empty? s)
      res
      (recur 
        (rest s)
        (conj head (first s))
        (if (= (test-turn turn (first s)) turn)
          (conj res (concat head [turn] (rest s)))
          res)))))

(declare mmax)
(declare abmmax)

; minmax without alpha-beta pruning
(defn mmin [node]
  (if (end-state? node)
    (value node)
    (apply min
      (map mmax 
        (generate-children node \o)))))

(defn mmax [node]
  (if (end-state? node)
    (value node)
    (apply max
      (map mmin 
        (generate-children node \x)))))

; alpha-beta pruning with custom one argument memoization 
(defn minvalue [node alpha beta]
  (if (end-state? node)
    (value node)
    (loop [beta beta 
            v 1
            children (generate-children node \o)]
      (cond
        (or (> alpha beta) (empty? children)) v
        :else 
          (let [v (min v (abmmax (first children) alpha beta))]
            (recur (min beta v) v (rest children)))))))

(def abmmin
  (let [mem (atom {})]
    (fn [node alpha beta]
      (or
        (when-let [memoed (get @mem node)]
          memoed) 
        (let [val (minvalue node alpha beta)]
          (swap! mem assoc node val)
          val)))))

(defn maxvalue [node alpha beta]
  (if (end-state? node)
    (value node)
    (loop [alpha alpha 
            v -1
            children (generate-children node \x)]
      (cond
        (or (> alpha beta) (empty? children)) v
        :else 
          (let [v (max v (abmmin (first children) alpha beta))]
            (recur (max alpha v) v (rest children)))))))

(def abmmax
  (let [mem (atom {})]
    (fn [node alpha beta]
      (or
        (when-let [memoed (get @mem node)]
          memoed) 
        (let [val (maxvalue node alpha beta)]
          (swap! mem assoc node val)
          val)))))

        
(defn play-game 
  ([start-state start-turn]
    (loop [game (seq start-state) 
             turn start-turn]
        (display-board game 3)
        (println "")
        (if (end-state? game)
          (value game)
          (recur 
            (if turn
              (apply max-key #(abmmin % -1 1) (generate-children game \x))
              (apply min-key #(abmmax % -1 1) (generate-children game \o)))
            (not turn)))))
  ([] (play-game "?????????" true)))

(defn player-state [state]
  (try
    (let [inp (edn/read-string (read-line))]
      (if (test-turn \x (nth state inp))
        (seq (assoc (vec state) inp \x))
        (player-state state)))
    (catch Exception e (player-state state))))

(defn player []
  (loop [game (seq "?????????")
         turn true]
    (display-board game 3)
    (println)
    (if (end-state? game)
      (case (value game)
        1 "x wins"
        -1 "o wins"
        "tie")
      (recur 
        (if turn
          (player-state game)
          (apply min-key #(abmmax % -1 1) (generate-children game \o)))
        (not turn)))))

(defn -main
  "Default TicTacToe"
  [& args]
  (player))
