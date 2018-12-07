(ns firestone.definition.card
  (:require [firestone.definitions :as definitions]
            [firestone.definitions :refer [get-definitions]]
            [clojure.test :refer [function?]]
            [ysera.test :refer [is is-not is= error?]]
            [ysera.random :refer [random-nth]]
            [firestone.construct :refer [create-game
                                         create-minion
                                         create-card
                                         create-secret
                                         update-minion
                                         update-in-hero
                                         minion?
                                         get-character
                                         get-minion
                                         get-minions
                                         get-secrets
                                         remove-secret
                                         remove-secrets
                                         get-hero
                                         get-damage
                                         get-player-id-in-turn
                                         opposing-player-id
                                         add-card-to-hand
                                         get-hand
                                         get-mana
                                         get-board-entity
                                         get-hero-id
                                         get-player
                                         get-seed
                                         remove-minion
                                         set-seed
                                         get-position
                                         add-buff
                                         hero?
                                         get-cards-played-this-turn
                                         get-player-id-in-turn
                                         remove-card-from-hand
                                         add-extra-mana]]
            [firestone.core :refer [change-minion-board-side
                                    get-owner
                                    get-attack
                                    heal-hero
                                    get-health
                                    summon-minion
                                    draw-card
                                    damage-minion
                                    damage-hero
                                    valid-plays
                                    destroy-minion
                                    valid-attack?
                                    give-card
                                    minion-card?
                                    add-to-max-mana
                                    deal-spell-damage
                                    spell-card?]]
            [firestone.api :refer [attack-with-minion
                                   play-minion-card
                                   end-turn]]))

