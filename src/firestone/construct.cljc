(ns firestone.construct
  (:require [clojure.test :refer [function?]]
            [ysera.test :refer [is is-not is= error?]]
            [firestone.definitions :refer [get-definition]]))


(defn create-hero
  "Creates a hero from its definition by the given hero name. The additional key-values will override the default values."
  {:test (fn []
           (is= (create-hero "Jaina Proudmoore")
                {:name         "Jaina Proudmoore"
                 :entity-type  :hero
                 :damage-taken 0})
           (is= (create-hero "Jaina Proudmoore" :owner-id "p1")
                {:name         "Jaina Proudmoore"
                 :entity-type  :hero
                 :damage-taken 0
                 :owner-id     "p1"})
           (is= (create-hero "Jaina Proudmoore" :damage-taken 10)
                {:name         "Jaina Proudmoore"
                 :entity-type  :hero
                 :damage-taken 10}))}
  [name & kvs]
  (let [hero {:name         name
              :entity-type  :hero
              :damage-taken 0}]
    (if (empty? kvs)
      hero
      (apply assoc hero kvs))))

(defn create-card
  "Creates a card from its definition by the given card name. The additional key-values will override the default values."
  {:test (fn []
           (is= (create-card "Imp" :id "i")
                {:id          "i"
                 :entity-type :card
                 :name        "Imp"}))}
  [name & kvs]
  (let [card {:name        name
              :entity-type :card}]
    (if (empty? kvs)
      card
      (apply assoc card kvs))))

(defn create-minion
  "Creates a minion from its definition by the given minion name. The additional key-values will override the default values."
  {:test (fn []
           (is= (create-minion "Imp" :id "i" :attacks-performed-this-turn 1)
                {:attacks-performed-this-turn 1
                 :damage-taken                0
                 :entity-type                 :minion
                 :name                        "Imp"
                 :id                          "i"}))}
  [name & kvs]
  (let [definition (get-definition name)                    ; Will be used later
        minion {:damage-taken                0
                :entity-type                 :minion
                :name                        name
                :attacks-performed-this-turn 0}]
    (if (empty? kvs)
      minion
      (apply assoc minion kvs))))


(defn create-empty-state
  "Creates an empty state with the given heroes."
  {:test (fn []
           (is= (create-empty-state [(create-hero "Jaina Proudmoore")
                                     (create-hero "Jaina Proudmoore")])
                (create-empty-state))

           (is= (create-empty-state [(create-hero "Jaina Proudmoore")
                                     (create-hero "Jaina Proudmoore")])
                {:player-id-in-turn             "p1"
                 :players                       {"p1" {:id      "p1"
                                                       :deck    []
                                                       :hand    []
                                                       :minions []
                                                       :hero    {:name         "Jaina Proudmoore"
                                                                 :id           "h1"
                                                                 :damage-taken 0
                                                                 :entity-type  :hero
                                                                 :owner-id     "p1"}
                                                       :fatigue 1}
                                                 "p2" {:id      "p2"
                                                       :deck    []
                                                       :hand    []
                                                       :minions []
                                                       :hero    {:name         "Jaina Proudmoore"
                                                                 :id           "h2"
                                                                 :damage-taken 0
                                                                 :entity-type  :hero
                                                                 :owner-id     "p2"}
                                                       :fatigue 1}}
                 :counter                       1
                 :minion-ids-summoned-this-turn []}))}
  ([heroes]
    ; Creates Jaina Proudmoore heroes if heroes are missing.
   (let [heroes (->> (concat heroes [(create-hero "Jaina Proudmoore")
                                     (create-hero "Jaina Proudmoore")])
                     (take 2))]
     {:player-id-in-turn             "p1"
      :players                       (->> heroes
                                          (map-indexed (fn [index hero]
                                                         {:id      (str "p" (inc index))
                                                          :deck    []
                                                          :hand    []
                                                          :minions []
                                                          :hero    (assoc hero :id (str "h" (inc index)) :owner-id (str "p" (inc index)))
                                                          :fatigue 1}))
                                          (reduce (fn [a v]
                                                    (assoc a (:id v) v))
                                                  {}))
      :counter                       1
      :minion-ids-summoned-this-turn []}))
  ([]
   (create-empty-state [])))

