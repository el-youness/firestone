(ns firestone.api
  (:require [ysera.test :refer [is is-not is= error?]]
            [ysera.collections :refer [seq-contains?]]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-game
                                         create-minion
                                         get-minion
                                         get-minions
                                         create-hero
                                         get-character
                                         update-minion
                                         create-card
                                         get-card-from-hand
                                         remove-card-from-hand
                                         get-player]]
            [firestone.core :refer [valid-attack?
                                    get-health
                                    get-attack
                                    damage-minion
                                    damage-hero
                                    hero?
                                    get-owner
                                    playable?
                                    consume-mana
                                    get-cost
                                    summon-minion
                                    draw-card
                                    restore-mana
                                    add-to-max-mana]]))

; TODO: function "play-card"

(defn end-turn
  "Ends the turn of the playing hero"
  {:test (fn []
           ; The mana increments at the beginning of a turn, a card is drawn and the minion's attacks are reset
           (is= (end-turn (create-game [{:max-mana 5
                                         :deck [(create-minion "Imp" :id "i1")]
                                         :minions [(create-minion "Imp" :id "i3" :attacks-performed-this-turn 1)]}
                                        {:minions [(create-minion "Imp" :id "i2")]}
                                        :minion-ids-summoned-this-turn ["i2"]
                                        :player-id-in-turn "p2"]))
                (create-game [{:max-mana 6
                               :hand [(create-minion "Imp" :id "i1")]
                               :minions [(create-minion "Imp" :id "i3" :attacks-performed-this-turn 0)]}
                              {:minions [(create-minion "Imp" :id "i2")]}
                              :minion-ids-summoned-this-turn []
                              :player-id-in-turn "p1"]))
           ; The mana of each player doesn't increment over 10 mana on a new turn
           ; Player without a card in the deck gets fatigue damage
           (is= (end-turn (create-game [{:hero (create-hero "Jaina Proudmoore")}
                                        {}
                                        :player-id-in-turn "p2" ]))
                (create-game [{:fatigue 2  :hero (create-hero "Jaina Proudmoore" :damage-taken 1)}
                              {}
                              :player-id-in-turn "p1"])))}
  [state]
  (let [pid (get state :player-id-in-turn)]
    (-> state
        ;TODO: trigger the "end of turn" card effects
        (assoc :player-id-in-turn (if (is= "p1" pid) "p1" "p2")
               :minion-ids-summoned-this-turn [])
        (draw-card pid)
        (add-to-max-mana
          (get state :player-id-in-turn)
          (if (>= (get (get-player state pid) :max-mana)
                  10) 0 1))
        (restore-mana pid)
        ;TODO: reset hero power
        ;TODO: trigger the "beginning of turn" card effects
        )
    ))

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

(defn play-card
  "Play a card from the hand if possible."
  {:test (fn []
           (is= (-> (create-game [{:hand [(create-card "War Golem" :id "wg")]}])
                    (play-card "p1" "wg" 0))
                (create-game [{:minions ["War Golem"] :used-mana (:mana-cost (get-definition "War Golem"))}] :minion-ids-summoned-this-turn ["m1"]))
           ; Not enough mana
           (is= (-> (create-game [{:hand [(create-minion "War Golem" :id "wg")] :used-mana 4}])
                    (play-card "p1" "wg" 0))
                (create-game [{:hand [(create-minion "War Golem" :id "wg")] :used-mana 4}]))
           ; No space on board
           (is= (-> (create-game [{:minions ["Imp" "Imp" "Imp" "Imp" "Imp" "Imp" "Imp"] :hand [(create-minion "War Golem" :id "wg")]}])
                    (play-card "p1" "wg" 0))
                (create-game [{:minions ["Imp" "Imp" "Imp" "Imp" "Imp" "Imp" "Imp"] :hand [(create-minion "War Golem" :id "wg")]}])))}
  [state player-id card-id position]
  (let [card (get-card-from-hand state card-id)]
    (if (playable? state player-id card-id)
      (-> (consume-mana state player-id (get-cost card))
          (summon-minion player-id card position)
          (remove-card-from-hand player-id card-id))
      state)))