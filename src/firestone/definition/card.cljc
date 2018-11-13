(ns firestone.definition.card
  (:require [firestone.definitions :as definitions]))

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
    :set         :classic
    :rarity      :epic
    :description "Battlecry: Destroy a minion with an Attack of 7 or more."}

   "Eater of Secrets"
   {:name        "Eater of Secrets"
    :attack      2
    :health      4
    :mana-cost   4
    :set         :whispers-of-the-old-gods
    :rarity      :rare
    :description "Battlecry: Destroy all enemy Secrets. Gain +1/+1 for each."}

   "Arcane Golem"
   {:name        "Arcane Golem"
    :attack      4
    :health      4
    :mana-cost   3
    :set         :classic
    :rarity      :rare
    :description "Battlecry: Give your opponent a Mana Crystal."}

   "Acolyte of Pain"
   {:name        "Acolyte of Pain"
    :attack      1
    :health      3
    :mana-cost   3
    :set         :classic
    :rarity      :common
    :description "Whenever this minion takes damage, draw a card."
    :triggers     [:on-damage]
    :on-damage   "Acolyte of Pain"}

   "Snake"
   {:name        "Snake"
    :attack      1
    :health      1
    :mana-cost   1
    :set         :classic}

   "Ancient Watcher"
   {:name        "Ancient Watcher"
    :attack      4
    :health      5
    :mana-cost   2
    :set         :classic
    :rarity      :rare
    :description "Can't attack."}

   "Sneed's Old Shredder"
   {:name        "Sneed's Old Shredder"
    :attack      5
    :health      7
    :mana-cost   8
    :set         :goblins-vs-gnomes
    :rarity      :legendary
    :description "Deathrattle: Summon a random Legendary minion."
    :deathrattle (fn [state change-to-your-args]
                   )}

   "King Mukla"
   {:name        "King Mukla"
    :attack      5
    :health      5
    :mana-cost   3
    :set         :classic
    :description "Battlecry: Give your opponent 2 Bananas."}

   "Frostbolt"
   {:name        "Frostbolt"
    :mana-cost   2
    :set         :basic
    :rarity      :none
    :description "Deal 3 damage to a character and Freeze it."}

   "Cabal Shadow Priest"
   {:name        "Cabal Shadow Priest"
    :attack      4
    :health      5
    :mana-cost   6
    :set         :classic
    :rarity      :epic
    :description "Battlecry: Take control of an enemy minion that has 2 or less Attack."}

   "Mind Control"
   {:name        "Mind Control"
    :mana-cost   10
    :set         :basic
    :rarity      :none
    :description "Take control of an enemy minion."}

   "Deranged Doctor"
   {:name        "Deranged Doctor"
    :attack      8
    :health      8
    :mana-cost   8
    :set         :the-witchwood
    :rarity      :common
    :description "Deathrattle: Restore 8 Health to your hero."}

   "Sylvanas Windrunner"
   {:name        "Sylvanas Windrunner"
    :attack      5
    :health      5
    :mana-cost   6
    :set         :hall-of-fame
    :rarity      :legendary
    :description "Deathrattle: Take control of a random enemy minion."}

   "Frothing Berserker"
   {:name        "Frothing Berserker"
    :attack      2
    :health      4
    :mana-cost   3
    :set         :classic
    :rarity      :rare
    :description "Whenever a minion takes damage, gain +1 Attack."}

   "Bananas"
   {:name        "Bananas"
    :mana-cost   1
    :set         :classic
    :description "Give a minion +1/+1."}

   "Loot Hoarder"
   {:name        "Loot Hoarder"
    :attack      2
    :health      1
    :mana-cost   2
    :set         :classic
    :rarity      :common
    :description "Deathrattle: Draw a card."}

   "Snake Trap"
   {:name        "Snake Trap"
    :mana-cost   2
    :set         :classic
    :rarity      :epic
    :description "Secret: When one of your minions is attacked summon three 1/1 Snakes."}
   })

(definitions/add-definitions! card-definitions)