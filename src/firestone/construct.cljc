(ns firestone.construct
  (:require [clojure.test :refer [function?]]
            [ysera.test :refer [is is-not is= error?]]
            [firestone.definitions :refer [get-definition]]))

(defn create-hero-power
  "Creates a hero power from its definition by the given hero power name. The additional key-values will override the default values."
  {:test (fn []
           (is= (create-hero-power "Fireblast")
                {:name        "Fireblast"
                 :type        :hero-power
                 :target-type :all
                 :used        false}))}
  [name & kvs]
  (let [definition (get-definition name)
        hero-power {:name        name
                    :type        (:type definition)
                    :target-type (:target-type definition)
                    :used        false}]
    (if (empty? kvs)
      hero-power
      (apply assoc hero-power kvs))))

(defn create-hero
  "Creates a hero from its definition by the given hero name. The additional key-values will override the default values."
  {:test (fn []
           (is= (create-hero "Jaina Proudmoore")
                {:name         "Jaina Proudmoore"
                 :entity-type  :hero
                 :damage-taken 0
                 :buffs        []
                 :hero-power   {:name        "Fireblast"
                                :type        :hero-power
                                :target-type :all
                                :used        false}})
           (is= (create-hero "Jaina Proudmoore" :owner-id "p1")
                {:name         "Jaina Proudmoore"
                 :entity-type  :hero
                 :damage-taken 0
                 :buffs        []
                 :owner-id     "p1"
                 :hero-power   {:name        "Fireblast"
                                :type        :hero-power
                                :target-type :all
                                :used        false}})
           (is= (create-hero "Jaina Proudmoore" :damage-taken 10)
                {:name         "Jaina Proudmoore"
                 :entity-type  :hero
                 :damage-taken 10
                 :buffs        []
                 :hero-power   {:name        "Fireblast"
                                :type        :hero-power
                                :target-type :all
                                :used        false}})
           (is= (create-hero "Jaina Proudmoore" :effects {:frozen true})
                {:name         "Jaina Proudmoore"
                 :entity-type  :hero
                 :damage-taken 0
                 :buffs        []
                 :effects      {:frozen true}
                 :hero-power   {:name        "Fireblast"
                                :type        :hero-power
                                :target-type :all
                                :used        false}})
           (is= (create-hero "Jaina Proudmoore" :hero-power (create-hero-power "Reinforce"))
                {:name         "Jaina Proudmoore"
                 :entity-type  :hero
                 :damage-taken 0
                 :buffs        []
                 :hero-power   {:name        "Reinforce"
                                :type        :hero-power
                                :target-type nil
                                :used        false}}))}
  [name & kvs]
  (let [hero {:name         name
              :entity-type  :hero
              :damage-taken 0
              :buffs        []
              :hero-power   (create-hero-power (:hero-power (get-definition name)))}]
    (if (empty? kvs)
      hero
      (apply assoc hero kvs))))

(defn create-card
  "Creates a card from its definition by the given card name. The additional key-values will override the default values."
  {:test (fn []
           (is= (create-card "Imp" :id "i")
                {:id          "i"
                 :entity-type :card
                 :type        :minion
                 :subtype     nil
                 :name        "Imp"
                 :target-type nil}))}
  [name & kvs]
  (let [definition (get-definition name)
        card {:name        name
              :entity-type :card
              :type        (:type definition)
              :subtype     (:subtype definition)
              :target-type (:target-type definition)}]
    (if (empty? kvs)
      card
      (apply assoc card kvs))))

(defn create-minion
  "Creates a minion from its definition by the given minion name. The additional key-values will override the default values."
  {:test (fn []
           (is= (create-minion "Ancient Watcher" :id "i" :attacks-performed-this-turn 1)
                {:attacks-performed-this-turn 1
                 :damage-taken                0
                 :entity-type                 :minion
                 :name                        "Ancient Watcher"
                 :id                          "i"
                 :silenced                    false
                 :buffs                       []})
           (is= (create-minion "Acolyte of Pain" :id "i" :attacks-performed-this-turn 1)
                {:attacks-performed-this-turn 1
                 :damage-taken                0
                 :entity-type                 :minion
                 :name                        "Acolyte of Pain"
                 :id                          "i"
                 :silenced                    false
                 :buffs                       []})
           (is= (create-minion "Blood Imp" :id "i")
                {:attacks-performed-this-turn 0
                 :damage-taken                0
                 :entity-type                 :minion
                 :name                        "Blood Imp"
                 :id                          "i"
                 :silenced                    false
                 :buffs                       [{:stealth true}]}))}
  [name & kvs]
  (let [definition (get-definition name)
        definition-buffs (select-keys definition [:stealth])
        minion {:damage-taken                0
                :entity-type                 :minion
                :name                        name
                :attacks-performed-this-turn 0
                :silenced                    false
                :buffs                       (if (empty? definition-buffs)
                                               []
                                               [definition-buffs])}]
    (if (empty? kvs)
      minion
      (apply assoc minion kvs))))

(defn create-secret
  "Creates a secret from its definition by the given secret name. The additional key-values will override the default values."
  {:test (fn []
           (is= (create-secret "Snake Trap" :id "s")
                {:name        "Snake Trap"
                 :id          "s"
                 :entity-type :secret}))}
  [name & kvs]
  (let [definition (get-definition name)
        secret {:name        name
                :entity-type :secret}]
    (if (empty? kvs)
      secret
      (apply assoc secret kvs))))

