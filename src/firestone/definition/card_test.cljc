(ns firestone.definition.card-test
  (:require [ysera.test :refer :all]
            [firestone.definitions :refer :all]
            [firestone.construct :refer :all]
            [firestone.core :refer :all]
            [firestone.api :refer :all]))

(deftest acolyte-of-pain
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
              1))

(deftest arcane-golem
         (as-> (create-game [{:hand [(create-card "Arcane Golem" :id "ag1") (create-card "Arcane Golem" :id "ag2")]}
                             {:max-mana 9}]) $
               (play-minion-card $ "p1" "ag1" {:position 0})
               (do (is= (get-mana $ "p2")
                        10)
                   $)
               (play-minion-card $ "p1" "ag2" {:position 0})
               (is= (get-mana $ "p2")
                    10)))

(deftest bananas
         (as-> (create-game [{:minions [(create-minion "Imp" :id "imp")]
                              :hand    [(create-card "Bananas" :id "b")]}]) $
               (play-spell-card $ "p1" "b" {:target-id "imp"})
               (do (is= (:extra-attack (get-minion-buffs $ "imp"))
                        1)
                   (is= (:extra-health (get-minion-buffs $ "imp"))
                        1))))

(deftest big-game-huner
         ; Available target on board
         (is= (as-> (create-game [{:hand [(create-card "Big Game Hunter" :id "bgh")]}
                                  {:minions [(create-minion "War Golem" :id "wg") "Imp"]}]) $
                    (play-minion-card $ "p1" "bgh" {:position 0 :target-id "wg"})
                    (get-minions $)
                    (map :name $)
                    (set $))
              #{"Imp" "Big Game Hunter"})
         ; Can be played without target if none available
         (is= (as-> (create-game [{:hand [(create-card "Big Game Hunter" :id "bgh")]}
                                  {:minions ["Imp"]}]) $
                    (play-minion-card $ "p1" "bgh" {:position 0})
                    (get-minions $)
                    (map :name $)
                    (set $))
              #{"Imp" "Big Game Hunter"}))

(deftest cabal-shadow-priest
         ; With available target.
         (is= (as-> (create-game [{:hand [(create-card "Cabal Shadow Priest" :id "c")]
                                   :deck ["Imp"]}
                                  {:minions [(create-minion "Defender" :id "d")]
                                   :deck    ["Imp"]}]) $
                    (play-minion-card $ "p1" "c" {:position 0 :target-id "d"})
                    (do (is= (count (get-minions $ "p1")) 2)
                        (is= (count (get-minions $ "p2")) 0)
                        ; Target minion should be sleepy
                        (is-not (valid-attack? $ "p1" "d" "h2"))
                        $)
                    (end-turn $)
                    (end-turn $)
                    (attack-with-minion $ "d" "h2")
                    (get-health $ "h2"))
              28)
         ; Can be played without available target
         (is= (-> (create-game [{:hand [(create-card "Cabal Shadow Priest" :id "c")]}])
                  (play-minion-card "p1" "c" {:position 0})
                  (get-minions "p1")
                  (first)
                  :name)
              "Cabal Shadow Priest"))

(deftest deragned-doctor
         (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg") (create-minion "Imp" :id "imp")]}
                                {:hero (create-hero "Rexxar" :id "h2" :damage-taken 9) :minions [(create-minion "Deranged Doctor" :id "dd")]}])
                  (attack-with-minion "wg" "dd")
                  (attack-with-minion "imp" "dd")
                  (get-health "h2"))
              29))

(deftest eater-of-secrets
         ; Opponent has one secret.
         (is= (as-> (create-game [{:hand [(create-card "Eater of Secrets" :id "es")]}
                                  {:secrets ["Snake Trap"]}]) $
                    (play-minion-card $ "p1" "es" {:position 0})
                    [(count (get-secrets $)) (get-attack $ "m2") (get-health $ "m2")])
              [0 3 5])
         ; Opponent has two secret.
         (is= (as-> (create-game [{:hand [(create-card "Eater of Secrets" :id "es")]}
                                  {:secrets ["Snake Trap" "Snake Trap"]}]) $
                    (play-minion-card $ "p1" "es" {:position 0})
                    [(count (get-secrets $)) (get-attack $ "m3") (get-health $ "m3")])
              [0 4 6])
         ; Opponent has no secrets.
         (is= (as-> (create-game [{:hand [(create-card "Eater of Secrets" :id "es")]}]) $
                    (play-minion-card $ "p1" "es" {:position 0})
                    [(count (get-secrets $)) (get-attack $ "m1") (get-health $ "m1")])
              [0 2 4]))

