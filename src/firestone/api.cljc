(ns firestone.api
  (:require [ysera.test :refer [is is-not is= error?]]
            [ysera.collections :refer [seq-contains?]]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-card
                                        create-game
                                        create-hero
                                        create-minion
                                        get-heroes
                                        get-minion
                                        get-minions]]
            [firestone.core :refer [valid-attack?
                                    get-health
                                    get-attack
                                    damage-minion]]))

; TODO: function "play-card"

; TODO: function "end-turn"
(comment
  "Taken from code base firestone.core"
  (defn end-turn
    [state player-id]
    (assoc state :player-id-in-turn "p2"))
  )

(defn attack-minion
  "Executes minion to minion attack if it is valid."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                  {:minions [(create-minion "War Golem" :id "wg")]}])
                    (attack-minion "p1" "i" "wg")
                    (get-health "wg"))
                (- ((get-definition "War Golem") :health) ((get-definition "Imp") :attack)))
           ; Fatal attack
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                  {:minions [(create-minion "War Golem" :id "wg")]}])
                    (attack-minion "p1" "i" "wg")
                    (get-minion "i"))
                nil)
           ; Invalid attack does nothing
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")
                                             (create-minion "War Golem" :id "wg")]}])
                    (attack-minion "p1" "i" "wg"))
                (create-game [{:minions [(create-minion "Imp" :id "i")
                                         (create-minion "War Golem" :id "wg")]}])))}
  [state player-id attacker-id target-id]
  (if (valid-attack? state player-id attacker-id target-id)
    (let [attacker-attack (get-attack state attacker-id)
          target-attack (get-attack state target-id)]
      (-> (damage-minion state target-id attacker-attack)
          (damage-minion attacker-id target-attack)))
    state))