(defn get-player
  "Returns the player with the given id."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-player "p1")
                    (:id))
                "p1"))}
  [state player-id]
  (get-in state [:players player-id]))

(defn get-hand
  "Returns the hand for the given player-id."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-hand "p1"))
                []))}
  ([state player-id]
   (:hand (get-player state player-id))))

(defn get-deck
  "Returns the deck for the given player-id."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-deck "p1"))
                []))}
  ([state player-id]
   (:deck (get-player state player-id)))
  )

(defn get-hero-id
  "Returns the hero id for the given player-id."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-hero-id "p1"))
                "h1"))}
  [state player-id]
   (:id (:hero (get-player state player-id)))
  )

(defn get-minions
  "Returns the minions on the board for the given player-id or for both players."
  {:test (fn []
           ; Getting minions is also tested in add-minion-to-board.
           (is= (-> (create-empty-state)
                    (get-minions "p1"))
                [])
           (is= (-> (create-empty-state)
                    (get-minions))
                []))}
  ([state player-id]
   (:minions (get-player state player-id)))
  ([state]
   (->> (:players state)
        (vals)
        (map :minions)
        (apply concat))))

(defn fatigue-damage
  "Increase a player's fatigue and return a tuple with the new state and the old fatigue."
  {:test (fn []
           (is= (fatigue-damage {:players {"p1" {:fatigue 1}}} "p1")
                [{:players {"p1" {:fatigue 2}}} 1]))}
  [state player-id]
  [(update-in state [:players player-id :fatigue] inc) (get-in state [:players player-id :fatigue])])

(defn- generate-id
  "Generates an id and returns a tuple with the new state and the generated id."
  {:test (fn []
           (is= (generate-id {:counter 6})
                [{:counter 7} 6]))}
  [state]
  [(update state :counter inc) (:counter state)])

(defn add-card-to-deck
  "Adds a card to a player's deck."
  {:test (fn []
           ; Adding a card to an empty deck
           (is= (as-> (create-empty-state) $
                      (add-card-to-deck $ {:player-id "p1" :card (create-card "Imp" :id "i")})
                      (get-deck $ "p1")
                      (map (fn [c] {:id (:id c) :name (:name c) :owner-id (:owner-id c)}) $))
                [{:id "i" :name "Imp" :owner-id "p1"}])
           ; Generating an id for the new card
           (let [state (-> (create-empty-state)
                           (add-card-to-deck {:player-id "p1" :card (create-card "Imp")}))]
             (is= (-> (get-deck state "p1")
                      (first)
                      (:id))
                  "c1")
             (is= (:counter state) 2))
           ; Adding two card to an empty hand
           (is= (as-> (create-empty-state) $
                      (add-card-to-deck $ {:player-id "p1" :card (create-card "Imp" :id "i1")})
                      (add-card-to-deck $ {:player-id "p1" :card (create-card "War Golem" :id "i2")})
                      (get-deck $ "p1")
                      (map (fn [c] {:id (:id c) :name (:name c)}) $))
                [{:id "i1" :name "Imp"}{:id "i2" :name "War Golem"}])
           )}
  [state {player-id :player-id card :card}]
  {:pre [(map? state) (string? player-id) (map? card)]}
  (let [[state id] (if (contains? card :id)
                     [state (:id card)]
                     (let [[state value] (generate-id state)]
                       [state (str "c" value)]))]
    (update-in state
               [:players player-id :deck]
               (fn [cards]
                 (conj cards
                       (assoc card :owner-id player-id
                                   :id id))))))

