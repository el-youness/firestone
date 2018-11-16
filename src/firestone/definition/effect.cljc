(ns firestone.definition.effect
  (:require [ysera.test :refer [deftest is is-not is= error?]]
            [ysera.random :refer [random-nth]]
            [firestone.definitions :as definitions]
            [firestone.api :refer [attack-with-minion]]
            [firestone.definitions :refer [get-definition
                                           get-definitions]]
            [firestone.core :refer [get-attack
                                    draw-card
                                    get-owner
                                    summon-minion
                                    get-health
                                    heal-hero]]
            [firestone.construct :refer [create-game
                                         create-minion
                                         get-hand
                                         create-hero
                                         update-in-minion
                                         get-minions
                                         get-hero-id]]))

(def effect-definitions
  {
   ; On damage effects
   "Acolyte of Pain effect"           (defn acolyte-of-pain-effect
                                        "Whenever this minion takes damage, draw a card."
                                        {:test (fn []
                                                 (is= (-> (create-game [{:minions [(create-minion "Acolyte of Pain" :id "ap")] :deck ["Imp"]}
                                                                        {:minions [(create-minion "Imp" :id "imp")]}])
                                                          (attack-with-minion "ap" "imp")
                                                          (get-hand "p1")
                                                          (count))
                                                      1)
                                                 ; Two Acolyte of Pain on board but only one should be triggered
                                                 (is= (-> (create-game [{:minions [(create-minion "Acolyte of Pain" :id "ap") "Acolyte of Pain"] :deck ["Imp" "Imp"]}
                                                                        {:minions [(create-minion "Imp" :id "imp")]}])
                                                          (attack-with-minion "ap" "imp")
                                                          (get-hand "p1")
                                                          (count))
                                                      1))}
                                        [state acolyte-id [damaged-minion-id]]
                                        (let [state (if (= damaged-minion-id acolyte-id)
                                                      (draw-card state (get-owner state acolyte-id))
                                                      state)]
                                          state))

   "Frothing Berserker effect"        (defn frothing-berserker-effect
                                        "Whenever a minion takes damage, gain +1 Attack."
                                        {:test (fn []
                                                 (is= (-> (create-game [{:minions [(create-minion "Frothing Berserker" :id "fb")]}
                                                                        {:minions [(create-minion "Imp" :id "imp")]}])
                                                          (attack-with-minion "fb" "imp")
                                                          (get-attack "fb"))
                                                      4))}
                                        [state frothing-berserker-id & _]
                                        (update-in-minion state frothing-berserker-id [:effects :extra-attack] inc))

   ; Deathrattles
   "Loot Hoarder deathrattle"         (defn loot-hoarder-deathrattle
                                        "Deathrattle: Draw a card."
                                        {:test (fn []
                                                 ; Trigger deathrattle for player in turn
                                                 (is= (-> (create-game [{:minions [(create-minion "Loot Hoarder" :id "lh")] :deck ["Imp"]}
                                                                        {:minions [(create-minion "War Golem" :id "wg")]}])
                                                          (attack-with-minion "lh" "wg")
                                                          (get-hand "p1")
                                                          (count))
                                                      1)
                                                 ; Trigger deathrattle for player not in turn
                                                 (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}
                                                                        {:minions [(create-minion "Loot Hoarder" :id "lh")] :deck ["Imp"]}])
                                                          (attack-with-minion "wg" "lh")
                                                          (get-hand "p2")
                                                          (count))
                                                      1))}
                                        [state player-id]
                                        (draw-card state player-id))

   "Sneed's Old Shredder deathrattle" (defn sneeds-old-shredder-deathrattle
                                        "Deathrattle: Summon a random Legendary minion."
                                        {:test (fn []
                                                 (is= (-> (create-game [{:minions [(create-minion "Sneed's Old Shredder" :id "sneeds")]}
                                                                        {:minions [(create-minion "War Golem" :id "wg")]}])
                                                          (attack-with-minion "sneeds" "wg")
                                                          (get-minions "p1")
                                                          (first)
                                                          (:name)
                                                          (get-definition)
                                                          (:rarity))
                                                      :legendary)
                                                 ; A random legendary minion should be summon even if board was full befire Sneed's died
                                                 (is= (-> (create-game [{:minions [(create-minion "Sneed's Old Shredder" :id "sneeds")
                                                                                   "Imp" "Imp" "Imp" "Imp" "Imp" "Imp"]}
                                                                        {:minions [(create-minion "War Golem" :id "wg")]}])
                                                          (attack-with-minion "sneeds" "wg")
                                                          (get-minions "p1")
                                                          (count))
                                                      7))}
                                        [state player-id]
                                        (let [[_ legendary-minion] (->> (get-definitions)
                                                                        (filter (fn [v] (= (:rarity v) :legendary)))
                                                                        (random-nth 0))]
                                          (summon-minion state player-id legendary-minion)))

   "Deranged Doctor deathrattle"      (defn deranged-doctor-deathrattle
                                        "Deathrattle: Restore 8 Health to your hero."
                                        {:test (fn []
                                                 (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg") (create-minion "Imp" :id "imp")]}
                                                                        {:hero (create-hero "Rexxar" :id "h2" :damage-taken 9) :minions [(create-minion "Deranged Doctor" :id "dd")]}])
                                                          (attack-with-minion "wg" "dd")
                                                          (attack-with-minion "imp" "dd")
                                                          (get-health "h2"))
                                                      29))}
                                        [state player-id]
                                        (heal-hero state (get-hero-id state player-id) 8))})

(definitions/add-definitions! effect-definitions)