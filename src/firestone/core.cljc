(ns firestone.core
  (:require [clojure.test :refer [function?]]
            [ysera.test :refer [is is-not is= error?]]
            [ysera.collections :refer [seq-contains?]]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-card
                                         create-game
                                         create-hero
                                         create-hero-power
                                         create-minion
                                         get-deck
                                         get-cards-from-deck
                                         remove-card-from-deck
                                         fatigue-damage
                                         get-hand
                                         get-player-id-in-turn
                                         add-card-to-hand
                                         get-hero-id
                                         get-player
                                         get-heroes
                                         get-hero-powers
                                         get-minion
                                         get-minions
                                         update-minion
                                         remove-minion
                                         update-hero
                                         update-in-hero
                                         get-hero
                                         get-hero-power
                                         get-hero-power-of-player
                                         get-character
                                         get-board-entity
                                         get-mana
                                         add-minion-to-board
                                         add-secret-to-player
                                         get-character-buffs
                                         get-card-from-hand
                                         get-secrets
                                         create-secret
                                         opposing-player-id
                                         remove-buffs
                                         get-triggered-effects
                                         hero?]]))

(defn get-extra-health
  "Returns the extra attack from buffs of the character."
  {:test (fn []
           (is= (-> (create-minion "Imp" :buffs [{:extra-health 1}
                                                 {:extra-health 2
                                                  :extra-attack 1}])
                    (get-extra-health))
                3))}
  ([character]
   (->> (get-character-buffs character)
        (reduce (fn [a b]
                  (if (contains? b :extra-health)
                    (+ a (:extra-health b))
                    a))
                0)))
  ([state id]
   (get-extra-health (get-board-entity state id))))

(defn get-extra-attack
  "Returns the extra attack from buffs of the character."
  {:test (fn []
           (is= (-> (create-minion "Imp" :buffs [{:extra-attack 2}
                                                 {:extra-health 2
                                                  :extra-attack 1}])
                    (get-extra-attack))
                3))}
  ([character]
   (->> (get-character-buffs character)
        (reduce (fn [a b]
                  (if (contains? b :extra-attack)
                    (+ a (:extra-attack b))
                    a))
                0)))
  ([state id]
   (get-extra-health (get-board-entity state id))))

(defn get-health
  "Returns the health of the character."
  {:test (fn []
           ; The health of minions
           (is= (get-health (create-minion "War Golem")) 7)
           (is= (get-health (create-minion "War Golem" :damage-taken 2)) 5)
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (get-health "i"))
                1)
           ; The health of heroes
           (is= (get-health (create-hero "Jaina Proudmoore")) 30)
           (is= (get-health (create-hero "Jaina Proudmoore" :damage-taken 2)) 28)
           (is= (-> (create-game [{:hero (create-hero "Jaina Proudmoore" :id "h1")}])
                    (get-health "h1"))
                30)
           ; The health of minions with extra-health
           (is= (get-health (create-minion "War Golem" :buffs [{:extra-health 3}])) 10))}
  ([character]
   {:pre [(map? character) (contains? character :damage-taken)]}
   (let [definition (get-definition character)]
     (- (+ (:health definition) (get-extra-health character))
        (:damage-taken character))))
  ([state id]
   (get-health (get-board-entity state id))))

(defn get-attack
  "Returns the attack of the minion with the given id."
  {:test (fn []
           (is= (get-attack (create-minion "Imp")) 1)
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (get-attack "i"))
                1)
           ; Minion with extra-attack effect
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg" :buffs [{:extra-attack 2}])]}])
                    (get-attack "wg"))
                9)
           ; Minion cannot have negative attack
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i" :buffs [{:extra-attack -2}])]}])
                    (get-attack "i"))
                0))}
  ([character]
   (let [definition (get-definition (:name character))]
     (max 0 (+ (:attack definition) (get-extra-attack character)))))
  ([state id]
   (get-attack (get-board-entity state id))))

(defn get-cost
  "Returns the cost of the card or hero power."
  {:test (fn []
           (is= (-> (create-card "Imp" :id "i")
                    (get-cost))
                1)
           (is= (get-cost "Imp") 1)
           (is= (-> (create-card "Dalaran Mage" :id "i")
                    (get-cost))
                3)
           (is= (-> (create-game [{:hand [(create-card "Dalaran Mage" :id "dm")]}])
                    (get-cost "dm"))
                3)
           (is= (-> (create-game)
                    (get-cost "hp1"))
                2))}
  ([source]
   (get (get-definition (if (string? source)
                          source
                          (:name source))) :mana-cost))
  ([state entity-id]
   (get-cost (or (get-card-from-hand state entity-id)
                 (first (filter (fn [hp] (= (:id hp) entity-id)) (get-hero-powers state)))))))

(defn get-entity-type
  "Returns the type of the given id or entity."
  {:test (fn []
           (is= (-> (create-card "Imp" :id "i")
                    (get-entity-type))
                :minion)
           (is= (-> (create-card "Bananas" :id "i")
                    (get-entity-type))
                :spell)
           (is= (-> (create-game [{:hand [(create-card "Dalaran Mage" :id "dm")]}])
                    (get-entity-type "dm"))
                :minion))}
  ([entity]
   (get (get-definition (:name entity)) :type))
  ([state entity-id]
   (get-entity-type (or (get-card-from-hand state entity-id)
                        (first (filter (fn [hp] (= (:id hp) entity-id)) (get-hero-powers state)))))))

(defn get-target-type
  "Returns the target type of the card or hero power with the given id or entity."
  {:test (fn []
           (is= (-> (create-card "Imp" :id "i")
                    (get-target-type))
                nil)
           (is= (-> (create-card "Bananas" :id "i")
                    (get-target-type))
                :all-minions)
           (is= (-> (create-game [{:hand [(create-card "Mind Control" :id "dm")]}])
                    (get-target-type "dm"))
                :enemy-minions)
           (is= (-> (create-game)
                    (get-target-type "hp1"))
                :all))}
  ([entity]
   (get (get-definition (:name entity)) :target-type))
  ([state entity-id]
   (get-target-type (or (get-card-from-hand state entity-id)
                        (first (filter (fn [hp] (= (:id hp) entity-id)) (get-hero-powers state)))))))

