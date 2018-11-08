(ns firestone.api
  (:require [ysera.test :refer [is is-not is= error?]]
            [ysera.collections :refer [seq-contains?]]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-game
                                         create-minion
                                         get-minion
                                         create-hero
                                         get-character
                                         update-minion]]
            [firestone.core :refer [valid-attack?
                                    get-health
                                    get-attack
                                    damage-minion
                                    damage-hero
                                    hero?
                                    get-owner]]))

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
           ; Attack the opponent's hero
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                  {:hero (create-hero "Rexxar")}])
                    (attack-with-minion "i" "h2"))
                (create-game [{:minions [(create-minion "Imp" :id "i" :attacks-performed-this-turn 1)]}
                              {:hero (create-hero "Rexxar" :damage-taken ((get-definition "Imp") :attack))}]))
           ; Attack an enemy minion
           (is= (-> (create-game [{:minions [(create-minion "Dalaran Mage" :id "m1")]}
                                  {:minions [(create-minion "Dalaran Mage" :id "m2")]}]
                                 :player-id-in-turn "p2")
                    (attack-with-minion "m2" "m1"))
                (create-game [{:minions [(create-minion "Dalaran Mage" :id "m1" :damage-taken ((get-definition "Dalaran Mage") :attack))]}
                              {:minions [(create-minion "Dalaran Mage" :id "m2" :damage-taken ((get-definition "Dalaran Mage") :attack) :attacks-performed-this-turn 1)]}]
                             :player-id-in-turn "p2"))
           ; Attack and kill an enemy minion
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                  {:minions [(create-minion "War Golem" :id "wg")]}]
                                 :player-id-in-turn "p2")
                    (attack-with-minion "wg" "i"))
                (create-game [{}
                              {:minions [(create-minion "War Golem" :id "wg" :damage-taken ((get-definition "Imp") :attack) :attacks-performed-this-turn 1)]}]
                             :player-id-in-turn "p2"))
           ; A minion cannot attack twice on the same turn
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                  {:hero (create-hero "Rexxar")}])
                    (attack-with-minion "i" "h2")
                    (attack-with-minion "i" "h2"))
                (create-game [{:minions [(create-minion "Imp" :id "i" :attacks-performed-this-turn 1)]}
                              {:hero (create-hero "Rexxar" :damage-taken ((get-definition "Imp") :attack))}]))
           ; Invalid attack does nothing
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")
                                             (create-minion "War Golem" :id "wg")]}])
                    (attack-with-minion "i" "wg"))
                (create-game [{:minions [(create-minion "Imp" :id "i")
                                         (create-minion "War Golem" :id "wg")]}])))}
  [state attacker-id target-id]
  (if (valid-attack? state (get-owner state attacker-id) attacker-id target-id)
    (let [state (update-minion state attacker-id :attacks-performed-this-turn 1)
          attacker-attack (get-attack state attacker-id)]
      (if (hero? state target-id)
        (damage-hero state target-id attacker-attack)
        (let [target-attack (get-attack state target-id)]
          (-> (damage-minion state target-id attacker-attack)
              (damage-minion attacker-id target-attack)))))
    state))