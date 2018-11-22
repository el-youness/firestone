(ns firestone.definition.hero
  (:require [firestone.definitions :as definitions]))

(def hero-definitions
  {
   "Anduin Wrynn"
   {:name       "Anduin Wrynn"
    :type       :hero
    :class      :priest
    :health     30
    :hero-power "Lesser Heal"}

   "Jaina Proudmoore"
   {:name       "Jaina Proudmoore"
    :type       :hero
    :class      :mage
    :health     30
    :hero-power "Fireblast"}

   "Rexxar"
   {:name       "Rexxar"
    :type       :hero
    :class      :hunter
    :health     30
    :hero-power "Steady Shot"}

   "Uther Lightbringer"
   {:name       "Uther Lightbringer"
    :type       :hero
    :class      :paladin
    :health     30
    :hero-power "Reinforce"}

   "Fireblast"
   {:name        "Fireblast"
    :mana-cost   2
    :type        :hero-power
    :target-type :all-minions
    :description "Deal 1 damage."}

   "Lesser Heal"
   {:name        "Lesser Heal"
    :mana-cost   2
    :type        :hero-power
    :target-type :all-minions
    :description "Restore 2 health."}

   "Reinforce"
   {:name        "Reinforce"
    :mana-cost   2
    :type        :hero-power
    :description "Summon a 1/1 Silver Hand Recruit."}

   "Steady Shot"
   {:name        "Steady Shot"
    :mana-cost   2
    :type        :hero-power
    :target-type :enemy-hero
    :description "Deal 2 damage to the enemy hero."}

   })

(definitions/add-definitions! hero-definitions)