(defn get-owner
  "Returns the player-id of the owner of the card, hero-power, character or secret with the given id."
  {:test (fn []
           ; Get owner of hero
           (is= (-> (create-game [{:hero (create-hero "Rexxar" :id "h1")}])
                    (get-owner "h1"))
                "p1")
           ; Get owner of minion
           (is= (-> (create-game [{}
                                  {:minions [(create-minion "Imp" :id "imp")]}])
                    (get-owner "imp"))
                "p2")
           ; Get owner of secret
           (is= (-> (create-game [{}
                                  {:secrets [(create-secret "Snake Trap" :id "s")]}])
                    (get-owner "s"))
                "p2")
           ; Get owner of card
           (is= (-> (create-game [{}
                                  {:hand [(create-card "Snake Trap" :id "s")]}])
                    (get-owner "s"))
                "p2")
           ; Get owner of hero-power
           (is= (-> (create-game)
                    (get-owner "hp2"))
                "p2")
           ; Get owner of non-existing character
           (is= (-> (create-game)
                    (get-owner "non-id"))
                nil)
           )}
  ([entity]
   (:owner-id entity))
  ([state id]
   (get-owner (or (get-hero-power state id)
                  (get-card-from-hand state id)
                  (get-board-entity state id)))))


(defn sleepy?
  "Checks if the minion with given id is sleepy."
  {:test (fn []
           (is (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}]
                                :minion-ids-summoned-this-turn ["i"])
                   (sleepy? "i")))
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                       (sleepy? "i"))))}
  [state id]
  (seq-contains? (:minion-ids-summoned-this-turn state) id))

(defn frozen?
  "Checks if the character is frozen."
  {:test (fn []
           (is (-> (create-minion "Imp" :buffs [{:frozen true}])
                   (frozen?)))
           (is (-> (create-hero "Rexxar" :buffs [{:frozen true}])
                   (frozen?)))
           (is-not (-> (create-minion "Imp" :id "i")
                       (frozen?))))}
  ([character]
   (> (->> (get-character-buffs character)
           (filter (fn [b] (:frozen b)))
           (count))
      0))
  ([state id]
   (frozen? (get-board-entity state id))))

(defn deathrattle-minion?
  "Checks if the minion has a deathrattle."
  {:test (fn []
           (is (-> (create-minion "Loot Hoarder")
                   (deathrattle-minion?)))
           (is-not (-> (create-minion "Imp")
                       (deathrattle-minion?))))}
  ([minion]
   (:deathrattle (get-definition minion)))
  ([state id]
   (deathrattle-minion? (get-minion state id))))

(defn hero-power?
  "Checks if the given entity-id is a hero power."
  {:test (fn []
           (is (-> (create-game)
                   (hero-power? "hp1")))
           (is-not (hero-power? (create-card "Imp"))))}
  ([entity]
   (= (:type (get-definition entity)) :hero-power))
  ([state entity-id]
   (let [entity (first (filter (fn [hp] (= (:id hp) entity-id)) (get-hero-powers state)))]
     (if (nil? entity)
       false
       (hero-power? entity)))))

(defn minion-card?
  "Checks if the card or the card with the given id is a minion card."
  {:test (fn []
           (is (minion-card? (create-card "Imp")))
           (is (minion-card? "Imp"))
           (is (-> (create-game [{:hand [(create-card "Imp" :id "i")]}])
                   (minion-card? "i")))
           (is-not (-> (create-game [{:hand [(create-card "Bananas" :id "b")]}])
                       (minion-card? "b"))))}
  ([card]
   (= (:type (get-definition card)) :minion))
  ([state id]
   (let [card (get-card-from-hand state id)]
     (if (nil? card)
       false
       (minion-card? card)))))

(defn spell-card?
  "Checks if the card or the card with the given id is a spell card."
  {:test (fn []
           (is (spell-card? (create-card "Frostbolt")))
           (is (spell-card? "Frostbolt"))
           (is (-> (create-game [{:hand [(create-card "Frostbolt" :id "f")]}])
                   (spell-card? "f")))
           (is-not (-> (create-game [{:hand [(create-card "Imp" :id "i")]}])
                       (spell-card? "i"))))}
  ([card]
   (= (:type (get-definition card)) :spell))
  ([state id]
   (let [card (get-card-from-hand state id)]
     (if (nil? card)
       false
       (spell-card? card)))))

(defn secret-card?
  "Checks if the card or the card with the given id is a secret card."
  {:test (fn []
           (is (secret-card? (create-card "Snake Trap")))
           (is (secret-card? "Snake Trap"))
           (is (-> (create-game [{:hand [(create-card "Snake Trap" :id "s")]}])
                   (secret-card? "s")))
           (is-not (-> (create-game [{:hand [(create-card "Bananas" :id "b")]}])
                       (secret-card? "b"))))}
  ([card]
   (= (:subtype (get-definition card)) :secret))
  ([state id]
   (let [card (get-card-from-hand state id)]
     (if (nil? card)
       false
       (secret-card? card)))))

(defn get-cannot-attack-function
  "Get the cannot attack function in the definition of a card."
  {:test (fn []
           (is (-> (create-game)
                   ((get-cannot-attack-function (create-minion "Ancient Watcher"))))))}
  ([minion]
   (:cannot-attack (get-definition minion)))
  ([state minion-id]
   (get-cannot-attack-function (get-minion state minion-id))))