(defn add-card-to-hand
  "Adds a card to a player's hand."
  {:test (fn []
           ; Adding a card to an empty hand
           (is= (as-> (create-empty-state) $
                      (add-card-to-hand $ {:player-id "p1" :card (create-card "Imp" :id "i")})
                      (get-hand $ "p1")
                      (map (fn [c] {:id (:id c) :name (:name c)}) $))
                [{:id "i" :name "Imp"}])
           ; Generating an id for the new card
           (let [state (-> (create-empty-state)
                           (add-card-to-hand {:player-id "p1" :card (create-card "Imp")}))]
             (is= (-> (get-hand state "p1")
                      (first)
                      (:id))
                  "c1")
             (is= (:counter state) 2))
           ; Adding two card to an empty hand
           (is= (as-> (create-empty-state) $
                      (add-card-to-hand $ {:player-id "p1" :card (create-card "Imp" :id "i1")})
                      (add-card-to-hand $ {:player-id "p1" :card (create-card "War Golem" :id "i2")})
                      (get-hand $ "p1")
                      (map (fn [c] {:id (:id c) :name (:name c)}) $))
                [{:id "i1" :name "Imp"}{:id "i2" :name "War Golem"}])
           )}
  [state {player-id :player-id card :card}]
  {:pre [(map? state) (string? player-id) (map? card)]}
  (let [[state id] (if (contains? card :id)
                     [state (:id card)]
                     (let [[state value] (generate-id state)]
                       [state (str "c" value)]))]
    (update-in state
               [:players player-id :hand]
               (fn [cards]
                 (conj cards
                       (assoc card :owner-id player-id
                                   :id id))))))

(defn add-minion-to-board
  "Adds a minion with a given position to a player's minions and updates the other minions' positions."
  {:test (fn []
           ; Adding a minion to an empty board
           (is= (as-> (create-empty-state) $
                      (add-minion-to-board $ {:player-id "p1" :minion (create-minion "Imp" :id "i") :position 0})
                      (get-minions $ "p1")
                      (map (fn [m] {:id (:id m) :name (:name m)}) $))
                [{:id "i" :name "Imp"}])
           ; Adding a minion and update positions
           (let [state (-> (create-empty-state)
                           (add-minion-to-board {:player-id "p1" :minion (create-minion "Imp" :id "i1") :position 0})
                           (add-minion-to-board {:player-id "p1" :minion (create-minion "Imp" :id "i2") :position 0})
                           (add-minion-to-board {:player-id "p1" :minion (create-minion "Imp" :id "i3") :position 1})
                           (get-minions "p1"))]
             (is= (map :id state) ["i1" "i2" "i3"])
             (is= (map :position state) [2 0 1]))
           ; Generating an id for the new minion
           (let [state (-> (create-empty-state)
                           (add-minion-to-board {:player-id "p1" :minion (create-minion "Imp") :position 0}))]
             (is= (-> (get-minions state "p1")
                      (first)
                      (:id))
                  "m1")
             (is= (:counter state) 2)))}
  [state {player-id :player-id minion :minion position :position}]
  {:pre [(map? state) (string? player-id) (map? minion) (number? position)]}
  (let [[state id] (if (contains? minion :id)
                     [state (:id minion)]
                     (let [[state value] (generate-id state)]
                       [state (str "m" value)]))
        ready-minion (assoc minion :position position
                                   :owner-id player-id
                                   :id id)]
    (update-in state
               [:players player-id :minions]
               (fn [minions]
                 (conj (->> minions
                            (mapv (fn [m]
                                    (if (< (:position m) position)
                                      m
                                      (update m :position inc)))))
                       ready-minion)))))

