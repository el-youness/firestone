(ns firestone.definition.card-test
  (:require [ysera.test :refer [is is-not is= error? deftest]]
            [firestone.construct :refer [create-game
                                         create-card
                                         create-minion
                                         get-minion
                                         get-player]]
            [firestone.core :refer [get-cost]]
            [firestone.api :refer [end-turn
                                   attack-with-minion
                                   play-spell-card
                                   play-minion-card]]))

(deftest frostbolt-test
         ; If I freeze an ennemy minion/hero on my turn it will thaw on the beginning of my next turn
         (as-> (create-game [{:hand [(create-card "Frostbolt" :id "f")] :deck [(create-card "Imp" :id "i1")]}
                             {:minions [(create-minion "War Golem" :id "wg" :attacks-performed-this-turn 1)] :deck [(create-card "Imp" :id "i2")]}]) $
               (play-spell-card $ "p1" "f" {:target-id "wg"})
               (let [minion (get-minion $ "wg") attacker (get-player $ "p1")]
                 (is= (get-in minion [:effects :frozen])
                      true)
                 (is= (get minion :damage-taken)
                      3)
                 ; Check that the player consumed the mana
                 (is= (get attacker :used-mana)
                      (get-cost (create-card "Frostbolt"))) $)
               (end-turn $)
               ; Minion should stay frozen after the first end-turn
               (let [minion (get-minion $ "wg")]
                 (is= (get-in minion [:effects :frozen])
                      true)
                 (is= (get minion :attacks-performed-this-turn)
                      0)
                 (is= (-> ((get-player $ "p2") :hand)
                          (count))
                      1) $)
               (end-turn $)
               ; Minion should be unfrozen after the second end-turn
               (let [minion (get-minion $ "wg")]
                 (is= (get-in minion [:effects :frozen])
                      false)
                 (is= (-> ((get-player $ "p1") :hand)
                          (count))
                      1) $))
         ; If I freeze a friendly minion, Unfreeze at end-turn if they didn't attack
         (as-> (create-game [{:hand [(create-card "Frostbolt" :id "f1") (create-card "Frostbolt" :id "f2")]
                              :deck [(create-card "Imp" :id "i1")]
                              :minions [(create-minion "War Golem" :id "wg" :attacks-performed-this-turn 0)]}
                             { :deck [(create-card "Imp" :id "i2") (create-card "Imp" :id "i3")]}]) $
               ;
               (play-spell-card $ "p1" "f1" {:target-id "wg"})
               (let [minion (get-minion $ "wg") attacker (get-player $ "p1")]
                 (is= (get-in minion [:effects :frozen])
                      true)
                 (is= (get minion :damage-taken)
                      3) $)
               (end-turn $)
               (let [minion (get-minion $ "wg")]
                 (is= (get-in minion [:effects :frozen])
                      false)
                 (is= (-> ((get-player $ "p2") :hand)
                          (count))
                      1) $))
         ; If I freeze a friendly minion, Unfreeze at the end of next turn if they already attacked
         (as-> (create-game [{:hand [(create-card "Frostbolt" :id "f1")]
                              :deck [(create-card "Imp" :id "i1")]
                              :minions [(create-minion "War Golem" :id "wg" :attacks-performed-this-turn 1)]}
                             { :deck [(create-card "Imp" :id "i2")]}]) $
               (play-spell-card $ "p1" "f1" {:target-id "wg"})
               (let [minion (get-minion $ "wg")]
                 (is= (get-in minion [:effects :frozen])
                      true)
                 (is= (get minion :attacks-performed-this-turn)
                      1) $)
               (end-turn $)

               (let [minion (get-minion $ "wg")]
                 (is= (get-in minion [:effects :frozen])
                      true)
                 (is= (get minion :attacks-performed-this-turn)
                      1) $)
               (end-turn $)
               (end-turn $)
               (do (is= (get-in (get-minion $ "wg") [:effects :frozen])
                        false) $)
               )

         )