(defn valid-attack?
  "Checks if the attack is valid."
  {:test (fn []
           ; Should be able to attack an enemy minion
           (is (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                 {:minions [(create-minion "War Golem" :id "wg")]}])
                   (valid-attack? "p1" "i" "wg")))
           ; Should be able to attack an enemy hero
           (is (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                   (valid-attack? "p1" "i" "h2")))
           ; Should not be able to attack your own minions
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i")
                                                (create-minion "War Golem" :id "wg")]}])
                       (valid-attack? "p1" "i" "wg")))
           ; Should not be able to attack if it is not your turn
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                     {:minions [(create-minion "War Golem" :id "wg")]}]
                                    :player-id-in-turn "p2")
                       (valid-attack? "p1" "i" "wg")))
           ; Should not be able to attack if you are sleepy
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                     {:minions [(create-minion "War Golem" :id "wg")]}]
                                    :minion-ids-summoned-this-turn ["i"])
                       (valid-attack? "p1" "i" "wg")))
           ; Should not be able to attack if you already attacked this turn
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i" :attacks-performed-this-turn 1)]}
                                     {:minions [(create-minion "War Golem" :id "wg")]}])
                       (valid-attack? "p1" "i" "wg")))
           ; Should not be able to attack if "cannot-attack" is true
           (is-not (-> (create-game [{:minions [(create-minion "Ancient Watcher" :id "aw")]}
                                     {:minions [(create-minion "War Golem" :id "wg")]}])
                       (valid-attack? "p1" "aw" "wg")))
           ; Should not be able to attack if "frozen" is true
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i" :buffs [{:frozen true}])]}
                                     {:minions [(create-minion "War Golem" :id "wg")]}])
                       (valid-attack? "p1" "i" "wg")))
           ; Should not be able to attack if attacker has 0 attack
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i" :buffs [{:extra-attack -1}])]}])
                       (valid-attack? "p1" "i" "h2"))))}
  [state player-id attacker-id target-id]
  (let [attacker (get-minion state attacker-id)
        target (get-board-entity state target-id)
        cannot-attack-function (get-cannot-attack-function attacker)]
    (and (= (get-player-id-in-turn state) player-id)
         (< (:attacks-performed-this-turn attacker) 1)
         (> (get-attack attacker) 0)
         (not (sleepy? state attacker-id))
         (not= (:owner-id attacker) (:owner-id target))
         (not (if cannot-attack-function (cannot-attack-function state) false))
         (not (frozen? attacker)))))

(defn valid-attacks
  "Get all valid attackers and their valid targets."
  {:test (fn []
           ; Enemy has one minion
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")
                                             (create-minion "Imp" :id "i2")]}
                                  {:minions [(create-minion "Defender" :id "d1")]}])
                    (valid-attacks))
                {"i1" ["d1" "h2"]
                 "i2" ["d1" "h2"]})
           ; Enemy has one minion, one of the player's minions has already attacked
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")
                                             (create-minion "Imp" :id "i2" :attacks-performed-this-turn 1)]}
                                  {:minions [(create-minion "Defender" :id "d1")]}])
                    (valid-attacks))
                {"i1" ["d1" "h2"]})
           ; Enemy has no minion
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]}])
                    (valid-attacks))
                {"i1" ["h2"]})
           ; No minions on the board
           (is= (-> (create-game)
                    (valid-attacks))
                {}))}
  [state]
  (let [player-in-turn (:player-id-in-turn state)]
    (reduce (fn [attacks attacker-id]
              (let [targets (filter (fn [target-id]
                                      (valid-attack? state player-in-turn attacker-id target-id))
                                    (map :id (concat (get-minions state)
                                                     (get-heroes state))))]
                (if (empty? targets)
                  attacks
                  (assoc attacks attacker-id targets))))
            {}
            (map :id (get-minions state player-in-turn)))))

(defn handle-triggers
  "Handle the triggers of multiple event listeners."
  {:test (fn []
           (is= (-> (create-game)
                    (handle-triggers :on-damage))
                (create-game))
           ; Minion with triggered effect
           (is= (-> (create-game [{:deck ["Imp"] :minions [(create-minion "Acolyte of Pain" :id "m1")]}])
                    (handle-triggers :on-damage "m1")
                    (get-hand "p1")
                    (count))
                1)
           ; Secret
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]
                                   :secrets ["Snake Trap"]}])
                    (handle-triggers :on-attack "i")
                    (get-minions "p1")
                    (count))
                4))}
  [state event & args]
  (let [triggered-effects (get-triggered-effects state event)]
    (if (empty? triggered-effects)
      state
      (reduce (fn [state effect]
                ((second effect) state (first effect) args))
              state
              triggered-effects))))

(defn full-board?
  "Checks if a player's board is full."
  {:test (fn []
           (is-not (-> (create-game)
                       (full-board? "p1")))
           (is (-> (create-game [{:minions (repeat 7 "Imp")}])
                   (full-board? "p1")))
           (is-not (-> (create-game [{:minions (repeat 7 "Imp")}])
                       (full-board? "p2"))))}
  [state player-id]
  (>= (count (get-minions state player-id)) 7))

(defn destroy-minion
  "Causes a minion on the board to die. Should trigger deathrattles and other on death effects."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}])
                    (destroy-minion "wg")
                    (get-minions))
                [])
           (is= (-> (create-game [{:minions [(create-minion "Loot Hoarder" :id "lh")] :deck ["Imp"]}])
                    (destroy-minion "lh")
                    (get-hand "p1")
                    (count))
                1))}
  [state id]
  (let [definition (get-definition (get-minion state id))
        deathrattle (:deathrattle definition)
        owner-id (get-owner state id)]
    (if deathrattle
      (-> (remove-minion state id)
          (deathrattle owner-id))
      (remove-minion state id))))

(defn change-minion-board-side
  "Causes a minion on the board to switch board side and owner."
  {:test (fn []
           (is= (as-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}]) $
                      (change-minion-board-side $ "wg")
                      [(get-owner $ "wg")
                       (count (get-minions $ "p1"))
                       (count (get-minions $ "p2"))])
                ["p2" 0 1])
           ; Should only destroy minion if the board to be moved to is full
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}
                                  {:minions (repeat 7 "Imp")}])
                    (change-minion-board-side "wg")
                    (get-minions "p2")
                    (count))
                7))}
  [state id]
  (let [minion (get-minion state id)
        new-owner-id (if (= (get-owner state id) "p1")
                       "p2"
                       "p1")]
    (if (full-board? state new-owner-id)
      (destroy-minion state id)
      (as-> (remove-minion state id) $
          (first (add-minion-to-board $ {:player-id new-owner-id
                                       :minion    minion
                                       :position  0}))))))

