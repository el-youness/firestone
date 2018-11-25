(ns firestone.definition.effect
  (:require [ysera.test :refer [deftest is is-not is= error?]]
            [ysera.random :refer [random-nth]]
            [firestone.definitions :as definitions]
            [firestone.definitions :refer [get-definitions]]
            [firestone.core :refer [draw-card
                                    summon-minion
                                    get-health
                                    heal-hero
                                    valid-plays
                                    change-minion-board-side
                                    destroy-minion
                                    get-owner
                                    heal-hero
                                    add-to-max-mana]]
            [firestone.construct :refer [create-game
                                         create-minion
                                         create-card
                                         get-hand
                                         create-hero
                                         update-in-minion
                                         get-minions
                                         get-hero-id
                                         create-card
                                         add-card-to-hand
                                         opposing-player-id
                                         get-mana
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

   "Sylvanas Windrunner deathrattle"    (defn sylvanas-deathrattle
                                          "Deathrattle: Take control of a random enemy minion."
                                          {:test (fn []
                                                   (is= (-> (create-game [{:minions [(create-minion "Sylvanas Windrunner" :id "s")]}
                                                                          {:minions [(create-minion "War Golem" :id "wg")]}])
                                                            (attack-with-minion "s" "wg")
                                                            (get-minions "p1")
                                                            (first)
                                                            (:name))
                                                        "War Golem")
                                                   ; If there are no opposing minions, nothing happens
                                                   (is= (-> (create-game [{:minions [(create-minion "Sylvanas Windrunner" :id "s")]}])
                                                            (destroy-minion "s")
                                                            (get-minions "p1")
                                                            (count))
                                                        0))}
                                          [state player-id]
                                          (let [opp-pid (opposing-player-id player-id)
                                                opp-minions (get-minions state opp-pid) ]
                                            (if (> (count opp-minions) 0)
                                              (change-minion-board-side state (:id (second(random-nth 0 opp-minions))))
                                              state)))

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