(defn create-game
  "Creates a game with the given deck, hand, minions (placed on the board), and heroes."
  {:test (fn []
           (is= (create-game) (create-empty-state))
           (is= (create-game [{:hero (create-hero "Anduin Wrynn")}])
                (create-game [{:hero "Anduin Wrynn"}]))
           (is= (create-game [{:minions [(create-minion "Imp") (create-minion "War Golem")]}])
                (create-game [{:minions ["Imp" "War Golem"]}]))
           (is= (create-game [{:hand [(create-card "Imp") (create-card "War Golem")]}])
                (create-game [{:hand ["Imp" "War Golem"]}]))
           (is= (create-game [{:deck [(create-card "Imp") (create-card "War Golem")]}])
                (create-game [{:deck ["Imp" "War Golem"]}]))
           (is= (create-game [{:minions [(create-minion "Imp")]}
                              {:hero (create-hero "Anduin Wrynn")}]
                             :player-id-in-turn "p2")
                {:player-id-in-turn             "p2"
                 :players                       {"p1" {:id      "p1"
                                                       :deck    []
                                                       :hand    []
                                                       :minions [{:damage-taken                0
                                                                  :attacks-performed-this-turn 0
                                                                  :entity-type                 :minion
                                                                  :name                        "Imp"
                                                                  :id                          "m1"
                                                                  :position                    0
                                                                  :owner-id                    "p1"}]
                                                       :hero    {:name         "Jaina Proudmoore"
                                                                 :id           "h1"
                                                                 :entity-type  :hero
                                                                 :damage-taken 0
                                                                 :owner-id     "p1"}
                                                       :fatigue 1}
                                                 "p2" {:id      "p2"
                                                       :deck    []
                                                       :hand    []
                                                       :minions []
                                                       :hero    {:name         "Anduin Wrynn"
                                                                 :id           "h2"
                                                                 :entity-type  :hero
                                                                 :damage-taken 0
                                                                 :owner-id     "p2"}
                                                       :fatigue 1}}
                 :counter                       2
                 :minion-ids-summoned-this-turn []})

           ; Test to create game with cards in the hand and deck
           (is= (create-game [{:hand [(create-card "Imp")] :deck [(create-card "Imp")]}
                              {:hero (create-hero "Anduin Wrynn")}])
                {:player-id-in-turn             "p1"
                 :players                       {"p1" {:id      "p1"
                                                       :deck    [{:id          "c2"
                                                                  :entity-type :card
                                                                  :name        "Imp"
                                                                  :owner-id    "p1"}]
                                                       :hand    [{:name        "Imp"
                                                                  :id          "c1"
                                                                  :entity-type :card
                                                                  :owner-id    "p1"}]
                                                       :minions []
                                                       :hero    {:name         "Jaina Proudmoore"
                                                                 :id           "h1"
                                                                 :entity-type  :hero
                                                                 :owner-id     "p1"
                                                                 :damage-taken 0}
                                                       :fatigue 1}
                                                 "p2" {:id      "p2"
                                                       :deck    []
                                                       :hand    []
                                                       :minions []
                                                       :hero    {:name         "Anduin Wrynn"
                                                                 :id           "h2"
                                                                 :entity-type  :hero
                                                                 :owner-id     "p2"
                                                                 :damage-taken 0}
                                                       :fatigue 1}}
                 :counter                       3
                 :minion-ids-summoned-this-turn []})
           )}
  ([data & kvs]
   (let [state (as-> (create-empty-state (map (fn [player-data]
                                                (cond (nil? (:hero player-data))
                                                      (create-hero "Jaina Proudmoore")

                                                      (string? (:hero player-data))
                                                      (create-hero (:hero player-data))

                                                      :else
                                                      (:hero player-data)))
                                              data)) $
                     ; Add custom fatigue to state if the player has it.
                     (reduce (fn [state {player-id :player-id fatigue :fatigue}]
                               (assoc-in state [:players player-id :fatigue] fatigue))
                             $
                             (map-indexed (fn [index player-data]
                                            (if (nil? (:fatigue player-data))
                                                {:player-id (str "p" (inc index))
                                                 :fatigue   1}
                                                {:player-id (str "p" (inc index))
                                                 :fatigue   (:fatigue player-data)}))
                                          data))
                     ; Add minions to the state
                     (reduce (fn [state {player-id :player-id minions :minions}]
                               (reduce (fn [state [index minion]] (add-minion-to-board state {:player-id player-id
                                                                                              :minion    (if (string? minion)
                                                                                                           (create-minion minion)
                                                                                                           minion)
                                                                                              :position  index}))
                                       state
                                       ;returns a sequence 0 and the 1st elem. of "minions", 2 and the 2nd elem ... untill minions is exhausted
                                       (map-indexed (fn [index minion] [index minion]) minions)))
                             $
                             (map-indexed (fn [index player-data]
                                            {:player-id (str "p" (inc index))
                                             :minions   (:minions player-data)})
                                          data))

                     ; Add cards to hand
                     (reduce (fn [state {player-id :player-id hand :hand}]
                               (reduce (fn [state card] (add-card-to-hand state {:player-id player-id
                                                                                 :card (if (string? card)
                                                                                         (create-card card)
                                                                                         card)}))
                                       state
                                       hand
                                       ))
                             $
                             (map-indexed (fn [index player-data] {:player-id (str "p" (inc index)) :hand (:hand player-data)})
                                          data))

                     ;Add cards to deck
                     (reduce (fn [state {player-id :player-id deck :deck}]
                                (reduce (fn [state card] (add-card-to-deck state {:player-id player-id
                                                                                  :card (if (string? card)
                                                                                          (create-card card)
                                                                                          card)}))
                                       state
                                       deck
                                       ))
                               $
                               (map-indexed (fn [index player-data] {:player-id (str "p" (inc index)) :deck   (:deck player-data)})
                                            data))

                     )]
     (if (empty? kvs)
       state
       (apply assoc state kvs))))
  ([]
   (create-game [])))

