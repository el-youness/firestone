(ns firestone.card_test
  (:require [ysera.test :refer [deftest is is-not is= error?]]
            [firestone.api :refer [attack-with-minion]]
            [firestone.definitions :refer [get-definition]]
            [firestone.core :refer []]
            [firestone.construct :refer [create-game
                                         create-minion
                                         get-mana
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

(deftest ancient-watcher
         "Tests for Ancient Watcher."
         (is= (-> (create-game [{:minions [(create-minion "Ancient Watcher" :id "a")]}
                                {:minions [(create-minion "Imp" :id "i")]}])
                  (attack-with-minion "a" "i")
                  (get-mana "p1"))
              10))