(defn damage-minion
  "Deals damage to the minion with the given id."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "i")]}])
                    (damage-minion "i" 3)
                    (get-health "i"))
                (as-> (get-definition "War Golem") $
                      (- ($ :health) 3)))
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "i" :damage-taken 1)]}])
                    (damage-minion "i" 1)
                    (get-health "i"))
                (as-> (get-definition "War Golem") $
                      (- ($ :health) 2)))
           ; Remove minion if dead
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (damage-minion "i" 1)
                    (get-minions))
                []))}
  [state id damage]
  (let [state (update-minion state id :damage-taken (partial + damage))]
    (let [state (handle-triggers state :on-damage id)]
      (if (> (get-health state id) 0)
        state
        (destroy-minion state id)))))

(defn heal-minion
  "Reduces damage taken of a minion by the given amount."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg" :damage-taken 5)]}])
                    (heal-minion "wg" 5)
                    (get-health "wg"))
                7)
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg" :damage-taken 6)]}])
                    (heal-minion "wg" 7)
                    (get-health "wg"))
                7))}
  [state id amount]
  (update-minion state id :damage-taken (fn [x] (max (- x amount) 0))))

(defn damage-hero
  "Deals damage to the hero with the given id."
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Rexxar")}
                                  {:hero (create-hero "Uther Lightbringer")}])
                    (damage-hero "h1" 5)
                    (get-health "h1"))
                (as-> (get-definition "Rexxar") $
                      (- ($ :health) 5)))
           (is= (-> (create-game [{:hero (create-hero "Rexxar" :damage-taken 2)}
                                  {:hero (create-hero "Uther Lightbringer")}])
                    (damage-hero "h1" 5)
                    (get-health "h1"))
                (as-> (get-definition "Rexxar") $
                      (- ($ :health) 7))))}
  [state id damage]
  (let [state (update-hero state id :damage-taken (partial + damage))]
    (if (> (get-health state id) 0)
      state
      ; TODO: game should be over
      state))
  )

(defn heal-hero
  "Reduces damage taken of a hero by the given amount."
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Rexxar" :id "h1" :damage-taken 10)}])
                    (heal-hero "h1" 5)
                    (get-health "h1"))
                25)
           (is= (-> (create-game [{:hero (create-hero "Rexxar" :id "h1" :damage-taken 10)}])
                    (heal-hero "h1" 20)
                    (get-health "h1"))
                30))}
  [state id amount]
  (update-hero state id :damage-taken (fn [x] (max (- x amount) 0))))

(defn summon-minion
  "Plays a minion card-"
  {:test (fn []
           ; Play minion card on empty board
           (let [state (-> (create-game)
                           (summon-minion "p1" (create-card "Imp" :id "c1")))]
             (is= (:minion-ids-summoned-this-turn state) ["m1"])
             (is= (map :name (get-minions state)) ["Imp"]))
           ; Play a minion card on a board with one minion
           (let [state (-> (create-game [{:minions ["War Golem"]}])
                           (summon-minion "p1" (create-card "Imp" :id "c1") 1))]
             (is= (:minion-ids-summoned-this-turn state) ["m2"])
             (is= (map :name (get-minions state)) ["War Golem" "Imp"]))
           ; No state change if board is already full
           (is= (-> (create-game [{:minions (repeat 7 "War Golem")}])
                    (summon-minion "p1" (create-card "Imp" :id "c1")))
                (create-game [{:minions (repeat 7 "War Golem")}])))}
  ([state player-id card position]
   (if-not (full-board? state player-id)
     (let [minion (create-minion (if (string? card)
                                   card
                                   (:name card)))
           [state id] (add-minion-to-board state {:player-id player-id :minion minion :position position})]
       (-> state
           (assoc-in [:minion-ids-summoned-this-turn] (conj (:minion-ids-summoned-this-turn state) id))
           (assoc :event {:name   "minion-summoned"
                          :minion minion})))
     state))
  ([state player-id card]
   (summon-minion state player-id card 0)))

(defn give-card
  "Adds a card to a given players hand, on the condition that it isn't full."
  {:test (fn []
           ; Add a card to an empty players hand
           (is= (-> (create-game)
                    (give-card "p2" (create-card "Imp"))
                    (get-hand "p2")
                    (count))
                1)
           ; Burn a card when the player's hand is full
           (is= (-> (create-game [{:hand [(create-card "Imp") (create-card "Imp") (create-card "Imp") (create-card "Imp")
                                          (create-card "Imp") (create-card "Imp") (create-card "Imp") (create-card "Imp")
                                          (create-card "Imp") (create-card "Imp")]}])
                    (give-card "p1" (create-card "Imp"))
                    (get-hand "p1")
                    (count))
                10))}
  ([state player-id card]
   {:pre [(map? state) (string? player-id)]}
    ; Check if there are cards in the deck
   (if (< (count (get-hand state player-id)) 10)
     (add-card-to-hand state {:player-id player-id :card card})
     state)))

(defn draw-card
  "Draw a card from a player's deck and put it in the hand. This is only done if the hand is not full
  and there are cards in the deck."
  {:test (fn []
           ; Test to draw a card when the player has a card in the deck
           (is= (-> (create-game [{:deck [(create-card "Imp")]}])
                    (draw-card "p1"))
                (create-game [{:hand [(create-card "Imp")]}]))
           ; Test that a player takes fatigue damage if there are no cards in the deck
           (is= (-> (create-game)
                    (draw-card "p1"))
                (create-game [{:fatigue 2 :hero (create-hero "Jaina Proudmoore" :damage-taken 1)}]))
           ; Test that the player takes increased damage when drawing multiple times from an empty deck
           (is= (-> (create-game)
                    (draw-card "p1")
                    (draw-card "p1"))
                (create-game [{:fatigue 3 :hero (create-hero "Jaina Proudmoore" :damage-taken 3)}])))}
  ([state player-id]
   {:pre [(map? state) (string? player-id)]}
    ; Check if there are cards in the deck
   (if (empty? (get-deck state player-id))
     (let [[state damage] (fatigue-damage state player-id)]
       (damage-hero state (get-hero-id state player-id) damage))
     (let [card (first (get-cards-from-deck state player-id 1))]
       (let [state (remove-card-from-deck state player-id (:id card))]
         (give-card state player-id card))))))

