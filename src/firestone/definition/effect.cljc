(ns firestone.definition.effect
  (:require [ysera.test :refer [deftest is is-not is= error?]]
            [ysera.random :refer [random-nth]]
            [firestone.definitions :as definitions]
            [firestone.definitions :refer [get-definitions]]
            [firestone.core :refer [draw-card
                                    summon-minion
                                    get-owner
                                    heal-hero]]
            [firestone.construct :refer [update-in-minion
                                         get-hero-id
                                         remove-secret]]))

; These functions are tested in 'card_test.cljc'

(def effect-definitions
  {
   ; On damage effects
   "Acolyte of Pain effect"           (fn [state acolyte-id [damaged-minion-id]]
                                        (let [state (if (= damaged-minion-id acolyte-id)
                                                      (draw-card state (get-owner state acolyte-id))
                                                      state)]
                                          state))

   "Frothing Berserker effect"        (fn [state frothing-berserker-id & _]
                                        (update-in-minion state frothing-berserker-id [:effects :extra-attack] inc))

   ; Deathrattles
   "Loot Hoarder deathrattle"         (fn [state player-id]
                                        (draw-card state player-id))

   "Sneed's Old Shredder deathrattle" (fn [state player-id]
                                        (let [[_ legendary-minion] (->> (get-definitions)
                                                                        (filter (fn [v] (= (:rarity v) :legendary)))
                                                                        (random-nth 0))]
                                          (summon-minion state player-id legendary-minion)))

   "Deranged Doctor deathrattle"      (fn [state player-id]
                                        (heal-hero state (get-hero-id state player-id) 8))

   ; Secrets
   "Snake Trap effect"                (fn [state snake-trap-id [attacked-minion-id]]
                                        (let [player-id (get-owner state snake-trap-id)]
                                          (if (= player-id (get-owner state attacked-minion-id))
                                            (-> (remove-secret state player-id snake-trap-id)
                                                (summon-minion player-id "Snake")
                                                (summon-minion player-id "Snake")
                                                (summon-minion player-id "Snake"))
                                            state)))
   })

(definitions/add-definitions! effect-definitions)