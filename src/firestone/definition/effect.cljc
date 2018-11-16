(ns firestone.definition.effect
  (:require [ysera.test :refer [deftest is is-not is= error?]]
            [firestone.definitions :as definitions]
            [firestone.api :refer [attack-with-minion]]
            [firestone.definitions :refer [get-definition]]
            [firestone.core :refer [get-attack
                                    draw-card
                                    get-owner]]
            [firestone.construct :refer [create-game
                                         create-minion
                                         get-hand
                                         create-hero
                                         update-in-minion]]))

(def effect-definitions
  {
   ; On damage effects
   "Acolyte of Pain effect"    (defn acolyte-of-pain-effect
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

   "Frothing Berserker effect" (defn frothing-berserker-effect
                                 {:test (fn []
                                          (is= (-> (create-game [{:minions [(create-minion "Frothing Berserker" :id "fb")]}
                                                                 {:minions [(create-minion "Imp" :id "imp")]}])
                                                   (attack-with-minion "fb" "imp")
                                                   (get-attack "fb"))
                                               4))}
                                 [state frothing-berserker-id [damaged-minion-id]]
                                 (update-in-minion state frothing-berserker-id [:effects :extra-attack] inc))

   ; Deathrattles
   "Loot Hoarder deathrattle"  (defn loot-hoarder-deathrattle
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
                                 [state loot-hoarder-id]
                                 (draw-card state (get-owner state loot-hoarder-id)))
   })

(definitions/add-definitions! effect-definitions)