(defn create-empty-state
  "Creates an empty state with the given heroes."
  {:test (fn []
           (is= (create-empty-state [(create-hero "Jaina Proudmoore")
                                     (create-hero "Jaina Proudmoore")])
                (create-empty-state))

           (is= (create-empty-state [(create-hero "Jaina Proudmoore")
                                     (create-hero "Jaina Proudmoore")])
                {:player-id-in-turn             "p1"
                 :players                       {"p1" {:id                          "p1"
                                                       :attacks-performed-this-turn 0
                                                       :deck                        []
                                                       :hand                        []
                                                       :minions                     []
                                                       :secrets                     []
                                                       :hero                        (create-hero "Jaina Proudmoore"
                                                                                                 :id "h1"
                                                                                                 :owner-id "p1"
                                                                                                 :hero-power (create-hero-power "Fireblast" :owner-id "p1" :id "hp1"))
                                                       :max-mana                    10
                                                       :used-mana                   0
                                                       :extra-mana                  0
                                                       :fatigue                     1}
                                                 "p2" {:id                          "p2"
                                                       :attacks-performed-this-turn 0
                                                       :deck                        []
                                                       :hand                        []
                                                       :minions                     []
                                                       :secrets                     []
                                                       :hero                        (create-hero "Jaina Proudmoore" :id "h2"
                                                                                                 :owner-id "p2"
                                                                                                 :hero-power (create-hero-power "Fireblast" :owner-id "p2" :id "hp2"))
                                                       :max-mana                    10
                                                       :used-mana                   0
                                                       :extra-mana                  0
                                                       :fatigue                     1}}
                 :counter                       1
                 :minion-ids-summoned-this-turn []
                 :cards-played-this-turn        []
                 :seed                          0}))}
  ([heroes]
    ; Creates Jaina Proudmoore heroes if heroes are missing.
   (let [heroes (->> (concat heroes [(create-hero "Jaina Proudmoore")
                                     (create-hero "Jaina Proudmoore")])
                     (take 2))]
     {:player-id-in-turn             "p1"
      :players                       (->> heroes
                                          (map-indexed (fn [index hero]
                                                         {:id                          (str "p" (inc index))
                                                          :attacks-performed-this-turn 0
                                                          :deck                        []
                                                          :hand                        []
                                                          :minions                     []
                                                          :secrets                     []
                                                          :hero                        (assoc hero :id (str "h" (inc index))
                                                                                                   :owner-id (str "p" (inc index))
                                                                                                   :hero-power (-> (assoc (:hero-power hero)
                                                                                                                     :id
                                                                                                                     (str "hp" (inc index)))
                                                                                                                   (assoc :owner-id (str "p" (inc index)))))
                                                          :max-mana                    10
                                                          :used-mana                   0
                                                          :extra-mana                  0
                                                          :fatigue                     1}))
                                          (reduce (fn [a v]
                                                    (assoc a (:id v) v))
                                                  {}))
      :counter                       1
      :minion-ids-summoned-this-turn []
      :cards-played-this-turn        []
      :seed                          0}))
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


(defn get-player-id-in-turn
  "Returns the player-id of the player in turn."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-player-id-in-turn))
                "p1"))}
  [state]
  (get state :player-id-in-turn))

(defn opposing-player-id
  "Returns the id of the other player w/ regards to player-id"
  {:test (fn []
           (is= (opposing-player-id "p1")
                "p2")
           (is= (opposing-player-id "p2")
                "p1"))}
  [player-id]
  (if (= "p1" player-id) "p2" "p1"))

(defn switch-player-in-turn
  "Switches the current player in turn."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (switch-player-in-turn)
                    (get-player-id-in-turn))
                "p2"))}
  [state]
  (assoc state :player-id-in-turn (opposing-player-id (get-player-id-in-turn state))))

(defn get-players
  "Rrturns the players."
  {:test (fn []
           (is= (as-> (create-empty-state) $
                      (get-players $)
                      (map (fn [p] (:id p)) $))
                ["p1" "p2"]))}
  [state]
  [(get-player state "p1") (get-player state "p2")])

(defn get-hand
  "Returns the hand for the given player-id."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-hand "p1"))
                []))}
  ([player]
   (:hand player))
  ([state player-id]
   (get-hand (get-player state player-id))))

(defn get-deck
  "Returns the deck for the given player-id."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-deck "p1"))
                []))}
  ([player]
   (:deck player))
  ([state player-id]
   (get-deck (get-player state player-id))))

(defn get-secrets
  "Returns all secrets or the secrets for the given player-id."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-secrets "p1"))
                []))}
  ([state player-id]
   (:secrets (get-player state player-id)))
  ([state]
   (->> (:players state)
        (vals)
        (map :secrets)
        (apply concat))))

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
                [])
           (is= (-> (create-empty-state)
                    (get-player "p1")
                    (get-minions))
                []))}
  ([state player-id]
   (:minions (get-player state player-id)))
  ([state-or-player]
   (if (contains? state-or-player :players)
     (->> (:players state-or-player)
          (vals)
          (map :minions)
          (apply concat))
     (:minions state-or-player))))

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
                [{:id "i1" :name "Imp"} {:id "i2" :name "War Golem"}])
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
                [{:id "i1" :name "Imp"} {:id "i2" :name "War Golem"}]))}
  [state {player-id :player-id card :card}]
  {:pre [(map? state) (string? player-id) (map? card)]}
  (let [[state id] (if (contains? card :id)
                     [state (:id card)]
                     (let [[state value] (generate-id state)]
                       [state (str "c" value)]))]
    (update-in state
               [:players player-id :hand]
               (fn [cards]
                 (conj cards (assoc card :owner-id player-id
                                         :id id))))))