(defn get-minion
  "Returns the minion with the given id."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (get-minion "i")
                    (:name))
                "Imp"))}
  [state id]
  (->> (get-minions state)
       (filter (fn [m] (= (:id m) id)))
       (first)))

(defn get-heroes
  {:test (fn []
           (is= (->> (create-game)
                     (get-heroes)
                     (map :name))
                ["Jaina Proudmoore" "Jaina Proudmoore"]))}
  [state]
  (->> (:players state)
       (vals)
       (map :hero)))

(defn get-character
  "Returns the character with the given id from the state."
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Jaina Proudmoore" :id "h1")}])
                    (get-character "h1")
                    (:name))
                "Jaina Proudmoore")
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (get-character "i")
                    (:name))
                "Imp"))}
  [state id]
  (->> (concat (get-minions state)
               (get-heroes state))
       (filter (fn [c] (= (:id c) id)))
       (first)))

(defn replace-minion
  "Replaces a minion with the same id as the given new-minion."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "minion")]}])
                    (replace-minion (create-minion "War Golem" :id "minion"))
                    (get-minion "minion")
                    (:name))
                "War Golem"))}
  [state new-minion]
  (let [owner-id (or (:owner-id new-minion)
                     (:owner-id (get-minion state (:id new-minion))))]
    (update-in state
               [:players owner-id :minions]
               (fn [minions]
                 (map (fn [m]
                        (if (= (:id m) (:id new-minion))
                          new-minion
                          m))
                      minions)))))

(defn replace-hero
  "Replaces a hero with the same id as the given new-hero."
  {:test (fn []
           (is= (-> (create-game (create-empty-state))
                    (replace-hero (create-hero "Rexxar" :id "h1"))
                    (get-character "h1")
                    (:name))
                "Rexxar"))}
  [state new-hero]
  (let [owner-id (or (:owner-id new-hero)
                     (:owner-id (get-character state (:id new-hero))))]
    (assoc-in state [:players owner-id :hero] new-hero)))

(defn update-minion
  "Updates the value of the given key for the minion with the given id. If function-or-value is a value it will be the
   new value, else if it is a function it will be applied on the existing value to produce the new value."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (update-minion "i" :damage-taken inc)
                    (get-minion "i")
                    (:damage-taken))
                1)
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (update-minion "i" :damage-taken 2)
                    (get-minion "i")
                    (:damage-taken))
                2))}
  [state id key function-or-value]
  (let [minion (get-minion state id)]
    (replace-minion state (if (function? function-or-value)
                            (update minion key function-or-value)
                            (assoc minion key function-or-value)))))

