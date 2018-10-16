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

   })

(definitions/add-definitions! card-definitions)