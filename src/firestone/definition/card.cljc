(ns firestone.definition.card
  (:require [firestone.definitions :as definitions]
            [clojure.test :refer [function?]]
            [ysera.test :refer [is is-not is= error?]]
            [firestone.construct :refer [create-game
                                         create-minion
                                         update-minion
                                         update-in-minion
                                         get-minion
                                         get-minions
                                         get-minion-effects]]
            [firestone.core :refer [change-minion-board-side
                                    get-owner]]))

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
   {:name        "Big Game Hunter"
    :attack      4
    :health      2
    :mana-cost   5
    :type        :minion
    :set         :classic
    :rarity      :epic
    :description "Battlecry: Destroy a minion with an Attack of 7 or more."}

   "Eater of Secrets"
   {:name        "Eater of Secrets"
    :attack      2
    :health      4
    :mana-cost   4
    :type        :minion
    :set         :whispers-of-the-old-gods
    :rarity      :rare
    :description "Battlecry: Destroy all enemy Secrets. Gain +1/+1 for each."}

   "Arcane Golem"
   {:name        "Arcane Golem"
    :attack      4
    :health      4
    :mana-cost   3
    :type        :minion
    :set         :classic
    :rarity      :rare
    :description "Battlecry: Give your opponent a Mana Crystal."
    :on-playing-card "Arcane Golem battlecry"}

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
   {:name        "Ancient Watcher"
    :attack      4
    :health      5
    :mana-cost   2
    :type        :minion
    :set         :classic
    :rarity      :rare
    :description "Can't attack."
    :cannot-attack   true}

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
    :description "Battlecry: Give your opponent 2 Bananas."
    :on-playing-card "King Mukla battelcry"}

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
    :set         :classic
    :rarity      :epic
    :description "Secret: When one of your minions is attacked summon three 1/1 Snakes."}
   })

(definitions/add-definitions! card-definitions)