(defn update-hero
  "Updates the value of the given key for the hero with the given id. If function-or-value is a value it will be the
   new value, else if it is a function it will be applied on the existing value to produce the new value."
  {:test (fn []
           (is= (-> (create-game)
                    (update-hero "h1" :damage-taken inc)
                    (get-character "h1")
                    (:damage-taken))
                1)
           (is= (-> (create-game)
                    (update-hero "h1" :damage-taken 2)
                    (get-character "h1")
                    (:damage-taken))
                2))}
  [state id key function-or-value]
  (let [hero (get-character state id)]
    (replace-hero state (if (function? function-or-value)
                            (update hero key function-or-value)
                            (assoc hero key function-or-value)))))
(defn remove-minion
  "Removes a minion with the given id from the state."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (remove-minion "i")
                    (get-minions))
                []))}
  [state id]
  (let [owner-id (:owner-id (get-minion state id))]
    (update-in state
               [:players owner-id :minions]
               (fn [minions]
                 (remove (fn [m] (= (:id m) id)) minions)))))

(defn remove-minions
  "Removes the minions with the given ids from the state."
  {:test (fn []
           (is= (as-> (create-game [{:minions [(create-minion "Imp" :id "i1")
                                               (create-minion "Imp" :id "i2")]}
                                    {:minions [(create-minion "Imp" :id "i3")
                                               (create-minion "Imp" :id "i4")]}]) $
                      (remove-minions $ "i1" "i4")
                      (get-minions $)
                      (map :id $))
                ["i2" "i3"]))}
  [state & ids]
  (reduce remove-minion state ids))

(defn get-cards-from-deck
  "Returns a given number of cards from the deck of the player id."
  {:test (fn []
           ; Test getting a card from a player's deck
           (is= (-> (create-game [{:deck [(create-card "Imp" :id "i")]}])
                    (get-cards-from-deck "p1" 1))
                [(create-card "Imp" :id "i" :owner-id "p1")])
           ; Test getting cards from a empty deck
           (is= (-> (create-game)
                    (get-cards-from-deck "p1" 2))
                [])
           ; Test getting two cards from a player's deck
           (is= (-> (create-game [{:deck [(create-card "Imp" :id "i1")(create-card "Imp" :id "i2")(create-card "Imp" :id "i3")]}])
                    (get-cards-from-deck "p1" 2))
                [(create-card "Imp" :id "i1" :owner-id "p1") (create-card "Imp" :id "i2" :owner-id "p1")]))}

  [state player-id amount]
  {:pre [(map? state)(string? player-id)(number? amount)]}
  (let [deck (get-deck state player-id)]
              (let [size (count deck)]
              (cond
                (= size 0)
                []

                (<= size amount)
                (subvec deck 0 size)

                :else
                (subvec deck 0 amount)))))

(defn remove-card-from-deck
  "Removes a card with the given id from the given player's deck."
  {:test (fn []
           (is= (-> (create-game [{:deck [(create-card "Imp" :id "i")]}])
                    (remove-card-from-deck "p1" "i")
                    (get-deck "p1"))
                []))}
  [state player-id id]
  (update-in state [:players player-id :deck]
               (fn [cards]
                 (remove (fn [c] (= (:id c) id)) cards))))

(defn remove-cards-from-deck
  "Removes the cards with the given ids from the given player's deck."
  {:test (fn []
           (is= (as-> (create-game [{:deck [(create-card "Imp" :id "i1")(create-card "Imp" :id "i2")]}]) $
                      (remove-cards-from-deck $ "p1" "i1" "i2")
                      (get-deck $ "p1"))
                []))}
  [state player-id & ids]
  (reduce (fn[state id]
            (remove-card-from-deck state player-id id))
          state
          ids))