(defn add-minion-to-board
  "Adds a minion with a given position to a player's minions and updates the other minions' positions. Returns a tuple
  with the state and the id of the minion added to board."
  {:test (fn []
           ; Adding a minion to an empty board
           (let [[state id] (-> (create-empty-state)
                                (add-minion-to-board {:player-id "p1" :minion (create-minion "Imp" :id "i") :position 0}))]
             (is= (->> (get-minions state)
                       (map :name))
                  ["Imp"])
             (is= id "i"))
           ; Adding a minion and update positions
           (let [minions (as-> (create-empty-state) $
                               (first (add-minion-to-board $ {:player-id "p1" :minion (create-minion "Imp" :id "i1") :position 0}))
                               (first (add-minion-to-board $ {:player-id "p1" :minion (create-minion "Imp" :id "i2") :position 0}))
                               (first (add-minion-to-board $ {:player-id "p1" :minion (create-minion "Imp" :id "i3") :position 1}))
                               (get-minions $ "p1"))]
             (is= (map :id minions) ["i1" "i2" "i3"])
             (is= (map :position minions) [2 0 1]))
           ; Generating an id for the new minion
           (let [[state id] (-> (create-empty-state)
                                (add-minion-to-board {:player-id "p1" :minion (create-minion "Imp") :position 0}))]
             (is= (->> (get-minions state)
                       (map :name))
                  ["Imp"])
             (is= id "m1")
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
    [(update-in state [:players player-id :minions]
                (fn [minions]
                  (conj (->> minions
                             (mapv (fn [m]
                                     (if (< (:position m) position)
                                       m
                                       (update m :position inc)))))
                        ready-minion)))
     id]))

(defn add-secret-to-player
  "Adds a secret to a player."
  {:test (fn []
           ; Adding a secret
           (is= (as-> (create-empty-state) $
                      (add-secret-to-player $ "p1" (create-secret "Snake Trap" :id "s"))
                      (get-secrets $ "p1")
                      (map (fn [c] {:id (:id c) :name (:name c)}) $))
                [{:id "s" :name "Snake Trap"}])
           ; Generating an id for the new secret
           (let [state (-> (create-empty-state)
                           (add-secret-to-player "p1" (create-secret "Snake Trap")))]
             (is= (-> (get-secrets state "p1")
                      (first)
                      (:id))
                  "s1")
             (is= (:counter state) 2)))}
  [state player-id secret]
  {:pre [(map? state) (string? player-id) (map? secret)]}
  (let [[state id] (if (contains? secret :id)
                     [state (:id secret)]
                     (let [[state value] (generate-id state)]
                       [state (str "s" value)]))]
    (update-in state
               [:players player-id :secrets]
               (fn [secrets]
                 (conj secrets
                       (assoc secret :owner-id player-id
                                     :id id))))))

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
           (is= (create-game [{:secrets [(create-secret "Snake Trap")]}])
                (create-game [{:secrets ["Snake Trap"]}]))
           (is= (create-game [{:minions [(create-minion "Imp")]}
                              {:hero (create-hero "Anduin Wrynn")}]
                             :player-id-in-turn "p2")
                {:player-id-in-turn             "p2"
                 :players                       {"p1" {:id                          "p1"
                                                       :attacks-performed-this-turn 0
                                                       :deck                        []
                                                       :hand                        []
                                                       :minions                     [(create-minion "Imp"
                                                                                                    :id "m1"
                                                                                                    :owner-id "p1"
                                                                                                    :position 0)]
                                                       :secrets                     []
                                                       :hero                        (create-hero "Jaina Proudmoore"
                                                                                                 :id "h1"
                                                                                                 :entity-type :hero
                                                                                                 :damage-taken 0
                                                                                                 :owner-id "p1"
                                                                                                 :hero-power (create-hero-power "Fireblast" :owner-id "p1" :id "hp1"))
                                                       :max-mana                    10
                                                       :used-mana                   0
                                                       :extra-mana                  0
                                                       :fatigue                     1}
                                                 "p2" {:id                          "p2"
                                                       :attacks-performed-this-turn 0
                                                       :deck                        []
                                                       :hand                        []
                                                       :minions                     []
                                                       :secrets                     []
                                                       :hero                        (create-hero "Anduin Wrynn"
                                                                                                 :id "h2"
                                                                                                 :entity-type :hero
                                                                                                 :damage-taken 0
                                                                                                 :owner-id "p2"
                                                                                                 :hero-power (create-hero-power "Lesser Heal" :owner-id "p2" :id "hp2"))
                                                       :max-mana                    10
                                                       :used-mana                   0
                                                       :extra-mana                  0
                                                       :fatigue                     1}}
                 :counter                       2
                 :minion-ids-summoned-this-turn []
                 :cards-played-this-turn        []
                 :seed                          0})

           ; Test to create game with cards in the hand and deck
           (is= (create-game [{:attacks-performed-this-turn 1
                               :hand                        [(create-card "Imp")] :deck [(create-card "Imp")]}
                              {:hero (create-hero "Anduin Wrynn") :secrets [(create-secret "Snake Trap")]}])
                {:player-id-in-turn             "p1"
                 :players                       {"p1" {:id                          "p1"
                                                       :attacks-performed-this-turn 1
                                                       :deck                        [(create-card "Imp" :id "c2" :owner-id "p1")]
                                                       :hand                        [(create-card "Imp" :id "c1" :owner-id "p1")]
                                                       :minions                     []
                                                       :secrets                     []
                                                       :hero                        (create-hero "Jaina Proudmoore"
                                                                                                 :id "h1"
                                                                                                 :entity-type :hero
                                                                                                 :owner-id "p1"
                                                                                                 :damage-taken 0
                                                                                                 :hero-power (create-hero-power "Fireblast" :owner-id "p1" :id "hp1"))
                                                       :max-mana                    10
                                                       :used-mana                   0
                                                       :extra-mana                  0
                                                       :fatigue                     1}
                                                 "p2" {:id                          "p2"
                                                       :attacks-performed-this-turn 0
                                                       :deck                        []
                                                       :hand                        []
                                                       :minions                     []
                                                       :secrets                     [(create-secret "Snake Trap" :id "s3" :owner-id "p2")]
                                                       :hero                        (create-hero "Anduin Wrynn"
                                                                                                 :id "h2"
                                                                                                 :entity-type :hero
                                                                                                 :owner-id "p2"
                                                                                                 :damage-taken 0
                                                                                                 :hero-power (create-hero-power "Lesser Heal" :owner-id "p2" :id "hp2"))
                                                       :max-mana                    10
                                                       :used-mana                   0
                                                       :extra-mana                  0
                                                       :fatigue                     1}}
                 :counter                       4
                 :minion-ids-summoned-this-turn []
                 :cards-played-this-turn        []
                 :seed                          0})
           ; Test to add mana
           (is= (create-game [{:extra-mana 1} {:max-mana 5 :used-mana 2 :fatigue 4}])
                {:player-id-in-turn             "p1"
                 :players                       {"p1" {:id                          "p1"
                                                       :attacks-performed-this-turn 0
                                                       :deck                        []
                                                       :hand                        []
                                                       :minions                     []
                                                       :secrets                     []
                                                       :hero                        (create-hero "Jaina Proudmoore"
                                                                                                 :id "h1"
                                                                                                 :entity-type :hero
                                                                                                 :owner-id "p1"
                                                                                                 :damage-taken 0
                                                                                                 :hero-power (create-hero-power "Fireblast" :owner-id "p1" :id "hp1"))
                                                       :max-mana                    10
                                                       :used-mana                   0
                                                       :extra-mana                  1
                                                       :fatigue                     1}
                                                 "p2" {:id                          "p2"
                                                       :attacks-performed-this-turn 0
                                                       :deck                        []
                                                       :hand                        []
                                                       :minions                     []
                                                       :secrets                     []
                                                       :hero                        (create-hero "Jaina Proudmoore"
                                                                                                 :id "h2"
                                                                                                 :entity-type :hero
                                                                                                 :owner-id "p2"
                                                                                                 :damage-taken 0
                                                                                                 :hero-power (create-hero-power "Fireblast" :owner-id "p2" :id "hp2"))
                                                       :max-mana                    5
                                                       :used-mana                   2
                                                       :extra-mana                  0
                                                       :fatigue                     4}}
                 :counter                       1
                 :minion-ids-summoned-this-turn []
                 :cards-played-this-turn        []
                 :seed                          0})
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
                     ; Add minions to the state
                     (reduce (fn [state {player-id :player-id minions :minions}]
                               (reduce (fn [state [index minion]] (first (add-minion-to-board state {:player-id player-id
                                                                                                     :minion    (if (string? minion)
                                                                                                                  (create-minion minion)
                                                                                                                  minion)
                                                                                                     :position  index})))
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
                                                                                 :card      (if (string? card)
                                                                                              (create-card card)
                                                                                              card)}))
                                       state
                                       hand
                                       ))
                             $
                             (map-indexed (fn [index player-data] {:player-id (str "p" (inc index)) :hand (:hand player-data)})
                                          data))

                     ; Add cards to deck
                     (reduce (fn [state {player-id :player-id deck :deck}]
                               (reduce (fn [state card] (add-card-to-deck state {:player-id player-id
                                                                                 :card      (if (string? card)
                                                                                              (create-card card)
                                                                                              card)}))
                                       state
                                       deck
                                       ))
                             $
                             (map-indexed (fn [index player-data] {:player-id (str "p" (inc index)) :deck (:deck player-data)})
                                          data))

                     ; Add secrets
                     (reduce (fn [state {player-id :player-id secrets :secrets}]
                               (reduce (fn [state secret] (add-secret-to-player state player-id (if (string? secret)
                                                                                                  (create-secret secret)
                                                                                                  secret)))
                                       state
                                       secrets
                                       ))
                             $
                             (map-indexed (fn [index player-data] {:player-id (str "p" (inc index)) :secrets (:secrets player-data)})
                                          data))

                     ; Add mana and fatigue to the players
                     (reduce (fn [state {player-id  :player-id
                                         max-mana   :max-mana
                                         used-mana  :used-mana
                                         extra-mana :extra-mana
                                         fatigue    :fatigue
                                         aptt       :attacks-performed-this-turn}]
                               (-> (assoc-in state [:players player-id :max-mana] max-mana)
                                   (assoc-in [:players player-id :used-mana] used-mana)
                                   (assoc-in [:players player-id :extra-mana] extra-mana)
                                   (assoc-in [:players player-id :fatigue] fatigue)
                                   (assoc-in [:players player-id :attacks-performed-this-turn] aptt)))
                             $
                             (map-indexed (fn [index player-data]
                                            {:player-id                   (str "p" (inc index))
                                             :fatigue                     (if (nil? (:fatigue player-data))
                                                                            1
                                                                            (:fatigue player-data))
                                             :max-mana                    (if (nil? (:max-mana player-data))
                                                                            10
                                                                            (:max-mana player-data))
                                             :used-mana                   (if (nil? (:used-mana player-data))
                                                                            0
                                                                            (:used-mana player-data))
                                             :extra-mana                  (if (nil? (:extra-mana player-data))
                                                                            0
                                                                            (:extra-mana player-data))
                                             :attacks-performed-this-turn (if (nil? (:attacks-performed-this-turn player-data))
                                                                            0
                                                                            (:attacks-performed-this-turn player-data))})
                                          data)))]
     (if (empty? kvs)
       state
       (apply assoc state kvs))))
  ([]
   (create-game [])))