(def card-definitions
  {

   "Alarm-o-Bot"
   {:name             "Alarm-o-Bot"
    :mana-cost        3
    :health           3
    :attack           0
    :type             :minion
    :rarity           :rare
    :race             :mech
    :set              :classic
    :description      "At the start of your turn, swap this minion with a random one in your hand."
    :triggered-effect {:on-start-turn (fn [state this _]
                                        (let [owner-id (get-owner state this)
                                              minion-cards-in-hand (->> (get-hand state owner-id)
                                                                        (filter (fn [c] (minion-card? c))))]
                                          (if (and (= owner-id (get-player-id-in-turn state))
                                                (> (count minion-cards-in-hand) 0))
                                            (let [[seed random-minion-card] (random-nth (get-seed state) minion-cards-in-hand)
                                                  position (get-position state this)]
                                              (-> (set-seed state seed)
                                                  (remove-minion this)
                                                  (summon-minion owner-id random-minion-card position)
                                                  (remove-card-from-hand owner-id (:id random-minion-card))
                                                  (give-card owner-id (create-card "Alarm-o-Bot"))))
                                            state)))}}

   "Dalaran Mage"
   {:name         "Dalaran Mage"
    :mana-cost    3
    :health       4
    :attack       1
    :type         :minion
    :set          :basic
    :rarity       :none
    :description  "Spell Damage +1"
    :spell-damage 1}

   "Defender"
   {:name      "Defender"
    :attack    2
    :health    1
    :mana-cost 1
    :set       :classic
    :class     :paladin
    :type      :minion
    :rarity    :common}

   "Silver Hand Recruit"
   {:name      "Silver Hand Recruit"
    :attack    1
    :health    1
    :mana-cost 1
    :set       :classic
    :class     :paladin
    :type      :minion
    :rarity    :none}

   "Imp"
   {:name      "Imp"
    :attack    1
    :health    1
    :mana-cost 1
    :rarity    :common
    :set       :classic
    :type      :minion
    :race      :demon}

   "Ogre Magi"
   {:name         "Ogre Magi"
    :attack       4
    :health       4
    :mana-cost    4
    :spell-damage 1
    :type         :minion
    :set          :basic
    :description  "Spell Damage +1"}

   "War Golem"
   {:name      "War Golem"
    :attack    7
    :health    7
    :mana-cost 7
    :type      :minion
    :set       :basic
    :rarity    :none}

   "Big Game Hunter"
   {:name             "Big Game Hunter"
    :attack           4
    :health           2
    :mana-cost        5
    :type             :minion
    :set              :classic
    :rarity           :epic
    :description      "Battlecry: Destroy a minion with an Attack of 7 or more."
    :target-type      :all-minions
    :target-condition (defn attack-seven-or-more?
                        {:test (fn []
                                 (is (-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}])
                                         (attack-seven-or-more? "wg")))
                                 (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                                             (attack-seven-or-more? "i"))))}
                        [state target-id]
                        {:pre [(map? state) (string? target-id)]}
                        (>= (get-attack state target-id) 7))
    :battlecry        (fn [state _ target-id]
                        (destroy-minion state target-id))}

   "Eater of Secrets"
   {:name        "Eater of Secrets"
    :attack      2
    :health      4
    :mana-cost   4
    :type        :minion
    :set         :whispers-of-the-old-gods
    :rarity      :rare
    :description "Battlecry: Destroy all enemy Secrets. Gain +1/+1 for each."
    :battlecry   (fn [state eater-of-secrets-id]
                   (let [opponent-id (opposing-player-id (get-owner state eater-of-secrets-id))]
                     (let [number-of-secrets (count (get-secrets state opponent-id))]
                       (-> (add-buff state eater-of-secrets-id {:extra-attack number-of-secrets
                                                                :extra-health number-of-secrets})
                           (remove-secrets opponent-id)))))}

   "Arcane Golem"
   {:name        "Arcane Golem"
    :attack      4
    :health      4
    :mana-cost   3
    :type        :minion
    :set         :classic
    :rarity      :rare
    :description "Battlecry: Give your opponent a Mana Crystal."
    :battlecry   (fn [state golem-id]
                   (let [opponent-player-id (opposing-player-id (get-owner state golem-id))]
                     (add-to-max-mana state opponent-player-id 1)))}

   "Acolyte of Pain"
   {:name             "Acolyte of Pain"
    :attack           1
    :health           3
    :mana-cost        3
    :type             :minion
    :set              :classic
    :rarity           :common
    :description      "Whenever this minion takes damage, draw a card."
    :triggered-effect {:on-damage (fn [state acolyte-id [damaged-minion-id]]
                                    (if (= damaged-minion-id acolyte-id)
                                      (draw-card state (get-owner state acolyte-id))
                                      state))}}

   "Snake"
   {:name      "Snake"
    :attack    1
    :health    1
    :mana-cost 1
    :type      :minion
    :rarity    :rare
    :set       :classic
    :race      :beast}

   "Ancient Watcher"
   {:name          "Ancient Watcher"
    :attack        4
    :health        5
    :mana-cost     2
    :type          :minion
    :set           :classic
    :rarity        :rare
    :description   "Can't attack."
    :cannot-attack (fn [_] true)}

   "Sneed's Old Shredder"
   {:name        "Sneed's Old Shredder"
    :attack      5
    :health      7
    :mana-cost   8
    :type        :minion
    :set         :goblins-vs-gnomes
    :rarity      :legendary
    :race        :mech
    :description "Deathrattle: Summon a random Legendary minion."
    :deathrattle (fn [state player-id]
                   (let [[seed legendary-minion] (->> (get-definitions)
                                                      (filter (fn [v] (= (:rarity v) :legendary)))
                                                      (random-nth (get-seed state)))]
                     (-> (summon-minion state player-id legendary-minion)
                         (set-seed seed))))}

   "King Mukla"
   {:name            "King Mukla"
    :attack          5
    :health          5
    :mana-cost       3
    :type            :minion
    :set             :classic
    :rarity          :legendary
    :description     "Battlecry: Give your opponent 2 Bananas."
    :battlecry       (fn [state minion-id]
                       (let [opponent-player-id (opposing-player-id (get-owner state minion-id))]
                         (-> (give-card state opponent-player-id (create-card "Bananas"))
                             (give-card opponent-player-id (create-card "Bananas")))))}

   "Frostbolt"
   {:name        "Frostbolt"
    :mana-cost   2
    :type        :spell
    :set         :basic
    :rarity      :none
    :description "Deal 3 damage to a character and Freeze it."
    :target-type :all
    :spell       (fn [state target-id]
                   (as-> (deal-spell-damage state target-id 3) $
                         (if (get-board-entity $ target-id)
                           (add-buff $ target-id {:frozen true})
                           $)))}

   "Cabal Shadow Priest"
   {:name             "Cabal Shadow Priest"
    :attack           4
    :health           5
    :mana-cost        6
    :type             :minion
    :set              :classic
    :rarity           :epic
    :description      "Battlecry: Take control of an enemy minion that has 2 or less Attack."
    :target-type      :enemy-minions
    :target-condition (defn attack-two-or-less?
                        {:test (fn []
                                 (is (-> (create-game [{:minions [(create-minion "Defender" :id "d")]}])
                                         (attack-two-or-less? "d")))
                                 (is-not (-> (create-game [{:minions [(create-minion "Ancient Watcher" :id "aw")]}])
                                             (attack-two-or-less? "aw"))))}
                        [state target-id]
                        {:pre [(map? state) (string? target-id)]}
                        (<= (get-attack state target-id) 2))
    :battlecry        (fn [state _ target-id]
                        (change-minion-board-side state target-id))}

   "Mind Control"
   {:name        "Mind Control"
    :mana-cost   10
    :type        :spell
    :set         :basic
    :rarity      :none
    :description "Take control of an enemy minion."
    :target-type :enemy-minions
    :spell       (fn [state target-id]
                   (change-minion-board-side state target-id))}

   "Deranged Doctor"
   {:name        "Deranged Doctor"
    :attack      8
    :health      8
    :mana-cost   8
    :type        :minion
    :set         :the-witchwood
    :rarity      :common
    :description "Deathrattle: Restore 8 Health to your hero."
    :deathrattle (fn [state player-id]
                   (heal-hero state (get-hero-id state player-id) 8))}

   "Sylvanas Windrunner"
   {:name        "Sylvanas Windrunner"
    :attack      5
    :health      5
    :mana-cost   6
    :type        :minion
    :set         :hall-of-fame
    :rarity      :legendary
    :description "Deathrattle: Take control of a random enemy minion."
    :deathrattle (fn [state player-id]
                   (let [opp-pid (opposing-player-id player-id)
                         opp-minions (get-minions state opp-pid)]
                     (if (> (count opp-minions) 0)
                       (let [[seed minion] (random-nth (get-seed state) opp-minions)]
                         (-> (change-minion-board-side state (:id minion))
                             (set-seed seed)))
                       state)))}

   "Frothing Berserker"
   {:name             "Frothing Berserker"
    :attack           2
    :health           4
    :mana-cost        3
    :type             :minion
    :set              :classic
    :rarity           :rare
    :description      "Whenever a minion takes damage, gain +1 Attack."
    :triggered-effect {:on-damage (fn [state frothing-berserker-id & _]
                                    (add-buff state frothing-berserker-id {:extra-attack 1}))}}

   "Bananas"
   {:name        "Bananas"
    :mana-cost   1
    :type        :spell
    :set         :classic
    :rarity      :none
    :description "Give a minion +1/+1."
    :target-type :all-minions
    :spell       (fn [state target-id]
                   (add-buff state target-id {:extra-health 1
                                              :extra-attack 1}))}

   "Loot Hoarder"
   {:name        "Loot Hoarder"
    :attack      2
    :health      1
    :mana-cost   2
    :type        :minion
    :set         :classic
    :rarity      :common
    :description "Deathrattle: Draw a card."
    :deathrattle (fn [state player-id]
                   (draw-card state player-id))}

   "Snake Trap"
   {:name             "Snake Trap"
    :mana-cost        2
    :type             :spell
    :subtype          :secret
    :set              :classic
    :rarity           :epic
    :class            :hunter
    :description      "Secret: When one of your minions is attacked summon three 1/1 Snakes."
    :triggered-effect {:on-attack (fn [state snake-trap-id [attacked-minion-id]]
                                    (let [player-id (get-owner state snake-trap-id)]
                                      (if (= player-id (get-owner state attacked-minion-id))
                                        (-> (remove-secret state player-id snake-trap-id)
                                            (summon-minion player-id "Snake")
                                            (summon-minion player-id "Snake")
                                            (summon-minion player-id "Snake"))
                                        state)))}}

   "Fireball"
   {:name        "Fireball"
    :type        :spell
    :mana-cost   4
    :class       :mage
    :set         :basic
    :rarity      :none
    :description "go and grab it from the internet"
    :target-type :all
    :spell       (fn [state target-id]
                   (deal-spell-damage state target-id 6))}

   "Archmage Antonidas"
   {:name             "Archmage Antonidas"
    :attack           5
    :health           7
    :type             :minion
    :mana-cost        7
    :class            :mage
    :set              :classic
    :rarity           :legendary
    :description      "Whenever you cast a spell, add a 'Fireball' spell to your hand."
    :triggered-effect {:on-play-card (fn [state archmage-id & [card-id]]
                                       (println "hey\n")
                                       (if (and (not (minion? state card-id))
                                                (= (get-owner state card-id)
                                                   (get-owner state archmage-id)))
                                         (give-card state (get-owner state archmage-id) (create-card "Fireball"))
                                         state))}}

   "Lorewalker Cho"
   {:name        "Lorewalker Cho"
    :attack      0
    :health      4
    :type        :minion
    :mana-cost   2
    :set         :classic
    :rarity      :legendary
    :description "Whenever a player casts a spell, put a copy into the other player's hand."}

   "Doomsayer"
   {:name             "Doomsayer"
    :attack           0
    :health           7
    :type             :minion
    :mana-cost        2
    :set              :classic
    :rarity           :epic
    :description      "At the start of your turn destroy ALL minions."
    :triggered-effect {:on-start-of-turn (fn [state doomsayer-id & _]
                                           (if (= (get-player-id-in-turn state)
                                                  (get-owner state (get-minion state doomsayer-id)))
                                             (reduce destroy-minion state (map :id (get-minions state)))
                                             state))}}

   "Rampage"
   {:name             "Rampage"
    :type             :spell
    :mana-cost        2
    :class            :warrior
    :set              :classic
    :rarity           :common
    :description      "Give a damaged minion +3/+3."
    :target-type      :all-minions
    :target-condition (defn damaged-minion?
                        [state target-id]
                        {:pre [(map? state) (string? target-id)]}
                        (let [minion (get-minion state target-id)]
                          (and minion
                               (> (get-damage minion) 0))))
    :spell            (fn [state target-id]
                        (add-buff state target-id {:extra-health 3
                                                   :extra-attack 3}))}

   "Abusive Sergeant"
   {:name        "Abusive Sergeant"
    :attack      1
    :health      1
    :mana-cost   1
    :type        :minion
    :set         :classic
    :rarity      :common
    :description "Battlecry: Give a minion +2 Attack this turn."
    :target-type :all-minions
    :battlecry   (fn [state _ target-id]
                   (add-buff state target-id {:extra-attack 2
                                              :counter      1}))}

   "Shrinkmeister"
   {:name        "Shrinkmeister"
    :attack      3
    :health      2
    :mana-cost   2
    :type        :minion
    :set         :goblins-vs-gnomes
    :rarity      :rare
    :description "Battlecry: Give a minion -2 Attack this turn."
    :target-type :all-minions
    :battlecry   (fn [state _ target-id]
                   (add-buff state target-id {:extra-attack -2
                                              :counter      1}))}

   "Malygos"
   {:name         "Malygos"
    :mana-cost    9
    :health       12
    :attack       4
    :type         :minion
    :set          :classic
    :rarity       :legendary
    :race         :dragon
    :description  "Spell Damage +5"
    :spell-damage 5}

   "Steward"
   {:name      "Steward"
    :mana-cost 1
    :health    1
    :attack    1
    :type      :minion
    :set       :one-night-in-karazhan
    :rarity    :none}

   "Unpowered Mauler"
   {:name          "Unpowered Mauler"
    :attack        2
    :health        4
    :mana-cost     2
    :type          :minion
    :set           :the-boomsday-project
    :rarity        :rare
    :race          :mech
    :description   "Can only attack if you cast a spell this turn."
    :cannot-attack (fn [state]
                     (-> (filter (fn [card] (spell-card? card)) (get-cards-played-this-turn state))
                         (count)
                         (= 0)))}

   "Competitive Spirit"
   {:name             "Competitive Spirit"
    :mana-cost        1
    :type             :spell
    :subtype          :secret
    :set              :grand-tournament
    :rarity           :rare
    :class            :paladin
    :description      "When your turn starts, give your minions +1/+1."
    :triggered-effect {:on-start-turn (fn [state competitive-spirit-id _]
                                        (let [owner-id (get-owner state competitive-spirit-id)
                                              minions (get-minions state owner-id)]
                                          ; If there are no minions on the board the secret doesn't activate
                                          (if (and (= owner-id (get-player-id-in-turn state))
                                                   (> (count minions) 0))
                                            (as-> (remove-secret state owner-id competitive-spirit-id) $
                                                  (reduce (fn [state minion]
                                                            (add-buff state (minion :id) {:extra-health 1
                                                                                          :extra-attack 1}))
                                                          $
                                                          (get-minions $ owner-id)))
                                            state)))}}
   "The Coin"
   {:name             "The Coin"
    :mana-cost        0
    :type             :spell
    :set              :basic
    :rarity           :none
    :description      "Gain 1 Mana Crystal this turn only."
    :spell            (fn [state]
                        (add-extra-mana state (get state :player-id-in-turn) 1))}
   })

(definitions/add-definitions! card-definitions)
