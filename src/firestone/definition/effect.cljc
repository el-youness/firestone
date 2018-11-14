(ns firestone.definition.effect
  (:require [firestone.definitions :as definitions]
            [firestone.core :refer [draw-card
                                    get-owner]]))

(def effect-definitions
  {
   "Acolyte of Pain effect" (fn [state acolyte-id damaged-minion-id]
                              (println "I was here")
                              (if (= damaged-minion-id acolyte-id)
                                (draw-card state (get-owner state acolyte-id))
                                state))
   })

(definitions/add-definitions! effect-definitions)