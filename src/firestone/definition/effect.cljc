(ns firestone.definition.effect
  (:require [firestone.definitions :as definitions]
            [firestone.core :refer [get-owner
                                    draw-card]]))

(def effect-definitions
  {
   "Acolyte of Pain effect"   (fn [state acolyte-id [damaged-minion-id]]
                                (let [state (if (= damaged-minion-id acolyte-id)
                                              (draw-card state (get-owner state acolyte-id))
                                              state)]
                                  state))
   "Loot Hoarder deathrattle" (fn [state loot-hoarder-id]
                                (draw-card state (get-owner state loot-hoarder-id)))
   })

(definitions/add-definitions! effect-definitions)