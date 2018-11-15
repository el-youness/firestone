(ns firestone.card_test
  (:require [ysera.test :refer [deftest is is-not is= error?]]
            [firestone.api :refer [attack-with-minion]]
            [firestone.definitions :refer [get-definition]]
            [firestone.core :refer []]
            [firestone.construct :refer [create-game
                                         create-minion
                                         get-hand
                                         create-hero]]))

(deftest acolyte-of-pain
         "Tests for Acolyte of Pain."
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

(deftest loot-hoarder
         "Tests for Loot Hoarder."
         (is= (-> (create-game [{:minions [(create-minion "Loot Hoarder" :id "lh")] :deck ["Imp"]}
                                {:minions [(create-minion "War Golem" :id "wg")]}])
                  (attack-with-minion "lh" "wg")
                  (get-hand "p1")
                  (count))
              1))