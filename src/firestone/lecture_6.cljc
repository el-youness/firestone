(ns firestone.lecture-6
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as spec-test]
            [clojure.spec.gen.alpha :as gen]))

;; primitive types

(s/def ::integer int?)

(comment
  (s/valid? ::integer 7)
  (s/valid? ::integer 6.5))

;; Any one-variable functions

(s/def ::almost-correct-position (fn [x] (<= 0 x 8)))

(comment
  (s/valid? ::almost-correct-position 4)
  (s/valid? ::almost-correct-position 9)
  (s/explain ::almost-correct-position 9))

;; and operation

(s/def ::non-negative-int (fn [x]
                            (and (int? x)
                                 (not (neg? x)))))

(s/def ::non-negative-int (s/and int?
                                 (complement neg?)))

(comment
  (s/valid? ::non-negative-int 4)

  (s/valid? ::non-negative-int -4)
  (s/explain ::non-negative-int -4)

  (s/valid? ::non-negative-int 4.5)
  (s/explain ::non-negative-int 4.5))


(s/def ::health ::integer)

(s/def ::position (s/and ::non-negative-int
                         (fn [x] (< x 7))))

;; Sets

(s/def ::entity-type #{"card"
                       "hero"
                       "hero-power"
                       "minion"
                       "player"
                       "secret"})

(comment
  (s/valid? ::entity-type "minion")

  (s/valid? ::entity-type "legendary"))

;; Many

(s/def ::id string?)

(s/def ::valid-target-ids (s/coll-of ::id)) ; s/* can also be used

(comment
  (s/valid? ::valid-target-ids ["1" "2" "3"])
  (s/valid? ::valid-target-ids "3")
  (s/explain ::valid-target-ids "3"))


;; Entities (objects, maps)

(s/def ::can-use boolean?)
(s/def ::owner-id string?)
(s/def ::has-used-your-turn boolean?)
(s/def ::name string?)
(s/def ::description string?)
(s/def ::mana-cost pos-int?)
(s/def ::original-mana-cost pos-int?)


(s/def ::hero-power (s/and (s/keys :req-un [::can-use
                                            ::owner-id
                                            ::entity-type
                                            ::has-used-your-turn
                                            ::name
                                            ::description]
                                   :opt-un [::mana-cost
                                            ::original-mana-cost
                                            ::valid-target-ids])
                           (fn [hero-power]
                             (= (:entity-type hero-power) "hero-power"))))

(def my-hero-power {:can-use            true
                    :owner-id           "p1"
                    :entity-type        "hero-power"
                    :has-used-your-turn false
                    :name               "Fireblast"
                    :description        "Deal 1 damage."
                    :mana-cost          1
                    :original-mana-cost 2
                    :valid-target-ids   ["h2"]})

(comment
  (s/valid? ::hero-power my-hero-power)

  (s/explain ::hero-power (assoc my-hero-power :valid-target-ids "h2")))

;; or

(s/def ::player-id string?)

(s/def :event/name #{"start-turn"
                     "attack"})

(s/def ::start-turn-event (s/and (s/keys :req-un {:event/name
                                                  ::player-id})
                                 (fn [x]
                                   (= (:name x) "start-turn"))))

(s/def ::attack-event (s/and (s/keys :req-un {:event/name
                                              ::id})
                             (fn [x]
                               (= (:name x) "attack"))))


(s/def ::event (s/or :start-turn-event ::start-turn-event
                     :attack-event ::attack-event))

(comment
  (s/valid? ::event {:name "start-turn" :player-id "p1" :bla "bla"})
  (s/valid? ::event {:name "start" :player-id "p1" :bla "bla"})
  (s/explain ::event {:name "start" :player-id "p1" :bla "bla"})

  (s/conform ::event {:name "start-turn" :player-id "p1"})
  (clojure.repl/doc s/conform))

;; with functions

(defn f [id]
  {:pre [(s/valid? ::id id)]}
  (println id))

(comment
  (f 3)
  (f "3"))

(defn ranged-rand
  "Returns a random int in the range start <= rand < end"
  [start end]
  (+ start (long (rand (- end start)))))

(ranged-rand -4 4)

(s/fdef ranged-rand
        :args (s/and (s/cat :start int? :end int?)
                     (fn [{start :start end :end}] (< start end)))
        :ret int?
        :fn (s/and (fn [{ret :ret args :args}]
                     (>= ret (:start args)))
                   (fn [{ret :ret args :args}]
                     (< ret (:end args)))))

(comment
  (clojure.repl/doc ranged-rand))

(comment
  (spec-test/instrument `ranged-rand)
  (ranged-rand -2 8))