(defn mulligan
  "Take x cards from player 1's deck and y cards from player 2's deck. The cards are removed from the
  player's decks and put into their hands."
  {:test (fn []
           ; Test mulligan with the same amount of cards as in the players' decks
           (is= (-> (create-game [{:deck [(create-card "Imp")]}
                                  {:deck [(create-card "Imp")]}])
                    (mulligan 1 1))
                (create-game [{:hand [(create-card "Imp")]}
                              {:hand [(create-card "Imp")]}]))
           ; Test mulligan with more than the amount of cards in the players' decks
           (is= (-> (create-game [{:deck [(create-card "Imp")]}
                                  {:deck [(create-card "Imp")]}])
                    (mulligan 2 2))
                (create-game [{:hand [(create-card "Imp")]}
                              {:hand [(create-card "Imp")]}]))
           ; Test mulligan when the players' decks are empty
           (is= (-> (create-game)
                    (mulligan 1 1))
                (create-game)))}
  ([state x y]
   {:pre [(map? state) (number? x) (number? y)]}
   (reduce (fn [state {player-id :player-id cards :cards}]
             (reduce (fn [state card]
                       (-> (add-card-to-hand state {:player-id player-id :card card})
                           (remove-card-from-deck player-id (:id card))))
                     state
                     cards)
             )
           state
           (map-indexed (fn [index amount]
                          (let [player-id (str "p" (inc index))]
                            {:player-id player-id
                             :cards     (get-cards-from-deck state player-id amount)}))
                        [x y]))))

(defn secret-active?
  "Returns true if the player has an active secret with the given name."
  {:test (fn []
           (is (-> (create-game [{:secrets ["Snake Trap"]}])
                   (secret-active? "p1" "Snake Trap")))
           (is-not (-> (create-game)
                       (secret-active? "p1" "Snake Trap")))
           (is (-> (create-game [{} {:secrets ["Snake Trap"]}])
                   (secret-active? "p2" (create-secret "Snake Trap")))))}
  [state player-id secret]
  (> (->> (get-secrets state player-id)
          (filter (fn [v] (= (:name v) (if (string? secret)
                                         secret
                                         (:name secret)))))
          (count))
     0))

(defn playable?
  "Checks if a card or hero power is playable for a specific player."
  {:test (fn []
           (is (-> (create-game [{:hand [(create-card "Imp" :id "c1")] :max-mana 1}])
                   (playable? "p1" "c1")))
           (is-not (-> (create-game [{:hand [(create-card "Imp" :id "c1")] :max-mana 0}])
                       (playable? "p1" "c1")))
           (is-not (-> (create-game [{:max-mana 5
                                      :hand     [(create-card "Imp" :id "c1")]
                                      :minions  ["War Golem" "War Golem" "War Golem" "War Golem" "War Golem" "War Golem" "War Golem"]}])
                       (playable? "p1" "c1")))
           (is (-> (create-game)
                   (playable? "p1" "hp1")))
           ; Secret not playable if same secret already in play.
           (is-not (-> (create-game [{:hand    [(create-card "Snake Trap" :id "c1")]
                                      :secrets ["Snake Trap"]}])
                       (playable? "p1" "c1"))))}
  [state player-id entity-id]
  (let [available-mana (get-mana state player-id)
        cost (get-cost state entity-id)]
    (and (<= cost available-mana)
         (if (hero-power? state entity-id)
           (not (:used (get-hero-power-of-player state player-id)))
           true)
         (if (minion-card? state entity-id)
           (not (full-board? state player-id))
           true)
         (if (secret-card? state entity-id)
           (not (secret-active? state player-id (get-card-from-hand state entity-id)))
           true))))

(defn spell-with-target?
  "Checks if a card is a spell that requires a target."
  {:test (fn []
           (is-not (-> (create-game [{:hand [(create-card "Imp" :id "i")]}])
                       (spell-with-target? "i")))
           (is-not (-> (create-game [{:hand [(create-card "Snake Trap" :id "s")]}])
                       (spell-with-target? "s")))
           (is (-> (create-game [{:hand [(create-card "Bananas" :id "b1")]}])
                   (spell-with-target? "b1"))))}
  [state card-id]
  (and (= (get-entity-type state card-id) :spell)
       (get-target-type state card-id)))

(defn get-target-condition-function
  "Get the target condition function in the definition of a card or hero power."
  {:test (fn []
           (is (-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}])
                   ((get-target-condition-function (create-card "Big Game Hunter")) "wg")))
           (is (as-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]
                                    :hand    [(create-card "Big Game Hunter" :id "bgh")]}]) $
                     ((get-target-condition-function $ "bgh") $ "wg"))))}
  ([entity]
   (:target-condition (get-definition entity)))
  ([state entity-id]
   (get-target-condition-function (or (get-card-from-hand state entity-id)
                                      (first (filter (fn [hp] (= (:id hp) entity-id)) (get-hero-powers state)))))))

