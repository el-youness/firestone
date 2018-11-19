(ns firestone.definition.card
  (:require [firestone.definitions :as definitions]
            [clojure.test :refer [function?]]
            [ysera.test :refer [is is-not is= error?]]
            [firestone.construct :refer [create-game
                                         create-minion
                                         create-card
                                         create-secret
                                         update-minion
                                         update-in-minion
                                         get-minion
                                         get-minions
                                         get-secrets
                                         get-effects
                                         remove-secrets]]
            [firestone.core :refer [change-minion-board-side
                                    get-owner
                                    get-attack
                                    get-health
                                    valid-plays
                                    destroy-minion]]
            [firestone.api :refer [play-minion-card]]))

(def card-definitions
  {

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
    :target-condition (defn attack-over-seven?
                        {:test (fn []
                                 (is (-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}])
                                         (attack-over-seven? "wg")))
                                 (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                                             (attack-over-seven? "i"))))}
                        [state target-id]
                        {:pre [(map? state) (string? target-id)]}
                        (>= (get-attack state target-id) 7))
    :battlecry        (defn big-game-hunter
                        {:test (fn []
                                 (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}])
                                          (big-game-hunter "m1" "wg")
                                          (get-minions "imp")
                                          (count))
                                      0))}
                        [state _ target-id]
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
    :battlecry   (defn eater-of-secrets-battlecry
                   {:test (fn []
                            ; Opponent has one secret.
                            (is= (as-> (create-game [{:hand [(create-card "Eater of Secrets" :id "es")]}
                                                     {:secrets ["Snake Trap"]}]) $
                                       (play-minion-card $ "p1" "es" {:position 0})
                                       [(count (get-secrets $)) (get-attack $ "m2") (get-health $ "m2")])
                                 [0 3 5])
                            ; Opponent has two secret.
                            (is= (as-> (create-game [{:hand [(create-card "Eater of Secrets" :id "es")]}
                                                     {:secrets ["Snake Trap" "Snake Trap"]}]) $
                                       (play-minion-card $ "p1" "es" {:position 0})
                                       [(count (get-secrets $)) (get-attack $ "m3") (get-health $ "m3")])
                                 [0 4 6])
                            ; Oppenent has no secrets.
                            (is= (as-> (create-game [{:hand [(create-card "Eater of Secrets" :id "es")]}]) $
                                       (play-minion-card $ "p1" "es" {:position 0})
                                       [(count (get-secrets $)) (get-attack $ "m1") (get-health $ "m1")])
                                 [0 2 4]))}
                   [state eater-of-secrets-id]
                   (println "In EoS battlecry: " state)
                   (let [opponent-id (if (= (get-owner state eater-of-secrets-id) "p1")
                                       "p2"
                                       "p1")]
                     (let [number-of-secrets (count (get-secrets state opponent-id))]
                       (-> (update-in-minion state eater-of-secrets-id [:effects :extra-attack] (partial + number-of-secrets))
                           (update-in-minion eater-of-secrets-id [:effects :extra-health] (partial + number-of-secrets))))))}

   "Arcane Golem"
   {:name        "Arcane Golem"
    :attack      4
    :health      4
    :mana-cost   3
    :type        :minion
    :set         :classic
    :rarity      :rare
    :description "Battlecry: Give your opponent a Mana Crystal."}

   "Acolyte of Pain"
   {:name        "Acolyte of Pain"
    :attack      1
    :health      3
    :mana-cost   3
    :type        :minion
    :set         :classic
    :rarity      :common
    :description "Whenever this minion takes damage, draw a card."
    :on-damage   "Acolyte of Pain effect"}

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
    :cannot-attack true}

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
    :deathrattle "Sneed's Old Shredder deathrattle"}

   "King Mukla"
   {:name        "King Mukla"
    :attack      5
    :health      5
    :mana-cost   3
    :type        :minion
    :set         :classic
    :rarity      :legendary
    :description "Battlecry: Give your opponent 2 Bananas."}

   "Frostbolt"
   {:name        "Frostbolt"
    :mana-cost   2
    :type        :spell
    :set         :basic
    :rarity      :none
    :description "Deal 3 damage to a character and Freeze it."}

   "Cabal Shadow Priest"
   {:name        "Cabal Shadow Priest"
    :attack      4
    :health      5
    :mana-cost   6
    :type        :minion
    :set         :classic
    :rarity      :epic
    :description "Battlecry: Take control of an enemy minion that has 2 or less Attack."}

   "Mind Control"
   {:name        "Mind Control"
    :mana-cost   10
    :type        :spell
    :set         :basic
    :rarity      :none
    :description "Take control of an enemy minion."
    :target-type :enemy-minions
    :spell       (defn mind-control
                   {:test (fn []
                            (is= (-> (create-game [{:minions [(create-minion "Imp" :id "imp")]}])
                                     (mind-control "imp")
                                     (get-owner "imp"))
                                 "p2"))}
                   [state target-id]
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
    :deathrattle "Deranged Doctor deathrattle"}

   "Sylvanas Windrunner"
   {:name        "Sylvanas Windrunner"
    :attack      5
    :health      5
    :mana-cost   6
    :type        :minion
    :set         :hall-of-fame
    :rarity      :legendary
    :description "Deathrattle: Take control of a random enemy minion."}

   "Frothing Berserker"
   {:name        "Frothing Berserker"
    :attack      2
    :health      4
    :mana-cost   3
    :type        :minion
    :set         :classic
    :rarity      :rare
    :description "Whenever a minion takes damage, gain +1 Attack."
    :on-damage   "Frothing Berserker effect"}

   "Bananas"
   {:name        "Bananas"
    :mana-cost   1
    :type        :spell
    :set         :classic
    :description "Give a minion +1/+1."
    :target-type :all-minions
    :spell       (defn banana
                   {:test (fn []
                            (is= (let [minion (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                                                  (banana "i")
                                                  (get-minion "i"))
                                       effects (get minion :effects)]
                                   [(get effects :extra-health)
                                    (get effects :extra-attack)]
                                   )
                                 [1 1]))}
                   [state target-id]
                   (-> (update-in-minion state target-id [:effects :extra-health] inc)
                       (update-in-minion target-id [:effects :extra-attack] inc)))}

   "Loot Hoarder"
   {:name        "Loot Hoarder"
    :attack      2
    :health      1
    :mana-cost   2
    :type        :minion
    :set         :classic
    :rarity      :common
    :description "Deathrattle: Draw a card."
    :deathrattle "Loot Hoarder deathrattle"}

   "Snake Trap"
   {:name        "Snake Trap"
    :mana-cost   2
    :type        :spell
    :subtype     :secret
    :set         :classic
    :rarity      :epic
    :description "Secret: When one of your minions is attacked summon three 1/1 Snakes."
    :on-attack   "Snake Trap effect"}
   })

(definitions/add-definitions! card-definitions)