(deftest frostbolt
         ; If I freeze an enemy minion/hero on my turn it will thaw on the beginning of my next turn
         (as-> (create-game [{:hand [(create-card "Frostbolt" :id "f")] :deck [(create-card "Imp" :id "i1")] :minions ["Dalaran Mage"]}
                             {:minions [(create-minion "War Golem" :id "wg")] :deck [(create-card "Imp" :id "i2")]}]) $
               (play-spell-card $ "p1" "f" {:target-id "wg"})
               (let [minion (get-minion $ "wg") attacker (get-player $ "p1")]
                 (is= (:frozen (get-minion-buffs minion))
                      true)
                 ; We check it's 4 because "Dalaran Mage" has +1 spell damage
                 (is= (get minion :damage-taken)
                      4)
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
                 (is= (:frozen (get-minion-buffs minion))
                      false)
                 (is= (-> ((get-player $ "p1") :hand)
                          (count))
                      1) $))
         ; If I freeze a friendly minion, Unfreeze at end-turn if they didn't attack
         (as-> (create-game [{:hand    [(create-card "Frostbolt" :id "f1") (create-card "Frostbolt" :id "f2")]
                              :deck    [(create-card "Imp" :id "i1")]
                              :minions ["Dalaran Mage" "Ogre Magi" (create-minion "War Golem" :id "wg" :attacks-performed-this-turn 0)]}
                             {:deck [(create-card "Imp" :id "i2") (create-card "Imp" :id "i3")]}]) $
               ;
               (play-spell-card $ "p1" "f1" {:target-id "wg"})
               (let [minion (get-minion $ "wg") attacker (get-player $ "p1")]
                 (is= (:frozen (get-minion-buffs minion))
                      true)
                 (is= (get minion :damage-taken)
                      5) $)
               (end-turn $)
               (let [minion (get-minion $ "wg")]
                 (is= (:frozen (get-minion-buffs minion))
                      false)
                 (is= (-> ((get-player $ "p2") :hand)
                          (count))
                      1) $))
         ; If I freeze a friendly minion, Unfreeze at the end of next turn if they already attacked
         (as-> (create-game [{:hand    [(create-card "Frostbolt" :id "f1")]
                              :deck    [(create-card "Imp" :id "i1")]
                              :minions [(create-minion "War Golem" :id "wg" :attacks-performed-this-turn 1)]}
                             {:deck [(create-card "Imp" :id "i2")]}]) $
               (play-spell-card $ "p1" "f1" {:target-id "wg"})
               (let [minion (get-minion $ "wg")]
                 (is= (:frozen (get-minion-buffs minion))
                      true)
                 (is= (get minion :attacks-performed-this-turn)
                      1) $)
               (end-turn $)
               (let [minion (get-minion $ "wg")]
                 (is= (:frozen (get-minion-buffs minion))
                      true)
                 (is= (get minion :attacks-performed-this-turn)
                      1) $)
               (end-turn $)
               (do (is= (get-in (get-minion $ "wg") [:effects :frozen])
                        true) $)
               (end-turn $)
               (do (is= (get-in (get-minion $ "wg") [:effects :frozen])
                        false) $)))

(deftest frothing-berserker
         (is= (-> (create-game [{:minions [(create-minion "Frothing Berserker" :id "fb")]}
                                {:minions [(create-minion "Imp" :id "imp")]}])
                  (attack-with-minion "fb" "imp")
                  (get-attack "fb"))
              4))