(defn get-max-mana
  "Returns the maximum usable mana for the player with the given id."
  {:test (fn []
           (is= (-> (create-game)
                    (get-max-mana "p1"))
                10))}
  ([player]
   (player :max-mana)
    )
  ([state player-id]
   (get-max-mana (get-player state player-id))))

(defn get-extra-mana
  "Returns the extra mana for the player with the given id."
  {:test (fn []
           (is= (-> (create-game)
                    (get-extra-mana "p2"))
                0)
           (is= (-> (create-game [{:extra-mana 2}])
                    (get-extra-mana "p1"))
                2))}
  ([player]
   (:extra-mana player))
  ([state player-id]
   (get-extra-mana (get-player state player-id))))

(defn add-extra-mana
  "Adds a given amount of extra-mana for the given player-id."
  {:test (fn []
           (is= (-> (create-game [{:extra-mana 1}])
                    (add-extra-mana "p1" 1))
                (create-game [{:extra-mana 2}])))}
  ([state player-id amount]
   (update-in state [:players player-id :extra-mana] (partial + amount))))

(defn reset-extra-mana
  {:test (fn []
           (is= (-> (create-game [{:extra-mana 2}])
                    (reset-extra-mana "p1")
                    (get-extra-mana "p1"))
                0))}
  [state player-id]
  (assoc-in state [:players player-id :extra-mana] 0))

