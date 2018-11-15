(ns firestone.card_test
  (:require [ysera.test :refer [deftest is is-not is= error?]]
            [firestone.api :refer [attack-with-minion]]
            [firestone.definitions :refer [get-definition]]
            [firestone.core :refer []]
            [firestone.construct :refer [create-game
                                         create-minion
                                         create-hero
                                         get-mana]]))

(deftest ancient-watcher
         "Tests for Ancient Watcher."
         (is= (-> (create-game [{:minions [(create-minion "Ancient Watcher" :id "a")]}
                                {:minions [(create-minion "Imp" :id "i")]}])
                  (attack-with-minion "a" "i")
                  (get-mana "p1"))
              10))