(ns firestone.hero-tests
  (:require [clojure.test :refer [function?
                                  deftest]]
            [ysera.test :refer [is is-not is= error?]]
            [firestone.construct :refer [create-game
                                         create-minion
                                         create-card
                                         create-secret
                                         update-minion
                                         update-in-minion
                                         update-in-hero
                                         get-character
                                         get-minion
                                         get-minions
                                         get-secrets
                                         get-effects
                                         remove-secrets
                                         get-hero
                                         get-minion-effects]]
            [firestone.core :refer [change-minion-board-side
                                    get-owner
                                    get-attack
                                    get-health
                                    damage-minion
                                    damage-hero
                                    valid-plays
                                    destroy-minion]]
            [firestone.api :refer [play-minion-card
                                   use-hero-power]]))


(deftest fireblast-test
         (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]}
                                {:minions [(create-minion "Imp" :id "i2")]}])
                  (use-hero-power "p1" {:target-id "i2"})
                  (get-minions "p2")
                  (count))
              0))