(defn get-mana
  "Returns the mana available to use for the given player-id."
  {:test (fn []
           (is= (-> (create-game)
                    (get-mana "p1"))
                10)
           (is= (-> (create-game [{:max-mana 5}])
                    (get-mana "p1"))
                5)
           (is= (-> (create-game [{:used-mana 4}])
                    (get-mana "p1"))
                6)
           (is= (-> (create-game [{:extra-mana 2}])
                    (get-mana "p1"))
                10)
           (is= (-> (create-game [{:max-mana 7 :extra-mana 5}])
                    (get-mana "p1"))
                10)
           (is= (-> (create-game [{:max-mana 7 :extra-mana 2}])
                    (get-mana "p1"))
                9)
           (is= (-> (create-game [{:extra-mana 2 :used-mana 4}])
                    (get-mana "p1"))
                8)
           )}
  [state player-id]
  (let [player-data (get-player state player-id)
        max-mana (get-max-mana state player-id)
        extra-mana (get-extra-mana state player-id)
        used-mana (:used-mana player-data)]
    (min (- (+ max-mana extra-mana) used-mana) 10)))

(defn get-minion
  "Returns the minion with the given id."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (get-minion "i")
                    (:name))
                "Imp")
           (is (nil? (-> (create-game)
                         (get-minion "i")))))}
  [state id]
  (->> (get-minions state)
       (filter (fn [m] (= (:id m) id)))
       (first)))

(defn get-secret
  "Returns the secret with the given id."
  {:test (fn []
           (is= (-> (create-game [{:secrets [(create-secret "Snake Trap" :id "s")]}])
                    (get-secret "s")
                    (:name))
                "Snake Trap"))}
  [state id]
  (->> (get-secrets state)
       (filter (fn [s] (= (:id s) id)))
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

(defn get-hero
  "Returns the hero for the given player."
  {:test (fn []
           (is= (as-> (create-game [{:hero (create-hero "Jaina Proudmoore")}
                                    {:hero (create-hero "Anduin Wrynn")}]) $
                      [((get-hero $ "p1") :name) ((get-hero $ "p2") :name)])
                ["Jaina Proudmoore" "Anduin Wrynn"]))}
  ([player]
   (:hero player))
  ([state owner-id]
   (->> (get-heroes state)
        (filter (fn [h] (= (:owner-id h) owner-id)))
        (first))))

(defn get-hero-powers
  "Returns the hero-powers of both players."
  {:test (fn []
           (is= (->> (create-game [{:hero (create-hero "Jaina Proudmoore")}
                                   {:hero (create-hero "Anduin Wrynn")}])
                     (get-hero-powers)
                     (map :name))
                ["Fireblast" "Lesser Heal"]))}
  [state]
  (->> (get-heroes state)
       (map :hero-power)))

(defn get-hero-power
  "Returns the hero power with the given id."
  {:test (fn []
           (is= (-> (create-game)
                    (get-hero-power "hp1")
                    (:name))
                "Fireblast"))}
  [state id]
  (->> (get-hero-powers state)
       (filter (fn [hp] (= (:id hp) id)))
       (first)))

(defn get-hero-power-of-player
  "Returns the hero-power for the given player-id."
  {:test (fn []
           (is= (as-> (create-game [{:hero (create-hero "Jaina Proudmoore")}
                                    {:hero (create-hero "Anduin Wrynn")}]) $
                      [((get-hero-power-of-player $ "p1") :name) ((get-hero-power-of-player $ "p2") :name)])
                ["Fireblast" "Lesser Heal"]))}
  [state owner-id]
  (->> (get-heroes state)
       (filter (fn [h] (= (:owner-id h) owner-id)))
       (first)
       :hero-power))

(defn get-characters
  {:test (fn []
           (is= (->> (create-game [{:hero "Rexxar"}])
                     (get-characters)
                     (map :name))
                ["Rexxar" "Jaina Proudmoore"])
           (is= (->> (create-game [{:minions ["Imp"]}])
                     (get-characters)
                     (map :name))
                ["Imp" "Jaina Proudmoore" "Jaina Proudmoore"]))}
  [state]
  (concat (get-minions state)
          (get-heroes state)))

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
  (->> (get-characters state)
       (filter (fn [c] (= (:id c) id)))
       (first)))

(defn get-trigger-entities [state]
  (concat (get-minions state)
          (get-secrets state)))

(defn get-board-entity
  "Returns the hero, minion or secret with the given id from the state."
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Jaina Proudmoore" :id "h1")}])
                    (get-board-entity "h1")
                    (:name))
                "Jaina Proudmoore")
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (get-board-entity "i")
                    (:name))
                "Imp")
           (is= (-> (create-game [{:secrets [(create-secret "Snake Trap" :id "s")]}])
                    (get-board-entity "s")
                    (:name))
                "Snake Trap"))}
  [state id]
  (->> (concat (get-minions state)
               (get-heroes state)
               (get-secrets state))
       (filter (fn [c] (= (:id c) id)))
       (first)))

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
           (is= (-> (create-game [{:deck [(create-card "Imp" :id "i1") (create-card "Imp" :id "i2") (create-card "Imp" :id "i3")]}])
                    (get-cards-from-deck "p1" 2))
                [(create-card "Imp" :id "i1" :owner-id "p1") (create-card "Imp" :id "i2" :owner-id "p1")]))}

  [state player-id amount]
  {:pre [(map? state) (string? player-id) (number? amount)]}
  (let [deck (get-deck state player-id)]
    (let [size (count deck)]
      (cond
        (= size 0)
        []

        (<= size amount)
        (take size deck)

        :else
        (take amount deck)))))