(defn available-targets
  "Takes the id of a card, minion, hero or hero power and returns its valid targets"
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")
                                             (create-minion "Imp" :id "i2")]}
                                  {:minions [(create-minion "Defender" :id "d1")
                                             (create-minion "Defender" :id "d2")]
                                   :hand    [(create-card "Bananas" :id "b1")]}])
                    (available-targets "p2" "b1"))
                ["i1" "i2" "d1" "d2"])
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]}
                                  {:minions [(create-minion "Defender" :id "d1")]
                                   :hand    [(create-card "Mind Control" :id "mc1")]}])
                    (available-targets "p2" "mc1"))
                ["i1"])
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg1") "Imp"]}
                                  {:minions [(create-minion "War Golem" :id "wg2")]
                                   :hand    [(create-card "Big Game Hunter" :id "bgh")]}])
                    (available-targets "p2" "bgh"))
                ["wg1" "wg2"])
           (is= (-> (create-game [{:minions ["Imp"]
                                   :hand    [(create-card "Big Game Hunter" :id "bgh")]}])
                    (available-targets "p1" "bgh"))
                [])
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]
                                   :hand    [(create-card "Frostbolt" :id "f")]}
                                  {:minions [(create-minion "Imp" :id "i2")]}])
                    (available-targets "p1" "f"))
                ["i1" "i2" "h1" "h2"])
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]}
                                  {:minions [(create-minion "Imp" :id "i2")]}])
                    (available-targets "p1" "hp1"))
                ["i1" "i2" "h1" "h2"])
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]}
                                  {:minions [(create-minion "Imp" :id "i2")]}])
                    (available-targets "p1" "i1"))
                ["i2" "h2"])
           ;TODO :O this combination should not be possible. Suggest removing player-id from arguments.
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")
                                             (create-minion "Imp" :id "i2")]}
                                  {:hand [(create-card "Bananas" :id "b1")]}])
                    (available-targets "p1" "b1"))
                []))}
  [state player-id entity-id]
  (let [opp-player-id (opposing-player-id player-id)]
    (if (nil? (get-minion state entity-id))
      (let [target-type (get-target-type state entity-id)
            targets (cond
                      (= target-type :all)
                      (concat (get-minions state)
                              (get-heroes state))

                      (= target-type :all-minions)
                      (get-minions state)

                      (= target-type :enemy-minions)
                      (get-minions state opp-player-id)

                      (= target-type :friendly-minions)
                      (get-minions state player-id)

                      ; TODO: Might need checks for other target-type.
                      :else
                      [])
            targets-ids (map :id targets)]
        (let [target-cond-func (get-target-condition-function state entity-id)]
          (if (nil? target-cond-func)
            targets-ids
            (filter (fn [target-id] (target-cond-func state target-id)) targets-ids))))
      (get (valid-attacks state) entity-id))))

(defn valid-play?
  "Determines if a card is playable (with a valid target if needed), or if a minion can attack an other one."
  {:test (fn []
           (let [state (create-game [{:minions [(create-minion "Imp" :id "i1")
                                                (create-minion "War Golem" :id "wg1")]
                                      :hand    [(create-card "Bananas" :id "b1")
                                                (create-card "Snake Trap" :id "st")
                                                (create-card "Mind Control" :id "mc1")
                                                (create-card "Imp" :id "i3")
                                                (create-card "Big Game Hunter" :id "bgh1")]}
                                     {:minions [(create-minion "Defender" :id "d1")
                                                (create-minion "Defender" :id "d2")]}])]
             (is (valid-play? state "bgh1" "wg1"))          ; Battlecry target targets the right target (tongue twister)
             (is-not (valid-play? state "bgh1" "i1"))       ; Battlecry target targets the wrong target
             (is-not (valid-play? state "bgh1"))            ; Battlecry target required
             (is (valid-play? state "b1" "wg1"))            ; Spell target
             (is (valid-play? state "mc1" "d2"))            ; Target opposing minion
             (is-not (valid-play? state "mc1" "i1"))        ; Non-valid target
             (is (valid-play? state "i3"))                  ; Play a minion card
             (is-not (valid-play? state "i3" "d2"))         ; Imps don't have a battlecry target
             (is-not (valid-play? state "b1"))              ; Banana spell needs a target
             (is-not (valid-play? state "hp1"))             ; Fireblast needs target (hero power)
             (is (valid-play? state "hp1" "d1"))            ; Fireblast targets minion
             (is-not (valid-play? state "hp1" "st"))
             )
           ; Play minion
           (is (-> (create-game [{:hand [(create-card "War Golem" :id "wg")]}])
                   (valid-play? "wg")))
           ; Play battlecry minion when there is an available target
           (is (-> (create-game [{:hand [(create-card "Big Game Hunter" :id "bgh")]}
                                 {:minions [(create-minion "War Golem" :id "wg")]}])
                   (valid-play? "bgh" "wg")))
           ; Play battlecry minion when there are no available targets
           (let [state (create-game [{:hand [(create-card "Big Game Hunter" :id "bgh")
                                             (create-card "Bananas" :id "b1")]}])]
             (is (valid-play? state "bgh"))
             (is-not (valid-play? state "b1")))
           ; Cannot play a secret card if that secret it already in play.
           (is-not (-> (create-game [{:secrets [(create-card "Snake Trap" :id "s1")]
                                      :hand    [(create-card "Snake Trap" :id "s2")]}])
                       (valid-play? "s2")))
           )}                                               ; Cannot target cards
  ([state entity-id & [target-id]]
   (let [player-in-turn (get-player-id-in-turn state)
         targets (available-targets state player-in-turn entity-id)]
     (if (playable? state player-in-turn entity-id)
       (if target-id
         (if (empty? targets)
           false
           (some (fn [x] (= target-id x)) targets))         ; Is the target in targets ?
         (if (spell-with-target? state entity-id)
           false
           (empty? targets)))
       false)))
  ([state entity-id]
   (valid-play? state entity-id nil)))