(deftest king-mukla
         (is= (-> (create-game [{:hand [(create-card "King Mukla" :id "km")]}])
                  (play-minion-card "p1" "km" {:position 0})
                  (get-hand "p2")
                  (->> (map :name)))
              ["Bananas" "Bananas"])
         ; Opponent with 9 cards in hand receives 1 bananas
         (is= (as-> (create-game [{:hand [(create-card "King Mukla" :id "km")]}
                                  {:hand (repeat 9 "Imp")}]) $
                    (play-minion-card $ "p1" "km" {:position 0})
                    (get-hand $ "p2")
                    (filter (fn [c] (= (:name c) "Bananas")) $)
                    (count $))
              1)
         ; Opponent with 10 cards in hand receives 0 bananas
         (is= (as-> (create-game [{:hand [(create-card "King Mukla" :id "km")]}
                                  {:hand (repeat 10 "Imp")}]) $
                    (play-minion-card $ "p1" "km" {:position 0})
                    (get-hand $ "p2")
                    (filter (fn [c] (= (:name c) "Bananas")) $)
                    (count $))
              0))

(deftest loot-hoarder
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
              1))

(deftest mind-control
         (as-> (create-game [{:hand [(create-card "Mind Control" :id "mc")]}
                             {:minions [(create-minion "Imp" :id "imp")]}]) $
               (play-spell-card $ "p1" "mc" {:target-id "imp"})
               (do
                 (is= (-> (get-minions $ "p1")
                          (count))
                      1)
                 ; Stolen minion cannot attack on same turn, unless it has charge
                 (is-not (valid-attack? $ "p1" "imp" "h2"))))

         ; Destroy target minion if board is full
         (as-> (create-game [{:hand    [(create-card "Mind Control" :id "mc")]
                              :minions (repeat 7 "Imp")}
                             {:minions [(create-minion "War Golem" :id "wg")]}]) $
               (play-spell-card $ "p1" "mc" {:target-id "wg"})
               (do
                 (is= (get-minions $ "p2")
                      [])
                 (is= (->> (get-minions $ "p1")
                           (filter (fn [x] (= (:name x) "War Golem"))))
                      []))))

(deftest snake-trap
         (is= (as-> (create-game [{:hand [(create-card "Snake Trap" :id "st")] :minions [(create-minion "War Golem" :id "wg")]}
                                  {:minions [(create-minion "Imp" :id "imp")]} :player-id-in-turn "p2"]) $
                    (play-spell-card $ "p1" "st" {})
                    (end-turn $)
                    (attack-with-minion $ "imp" "wg")
                    (get-minions $ "p1")
                    (filter (fn [m] (= (:name m) "Snake")) $)
                    (count $))
              3)
         ; The Snake Trap should only work once
         (is= (as-> (create-game [{:secrets ["Snake Trap"] :minions [(create-minion "War Golem" :id "wg")]}
                                  {:minions [(create-minion "Imp" :id "imp1") (create-minion "Imp" :id "imp2")]}] :player-id-in-turn "p2") $
                    (attack-with-minion $ "imp1" "wg")
                    (attack-with-minion $ "imp2" "wg")
                    (get-minions $ "p1")
                    (filter (fn [m] (= (:name m) "Snake")) $)
                    (count $))
              3)
         ; Snake Trap should not trigger when player with the trap is attacking
         (is= (as-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}
                                  {:secrets ["Snake Trap"] :minions [(create-minion "Imp" :id "imp")]}] :player-id-in-turn "p2") $
                    (attack-with-minion $ "imp" "wg")
                    (get-minions $ "p2")
                    (filter (fn [m] (= (:name m) "Snake")) $)
                    (count $))
              0))

(deftest sneeds-old-shredder
         (is= (-> (create-game [{:minions [(create-minion "Sneed's Old Shredder" :id "sneeds")]}
                                {:minions [(create-minion "War Golem" :id "wg")]}])
                  (attack-with-minion "sneeds" "wg")
                  (get-minions "p1")
                  (first)
                  (:name)
                  (get-definition)
                  (:rarity))
              :legendary)
         ; A random legendary minion should be summon even if board was full before Sneed's died
         (is= (-> (create-game [{:minions [(create-minion "Sneed's Old Shredder" :id "sneeds")
                                           "Imp" "Imp" "Imp" "Imp" "Imp" "Imp"]}
                                {:minions [(create-minion "War Golem" :id "wg")]}])
                  (attack-with-minion "sneeds" "wg")
                  (get-minions "p1")
                  (count))
              7))