(defn get-all
  "Returns the heroes, minions, secrets and all card (both in hand and deck) from the state."
  {:test (fn []
           (is= (->> (create-game)
                     (get-all)
                     (map :name))
                ["Jaina Proudmoore" "Jaina Proudmoore"])
           (is= (->> (create-game [{:minions [(create-minion "Imp")]
                                   :secrets [(create-secret "Snake Trap")]}
                                   {:hand [(create-card "Imp")]}])
                    (get-all)
                    (map :name))
                ["Imp" "Jaina Proudmoore" "Jaina Proudmoore" "Snake Trap" "Imp"]))}
  [state]
  (concat (get-minions state)
          (get-heroes state)
          (get-secrets state)
          (concat (get-deck state "p1")
                  (get-deck state "p2"))
          (concat (get-hand state "p1")
                  (get-hand state "p2"))))

(defn get-entity
  "Returns any entity from the state, given the right id."
  [state id]
  (->> (get-all state)
       (filter (fn [c] (= (:id c) id)))
       (first)))

(defn hero?
  "Checks if the character with given id is a hero."
  {:test (fn []
           (is (-> (create-game [{:hero (create-hero "Rexxar" :id "h1")}])
                   (hero? "h1")))
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "imp")]}])
                       (hero? "imp"))))}
  ([entity]
   (= (get entity :entity-type) :hero))
  ([state id]
   (hero? (get-board-entity state id))))

(defn minion?
  "Checks if the character with given id is a minion."
  {:test (fn []
           (is-not (-> (create-game [{:minion (create-hero "Rexxar" :id "h1")}])
                       (minion? "h1")))
           (is (minion? (create-minion "Imp"))))}
  ([entity]
   (= (get entity :entity-type) :minion))
  ([state id]
   (minion? (get-board-entity state id))))

(defn card?
  "Checks if the entity with given id is a card."
  {:test (fn []
           (is (-> (create-game [{:hand [(create-card "Imp" :id "i")] }])
                   (card? "i"))))}
  ([entity]
   (= (get entity :entity-type) :card))
  ([state id]
   (card? (get-entity state id))))

(defn secret?
  "Checks if the character with given id is a hero."
  {:test (fn []
           (is-not (-> (create-game [{:hero (create-hero "Rexxar" :id "h1")}])
                       (secret? "h1")))
           (is (secret? (create-secret "Snake Trap"))))}
  ([entity]
   (= (get entity :entity-type) :secret))
  ([state id]
   (secret? (get-board-entity state id))))

(defn get-damage
  "returns the amount of damage taken by the character"
  {:test (fn []
           (is= (get-damage (create-hero "Rexxar" :id "h1" :damage-taken 2))
                2)
           (is= (get-damage (create-minion "Imp" :damage-taken 1))
                1))}
  [character]
  (:damage-taken character))

(defn get-card-from-hand
  "Returns the card with the given id from the hand."
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Imp" :id "imp")]}])
                    (get-card-from-hand "imp")
                    (:name))
                "Imp"))}
  [state id]
  (->> (concat (get-hand state "p1")
               (get-hand state "p2"))
       (filter (fn [c] (= (:id c) id)))
       (first)))

(defn get-minion-ids-summoned-this-turn
  "Returns the minion ids summoned this turn from the state."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}]
                                 :minion-ids-summoned-this-turn ["i"])
                    (get-minion-ids-summoned-this-turn))
                ["i"]))}
  [state]
  (:minion-ids-summoned-this-turn state))

(defn reset-minion-ids-summoned-this-turn
  "Wakes up all the minions."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}] :minion-ids-summoned-this-turn ["i"])
                    (reset-minion-ids-summoned-this-turn)
                    (get-minion-ids-summoned-this-turn)
                    (count))
                0))}
  [state]
  (assoc state :minion-ids-summoned-this-turn []))

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
                    (get-board-entity "h1")
                    (:name))
                "Rexxar"))}
  [state new-hero]
  (let [owner-id (or (:owner-id new-hero)
                     (:owner-id (get-board-entity state (:id new-hero))))]
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
                    (get-board-entity "h1")
                    (:damage-taken))
                1)
           (is= (-> (create-game)
                    (update-hero "h1" :damage-taken 2)
                    (get-board-entity "h1")
                    (:damage-taken))
                2))}
  [state id key function-or-value]
  (let [hero (get-board-entity state id)]
    (replace-hero state (if (function? function-or-value)
                          (update hero key function-or-value)
                          (assoc hero key function-or-value)))))

(defn update-hero-power
  "Updates the value of the given key for the hero power for the given player id. If function-or-value is a value it will be the
   new value, else if it is a function it will be applied on the existing value to produce the new value."
  {:test (fn []
           (is (-> (create-game)
                   (update-hero-power "p1" :used true)
                   (get-hero-power-of-player "p1")
                   :used)))}
  [state player-id key function-or-value]
  (let [hero (get-hero state player-id)
        hero-power (get-hero-power-of-player state player-id)]
    (replace-hero state (assoc hero :hero-power (if (function? function-or-value)
                                                  (update hero-power key function-or-value)
                                                  (assoc hero-power key function-or-value))))))