(defn valid-plays
  "Get all playable cards and hero powers and their valid targets."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")
                                             (create-minion "War Golem" :id "wg1")]
                                   :hand    [(create-card "Bananas" :id "b1")
                                             (create-card "Snake Trap" :id "st")
                                             (create-card "Mind Control" :id "mc1")
                                             (create-card "Imp" :id "i3")
                                             (create-card "Big Game Hunter" :id "bgh1")]}
                                  {:minions [(create-minion "Defender" :id "d1")
                                             (create-minion "Defender" :id "d2")]}])
                    (valid-plays))
                {"b1"   ["i1" "wg1" "d1" "d2"]
                 "mc1"  ["d1" "d2"]
                 "i3"   []
                 "st"   []
                 "bgh1" ["wg1"]
                 "hp1"  ["i1" "wg1" "d1" "d2" "h1" "h2"]})
           (is= (-> (create-game [{:hero "Uther Lightbringer"
                                   :hand ["Bananas"
                                          (create-card "Big Game Hunter" :id "bgh")
                                          (create-card "Frostbolt" :id "f")]}])
                    (valid-plays))
                {"bgh" []
                 "f"   ["h1" "h2"]
                 "hp1" []})

           ; Cannot play a secret card if that secret it already in play.
           (is= (-> (create-game [{:secrets ["Snake Trap"] :hand ["Snake Trap"]}])
                    (valid-plays))
                {"hp1" ["h1" "h2"]}))}
  [state]
  (reduce (fn [plays entity-id]
            (let [targets (filter (fn [target-id]
                                    (valid-play? state entity-id target-id))
                                  (map :id (concat (get-minions state)
                                                   (get-heroes state))))]
              (if (empty? targets)
                (if (valid-play? state entity-id)
                  (assoc plays entity-id [])
                  plays)
                (assoc plays entity-id targets))))
          {}
          (map :id (conj (get-hand state (get-player-id-in-turn state))
                         (get-hero-power-of-player state (get-player-id-in-turn state))))))

(defn play-secret
  "Puts a secret into play if there is space."
  {:test (fn []
           (is= (-> (create-game)
                    (play-secret "p1" (create-secret "Snake Trap"))
                    (get-secrets)
                    (count))
                1)
           ; Cannot have more than 5 secrets in play (need more secrets for better test)
           (is= (-> (create-game [{:secrets ["Snake Trap" "Snake Trap" "Snake Trap" "Snake Trap" "Snake Trap"]}])
                    (play-secret "p1" (create-secret "Snake Trap"))
                    (get-secrets)
                    (count))
                5)
           ; Can only have one of each secret
           (is= (-> (create-game [{:secrets ["Snake Trap"]}])
                    (play-secret "p1" (create-secret "Snake Trap"))
                    (get-secrets)
                    (count))
                1))}
  [state player-id secret]
  (if (and (not (secret-active? state player-id secret))
           (< (count (get-secrets state player-id)) 5))
    (add-secret-to-player state player-id secret)
    state))

(defn get-spell-function
  "Get the spell function in the definition of a card."
  {:test (fn []
           (is= (as-> (create-game [{:minions [(create-minion "Imp" :id "i1")]}]) $
                      ((get-spell-function (create-card "Bananas")) $ "i1")
                      [(get-attack $ "i1") (get-health $ "i1")])
                [2 2])
           (is= (as-> (create-game []) $
                      ((get-spell-function (create-card "Snake Trap")) $)
                      (get-secrets $ "p1")
                      (count $))
                1))}
  [card]
  (if (= (:subtype card) :secret)
    (fn [state] (play-secret state (get-player-id-in-turn state) (create-secret (:name card))))
    (:spell (get-definition card))))

(defn get-hero-power-function
  "Get the hero power function in the definition of the hero power."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]}])
                    ((get-hero-power-function (create-hero-power "Fireblast")) "i1")
                    (get-minions)
                    (count))
                0)
           (is= (-> (create-game)
                    ((get-hero-power-function (create-hero-power "Reinforce")))
                    (get-minions)
                    (count))
                1))}
  [hero-power]
  (:power (get-definition hero-power)))

(defn get-battlecry-function
  "Get the battlecry function in the definition of a card."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}])
                    ((get-battlecry-function (create-card "Big Game Hunter")) "m1" "wg")
                    (get-minions)
                    (count))
                0))}
  ([card]
   (:battlecry (get-definition card)))
  ([state card-id]
   (get-battlecry-function (get-card-from-hand state card-id))))

(defn battlecry-minion-with-target?
  "Checks if a card is a battlecry minion that requires a target to execute the battlecry."
  {:test (fn []
           ; True for a minion with battlecry in need of a target
           (is (-> (create-game [{:hand [(create-card "Big Game Hunter" :id "bgh")]}])
                   (battlecry-minion-with-target? "bgh")))
           ; False for a minion with battlecry that does not need a target
           (is-not (-> (create-game [{:hand [(create-card "Eater of Secrets" :id "es")]}])
                       (battlecry-minion-with-target? "es")))
           ; False for a minion without battlecry
           (is-not (-> (create-game [{:hand [(create-card "Imp" :id "i")]}])
                       (battlecry-minion-with-target? "i"))))}
  ([state card-id]
   (if (and (= (get-entity-type state card-id) :minion)
            (not (nil? (get-battlecry-function state card-id)))
            (get-target-type state card-id))
     true
     false))
  ([card]
   (if (and (= (get-entity-type card) :minion)
            (not (nil? (get-battlecry-function card)))
            (get-target-type card))
     true
     false)))

(defn consume-mana
  "Consume a given amount of a player's mana."
  {:test (fn []
           (is= (-> (create-game [{:used-mana 2}])
                    (consume-mana "p1" 5)
                    (get-mana "p1"))
                3))}
  [state player-id amount]
  (update-in state [:players player-id :used-mana] (partial + amount)))

(defn restore-mana
  "resets the consumed amount of a player's mana."
  {:test (fn []
           (is= (-> (create-game [{:used-mana 3}])
                    (restore-mana "p1")
                    (get-mana "p1"))
                10))}
  [state player-id]
  (assoc-in state [:players player-id :used-mana] 0))

