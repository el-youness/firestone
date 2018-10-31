(ns firestone.basic
  (:require [ysera.test :refer [is is= is-not error?]]))

; Maps

(def a {})

; adding key values in a map
(assoc a :name "Tomas")

(def b {:name     "Tomas"
        :dog      false
        :age      10
        [0 0]     "origin"
        :children {:maja {:age 13}
                   :emil {:age 6}}})

(dissoc b :dog)

(update b :age (fn [old-value]
                 (+ 2 old-value)))

(update b :age inc)

(get b :age)
(:age b)

(get b [0 0])

; Nested map actions
(assoc-in a [:a :b :c] "Here!")

(get-in b [:children :maja])
(get-in b [:children :maja :b])

(update-in b [:children :maja :age] inc)

(keys b)
(vals b)

; Vectors
(def c [1 2 3])

(conj c 4)

; Lists
(def d (list 1 2 3))

(conj d 4)

; map, filter, reduce - functions

(map (fn [x] (inc x)) [1 2 3 4])
(map inc [1 2 3 4])

(filter (fn [x] (odd? x)) (range 10))
(filter even? (range 10))

(reduce (fn [a v]
          (update a
                  (if (odd? v)
                    :odd
                    :even)
                  inc))
        {:odd  0
         :even 0}
        (range 100))

(defn f
  "This is a nice doc string..."
  {:test (fn []
           (is= (f 3 4) 22)
           (is= (f 1 1) 6))}
  [x y]
  (+ (* 2 x) (* 4 y)))

(f 3 4)

(defn g
  "This is a nice doc string..."
  {:test (fn []
           (is= (g 3 4) 22)
           (is= (g 1 1) 6))}
  ([x y]
   (+ (* 2 x) (* 4 y)))
  ([x y & zs]
   (g x (apply + y zs))))

(g 2 3 4 5 6 7)
(g 2 3)

; Apply

(+ 1 2 3 4 5 6)
(range 7)
(apply + 1 2 3 4 (range 7))

; do

(do (println "Something")
    (+ 1 2))

; let
(let [name "Maja"]
  (println name)
  (let [name "Tomas"
        age 10]
    (println name)
    (inc age))
  (println name))


; Threading macros

; ->>

(reduce + 0 (map inc (filter odd? (range 10))))

(->> (range 10)
     (filter odd?)
     (map inc)
     (reduce + 0))

; ->

(dissoc (update (assoc {} :name "Tomas" :age 10) :age inc) :name)

(-> {}
    (assoc :name "Tomas" :age 10)
    (update :age inc)
    (dissoc :name))

(as-> {} $
      (assoc $ :name "Tomas" :age 10)
      (update $ :age inc)
      (dissoc $ :name))



















