(ns firestone.api
  (:require [ysera.test :refer [is is-not is= error?]]
            [ysera.collections :refer [seq-contains?]]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-game
                                         create-minion
                                         get-minion
                                         create-hero
                                         get-character]]
            [firestone.core :refer [valid-attack?
                                    get-health
                                    get-attack
                                    damage-minion
                                    damage-hero]]))

; TODO: function "play-card"

; TODO: function "end-turn"
(comment
  "Taken from code base firestone.core"
  (defn end-turn
    [state player-id]
    (assoc state :player-id-in-turn "p2"))
  )

(defn attack-with-minion
  "Executes minion to minion attack if it is valid."
  {:test (fn []
           ; Attack hero
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (attack-with-minion "p1" "i" "h2")
                    (get-health "h2"))
                (- ((get-definition "Jaina Proudmoore") :health) ((get-definition "Imp") :attack)))
           ; Attack minion
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                  {:minions [(create-minion "War Golem" :id "wg")]}])
                    (attack-with-minion "p1" "i" "wg")
                    (get-health "wg"))
                (- ((get-definition "War Golem") :health) ((get-definition "Imp") :attack)))
           ; Fatal attack
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                  {:minions [(create-minion "War Golem" :id "wg")]}])
                    (attack-with-minion "p1" "i" "wg")
                    (get-minion "i"))
                nil)
           ; Invalid attack does nothing
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")
                                             (create-minion "War Golem" :id "wg")]}])
                    (attack-with-minion "p1" "i" "wg"))
                (create-game [{:minions [(create-minion "Imp" :id "i")
                                         (create-minion "War Golem" :id "wg")]}])))}
  [state player-id attacker-id target-id]
  (if (valid-attack? state player-id attacker-id target-id)
    (let [attacker-attack (get-attack state attacker-id)]
      (if (= (-> (get-character state target-id)
              (get :entity-type))
              :hero)
        (damage-hero state target-id attacker-attack)
        (let [target-attack (get-attack state target-id)]
          (-> (damage-minion state target-id attacker-attack)
              (damage-minion attacker-id target-attack)))))
    state))