(defn update-in-hero
  "Updates the value of the given key nested inside the hero with the given id. If function-or-value is a value it will be the
   new value, else if it is a function it will be applied on the existing value to produce the new value."
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Jaina Proudmoore" :effects {:frozen true})}])
                    (update-in-hero "h1" [:effects :frozen] false)
                    (get-character "h1")
                    (get-in [:effects :frozen]))
                false))}
  [state id keys function-or-value]
  (let [hero (get-character state id)]
    (replace-hero state (if (function? function-or-value)
                          (update-in hero keys function-or-value)
                          (assoc-in hero keys function-or-value)))))

(defn update-entity
  "Updates the value of the given key for the entity with the given id. If function-or-value is a value it will be the\n
  new value, else if it is a function it will be applied on the existing value to produce the new value."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (update-entity "i" :damage-taken inc)
                    (get-minion "i")
                    (:damage-taken))
                1)
           (is= (-> (create-game)
                    (update-entity "h1" :damage-taken inc)
                    (get-board-entity "h1")
                    (:damage-taken))
                1))}
  [state id key function-or-value]
  (cond
    (minion? state id) (update-minion state id key function-or-value)
    (hero? state id) (update-hero state id key function-or-value)))

(defn get-cards-played-this-turn
  "Get all cards that have been played this turn"
  {:test (fn []
           (is= (-> (create-game)
                    (get-cards-played-this-turn))
                []))}
  [state]
  (:cards-played-this-turn state))

(defn add-to-cards-played-this-turn
  "Add a card to cards played this turn"
  {:test (fn []
           (is= (-> (create-game)
                    (add-to-cards-played-this-turn (create-card "Imp" :id "i"))
                    (get-cards-played-this-turn))
                [(create-card "Imp" :id "i")]))}
  [state card]
  (assoc-in state [:cards-played-this-turn] (conj (:cards-played-this-turn state) card)))

(defn reset-cards-played-this-turn
  "Reset which cards have been played this turn"
  {:test (fn []
           (is= (-> (create-game [] :cards-played-this-turn ["c"])
                    (reset-cards-played-this-turn)
                    (get-cards-played-this-turn)
                    (count))
                0))}
  [state]
  (assoc state :cards-played-this-turn []))

(defn add-buff
  "Adds buff to the given character"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (add-buff "i" {:frozen true})
                    (get-minion "i")
                    :buffs)
                [{:frozen true}])
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i" :buffs [{:frozen true}])]}])
                    (add-buff "i" {:extra-attack 1
                                   :extra-health 1})
                    (get-minion "i")
                    (:buffs)
                    (count))
                2))}
  [state id buff]
  (update-entity state id :buffs (fn [buffs] (conj buffs buff))))

(defn remove-buffs
  "Remove buffs with the given effect key."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i" :buffs [{:frozen true} {:extra-attack 1}])]}])
                    (remove-buffs "i" :frozen)
                    (get-minion "i")
                    (:buffs))
                '({:extra-attack 1})))}
  [state id effect]
  (update-entity state id :buffs (fn [buffs]
                                   (filter (fn [b]
                                             (not (contains? b effect)))
                                           buffs))))

(defn decrement-buff-counters
  "Decrement all buff counters and remove the buff if the counter is 0."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i" :buffs [{:extra-attack 1}
                                                                                  {:extra-health 2 :counter 1}
                                                                                  {:extra-attack 5 :counter 2}])]}])
                    (decrement-buff-counters)
                    (get-minion "i")
                    (:buffs))
                [{:extra-attack 1}
                 {:extra-attack 5 :counter 1}]))}
  [state]
  (reduce (fn [state id]
            (update-entity state id :buffs (fn [buffs]
                                             (->> (map (fn [b]
                                                         (if (contains? b :counter)
                                                           (update b :counter dec)
                                                           b))
                                                       buffs)
                                                  (remove (fn [b] (and (:counter b)
                                                                       (< (:counter b) 1))))))))
          state (map :id (concat (get-minions state)
                                 (get-heroes state)))))


(defn get-character-buffs
  "Gets the buffs vector from the given character."
  {:test (fn []
           (is= (-> (create-minion "Imp" :buffs [{:extra-attack 1 :extra-health 1}])
                    (get-character-buffs))
                [{:extra-attack 1
                  :extra-health 1}])
           (is= (-> (create-hero "Rexxar" :buffs [{:frozen true}])
                    (get-character-buffs))
                [{:frozen true}]))}
  ([character]
   (:buffs character))
  ([state id]
   (get-character-buffs (get-minion state id))))

(defn get-secret-triggered-effect
  "Gets the triggered effect from the given secret."
  {:test (fn []
           (is= (as-> (create-game [{:secrets [(create-secret "Snake Trap" :id "s")]
                                     :minions [(create-minion "Imp" :id "imp")]}]) $
                      ((:on-attack (get-secret-triggered-effect $ "s")) $ "s" ["imp"])
                      (get-minions $ "p1")
                      (count $))
                4))}
  ([secret]
   (:triggered-effect (get-definition secret)))
  ([state id]
   (get-secret-triggered-effect (get-secret state id)))
  )

(defn get-minion-triggered-effect
  "Gets the triggered effect from the given minion."
  {:test (fn []
           (is= (as-> (create-game [{:minions [(create-minion "Acolyte of Pain" :id "ap")]
                                     :deck    ["Imp"]}]) $
                      ((:on-damage (get-minion-triggered-effect $ "ap")) $ "ap" ["ap"])
                      (get-hand $ "p1")
                      (count $))
                1))}
  ([minion]
   (:triggered-effect (get-definition minion)))
  ([state id]
   (get-minion-triggered-effect (get-minion state id)))
  )

