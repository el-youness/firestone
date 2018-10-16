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

   })

(definitions/add-definitions! hero-definitions)