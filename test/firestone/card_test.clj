(ns firestone.card_test
  (:require [ysera.test :refer [deftest is is-not is= error?]]
            [firestone.api :refer [attack-with-minion]]
            [firestone.definitions :refer [get-definition]]
            [firestone.core :refer []]
            [firestone.construct :refer [create-game
                                         create-minion
                                         create-hero
                                         get-minion]]))

(deftest ancient-watcher
         "Tests for Ancient Watcher."
         (is= (-> (create-game [{:minions [(create-minion "Ancient Watcher" :id "aw")]}
                                {:minions [(create-minion "War Golem" :id "wg")]}])
                  (attack-with-minion "a" "i")
                  (get-minion "wg")
                  (:damage-taken))
              0))