(defn get-entity-triggered-effect
  "Gets the triggered effect from the given entity."
  {:test (fn []
           ; Minion
           (is= (as-> (create-game [{:minions [(create-minion "Acolyte of Pain" :id "ap")]
                                     :deck    ["Imp"]}]) $
                      ((:on-damage (get-entity-triggered-effect $ "ap")) $ "ap" ["ap"])
                      (get-hand $ "p1")
                      (count $))
                1)
           ; Secret
           (is= (as-> (create-game [{:secrets [(create-secret "Snake Trap" :id "s")]
                                     :minions [(create-minion "Imp" :id "imp")]}]) $
                      ((:on-attack (get-entity-triggered-effect $ "s")) $ "s" ["imp"])
                      (get-minions $ "p1")
                      (count $))
                4))}
  ([entity]
   (cond
     (minion? entity) (get-minion-triggered-effect entity)
     (secret? entity) (get-secret-triggered-effect entity)))
  ([state id]
   (get-entity-triggered-effect (get-board-entity state id))))

(defn get-triggered-effects
  "Gets all the active triggered effects with given trigger."
  {:test (fn []
           (let [state (create-game [{:secrets [(create-secret "Snake Trap" :id "s")]
                                      :minions [(create-minion "Acolyte of Pain" :id "ap")
                                                (create-minion "Frothing Berserker" :id "fb")]
                                      :deck    ["Imp"]}])
                 on-damage-effects (get-triggered-effects state :on-damage)
                 on-attack-effects (get-triggered-effects state :on-attack)]
             (as-> ((get on-damage-effects "ap") state "ap" ["ap"]) $
                   ((get on-damage-effects "fb") $ "fb" ["ap"])
                   ((get on-attack-effects "s") $ "s" ["ap"])
                   (do (is= (count (get-minions $ "p1")) 5) ; p1 summons 3 snakes from Snake Trap effect
                       (is= (get-character-buffs $ "fb") [{:extra-attack 1}]) ; Frothing Berseker gets +1 attack buff
                       (is= (count (get-hand $ "p1")) 1)))))} ; p1 draws one card from Acolyte of Pain effect
  [state trigger]
  {:pre [(keyword? trigger)]}
  (->> (concat (get-minions state) (get-secrets state))
       (reduce (fn [m e]
                 (let [triggered-effect (get-entity-triggered-effect e)]
                   (if (contains? triggered-effect trigger)
                     (assoc m (:id e) (trigger triggered-effect))
                     m)))
               {})))

(defn remove-minion
  "Removes a minion with the given id from the state."
  {:test (fn []
           (as-> (create-game [{:minions [(create-minion "War Golem" :id "wg" :position 0)
                                          (create-minion "Imp" :id "imp" :position 1)]}]) $
                 (remove-minion $ "wg")
                 (do (is= (->> (get-minions $)
                               (map :name))
                          ["Imp"])
                     (is= (->> (get-minions $)
                               (map :position))
                          [0]))))}
  [state id]
  (let [minion (get-minion state id)
        owner-id (:owner-id minion)
        position (:position minion)
        state (-> (update-in state
                             [:players owner-id :minions]
                             (fn [minions]
                               (remove (fn [m] (= (:id m) id)) minions))))]
    (->> (get-minions state owner-id)
         (map :id)
         (reduce (fn [state id]
                   (update-minion state id :position (fn [p]
                                                       (if (> p position)
                                                         (dec p)
                                                         p))))
                 state))))

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

(defn remove-card-from-hand
  "Removes a card with the given id from the given player's hand."
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "Imp" :id "i")]}])
                    (remove-card-from-hand "p1" "i")
                    (get-hand "p1"))
                []))}
  [state player-id id]
  (update-in state [:players player-id :hand]
             (fn [cards]
               (remove (fn [c] (= (:id c) id)) cards))))

(defn remove-secret
  "Removes the secret with the given id from the given player."
  {:test (fn []
           (is= (-> (create-game [{:secrets [(create-secret "Snake Trap" :id "s")]}])
                    (remove-secret "p1" "s")
                    (get-secrets))
                []))}
  [state player-id id]
  (update-in state [:players player-id :secrets]
             (fn [secret]
               (remove (fn [s] (= (:id s) id)) secret))))

(defn remove-secrets
  "Removes all secrets from a player."
  {:test (fn []
           (is= (-> (create-game [{:secrets ["Snake Trap" "Snake Trap"]}])
                    (remove-secrets "p1")
                    (get-secrets))
                []))}
  [state player-id]
  (assoc-in state [:players player-id :secrets] []))

(defn get-seed
  "Returns the seed of the state."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (get-seed))
                0))}
  [state]
  (:seed state))

(defn set-seed
  "Set the seed of the state."
  {:test (fn []
           (is= (-> (create-empty-state)
                    (set-seed 5)
                    (get-seed))
                5))}
  [state seed]
  (assoc state :seed seed))

(defn get-position
  "Returns the position of a minion."
  {:test (fn []
           (is= (as-> (create-game [{:minions ["Imp" "Imp"]}]) $
                      (first (add-minion-to-board $ {:player-id "p1" :minion (create-minion "War Golem" :id "wg") :position 1}))
                      (get-position $ "wg"))
                1))}
  ([minion]
   (:position minion))
  ([state id]
    (get-position (get-minion state id))))


(defn get-card-duplicate
  "Creates a card duplicate from the given id."
  {:test (fn []
           (let [state (create-game [{:secrets [(create-secret "Snake Trap" :id "s")]
                                      :minions [(create-minion "Acolyte of Pain" :id "ap")
                                                (create-minion "Frothing Berserker" :id "fb")]
                                      :deck    [(create-card "Imp" :id "i")]}])]
             (is (card? (get-card-duplicate state "s")))
             (is-not (= (:id (get-entity state "i"))
                        (:id (get-card-duplicate state "i"))))
             (is= (:name (get-entity state "ap"))
                  (:name (get-card-duplicate state "ap")))))}
  ([entity]
   {:pre (map? entity)}
   (create-card (:name entity)))
  ([state id]
   (get-card-duplicate (get-entity state id))))