(defn add-to-max-mana
  "Adds a given amount of mana to the max-mana pool of a player (doesn't affect consumed mana)"
  {:test (fn []
           (is= (-> (create-game [{:max-mana 3}])
                    (add-to-max-mana "p1" 2)
                    (get-mana "p1"))
                5)
           (is= (-> (create-game)
                    (add-to-max-mana "p1" 2)
                    (get-mana "p1"))
                10))}
  [state player-id amount]
  (let [max-mana (get-in state [:players player-id :max-mana])]
    (assoc-in state [:players player-id :max-mana] (+ max-mana (min (- 10 max-mana) amount)))))

(defn get-spell-damage
  "Returns the spell damage of a minion."
  {:test (fn []
           ; Minion with spell damage in definition
           (is= (-> (create-minion "Dalaran Mage")
                    (get-spell-damage))
                1)
           ; Minions with spell damage in definition and buff
           (is= (-> (create-minion "Dalaran Mage" :buffs [{:spell-damage 2}])
                    (get-spell-damage))
                3)
           ; Minion without spell damage
           (is= (-> (create-minion "Imp")
                    (get-spell-damage))
                0))}
  ([minion]
   (let [definition-spell-damage (or (:spell-damage (get-definition minion))
                                     0)
         buff-spell-damage (->> (get-character-buffs minion)
                                (reduce (fn [a b]
                                          (if (contains? b :spell-damage)
                                            (+ a (:spell-damage b))
                                            a))
                                        0))]
     (+ definition-spell-damage buff-spell-damage)))
  ([state id]
   (get-spell-damage (get-minion state id))))

(defn get-player-spell-damage
  "Returns the total spell-damage a player has"
  {:test (fn []
           ; Both Dalaran Mage and Ogre Magi have spell-damage effects, so we test a player with 2 spell-damage and another with none
           (is= (as-> (create-game [{:minions [(create-minion "Dalaran Mage") (create-minion "Ogre Magi")]}]) $
                      [(get-player-spell-damage $ "p1") (get-player-spell-damage $ "p2")])
                [2 0])
           )}
  [state player-id]
  (reduce + (->> (get-minions state player-id)
                 (map (fn [m] (get-spell-damage m))))))

(defn deal-spell-damage
  "Deals damage to a character taking into account the spell-damage"
  {:test (fn []
           ; Hero has {:spell-damage 2} deals spell damage to opposing hero
           (is= (-> (create-game [{:hero (create-hero "Uther Lightbringer") :minions [(create-minion "Dalaran Mage") (create-minion "Ogre Magi")]}
                                  {:hero (create-hero "Jaina Proudmoore")}])
                    (deal-spell-damage "h1" 3)
                    (get-health "h1"))
                (as-> (get-definition "Jaina Proudmoore") $
                      (- ($ :health) 5)))
           ; Hero has {:spell-damage 2} deals spell damage to opposing minoin
           (is= (-> (create-game [{:hero (create-hero "Uther Lightbringer") :minions [(create-minion "Dalaran Mage") (create-minion "Ogre Magi")]}
                                  {:hero (create-hero "Jaina Proudmoore") :minions [(create-minion "War Golem" :id "wg")]}])
                    (deal-spell-damage "wg" 3)
                    (get-health "wg"))
                (as-> (get-definition "War Golem") $
                      (- ($ :health) 5))))}
  [state target-id damage]
  (let [total-damage (+ damage (get-player-spell-damage state (get-player-id-in-turn state)))
        target (get-character state target-id)]
    (if (= (:entity-type target) :hero)
      (damage-hero state target-id total-damage)
      (damage-minion state target-id total-damage))))

(defn reset-minion-attack-this-turn
  "Resets :attack-this-turn back to 0 for all minions of a given player"
  {:test (fn []
           (is= (-> {:players {"p1" {:minions [(create-minion "Imp" :attacks-performed-this-turn 1)
                                               (create-minion "Ogre Magi" :attacks-performed-this-turn 1)]}}}
                    (reset-minion-attack-this-turn "p1"))
                {:players {"p1" {:minions [(create-minion "Imp")
                                           (create-minion "Ogre Magi")]}}}))}
  [state player-id]
  (assoc-in state [:players player-id :minions]
            (map (fn [minion] (assoc minion :attacks-performed-this-turn 0))
                 (get-minions state player-id))))

(defn unfreeze-characters
  "Unfreezes all characters of the player in turn that are Frozen if the conditions are met"
  {:test (fn []
           ; Minion frozen and didn't attack yet => should be unfrozen
           (is-not (-> (create-game [{:minions [(create-minion "Imp"
                                                               :id "m1"
                                                               :attacks-performed-this-turn 0
                                                               :buffs [{:frozen true}])]}])
                       (unfreeze-characters)
                       (get-minion "m1")
                       (frozen?)))
           ; Minion frozen and already attacked => should stay frozen
           (is (-> (create-game [{:minions [(create-minion "Imp"
                                                           :id "m1"
                                                           :attacks-performed-this-turn 1
                                                           :buffs [{:frozen true}])]}])
                   (unfreeze-characters)
                   (get-minion "m1")
                   (frozen?)))
           ; Hero frozen and didn't attack yet => should be unfrozen
           (is-not (-> (create-game [{:hero (create-hero "Jaina Proudmoore"
                                                         :buffs [{:frozen true}])}])
                       (unfreeze-characters)
                       (get-hero "p1")
                       (frozen?)))
           ; Hero frozen and already attacked => should still be frozen
           (is (-> (create-game [{:attacks-performed-this-turn 1
                                  :hero                        (create-hero "Jaina Proudmoore"
                                                                            :buffs [{:frozen true}])}])
                   (unfreeze-characters)
                   (get-hero "p1")
                   (frozen?))))}
  [state]
  (let [player (get-player state (get state :player-id-in-turn))]
    ; on minions
    (as-> (conj (get-minions state (:id player)) (get-hero state (:id player))) $
          (reduce (fn [state character]
                    (let [has-attacked (> (:attacks-performed-this-turn (if (hero? character)
                                                                          player
                                                                          character))
                                          0)]
                      (if (and (frozen? character) (not has-attacked))
                        (remove-buffs state (:id character) :frozen)
                        state)))
                  state $))))