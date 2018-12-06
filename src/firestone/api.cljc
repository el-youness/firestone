(ns firestone.api
  (:require [ysera.test :refer [is is-not is= error?]]
            [ysera.collections :refer [seq-contains?]]
            [ysera.error :refer [error]]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-game
                                         get-player
                                         get-player-id-in-turn
                                         opposing-player-id
                                         switch-player-in-turn
                                         create-minion
                                         get-minion
                                         get-player-id-in-turn
                                         get-minions
                                         create-hero
                                         get-hero-power-of-player
                                         get-board-entity
                                         get-minion-ids-summoned-this-turn
                                         reset-minion-ids-summoned-this-turn
                                         update-minion
                                         create-card
                                         get-card-from-hand
                                         remove-card-from-hand
                                         update-hero-power
                                         hero?
                                         decrement-buff-counters]]
            [firestone.core :refer [valid-attack?
                                    get-health
                                    get-attack
                                    damage-minion
                                    damage-hero
                                    valid-play?
                                    valid-plays
                                    valid-attacks
                                    get-owner
                                    get-spell-function
                                    get-hero-power-function
                                    get-battlecry-function
                                    battlecry-minion-with-target?
                                    consume-mana
                                    get-cost
                                    summon-minion
                                    draw-card
                                    restore-mana
                                    add-to-max-mana
                                    reset-minion-attack-this-turn
                                    unfreeze-characters
                                    handle-triggers]]))

(defn end-turn
  "Ends the turn of the playing hero"
  {:test (fn []
           ; The mana increments at the beginning of a turn, a card is drawn and the minion's attacks are reset
           (is= (end-turn (create-game [{:max-mana 5
                                         :deck     [(create-card "Imp" :id "i1")]
                                         :minions  [(create-minion "Imp" :id "i3" :attacks-performed-this-turn 1)]}
                                        {:minions [(create-minion "Imp" :id "i2")]}]
                                       :player-id-in-turn "p2"
                                       :minion-ids-summoned-this-turn ["i2"]))
                (create-game [{:max-mana 6
                               :hand     [(create-card "Imp" :id "i1")]
                               :minions  [(create-minion "Imp" :id "i3" :attacks-performed-this-turn 0)]}
                              {:minions [(create-minion "Imp" :id "i2")]}]
                             :player-id-in-turn "p1"
                             :minion-ids-summoned-this-turn []))
           ; The mana of each player doesn't increment over 10 mana on a new turn
           ; Player without a card in the deck gets fatigue damage
           (is= (end-turn (create-game [{:hero (create-hero "Jaina Proudmoore")}]
                                       :player-id-in-turn "p2"))
                (create-game [{:fatigue 2 :hero (create-hero "Jaina Proudmoore" :damage-taken 1)}]
                             :player-id-in-turn "p1")))}
  [state]
  (let [old-pid (get-player-id-in-turn state)]
    (let [new-pid (opposing-player-id old-pid)]
      (-> state
          ; End of turn events for player
          (decrement-buff-counters)
          (unfreeze-characters)
          (reset-minion-ids-summoned-this-turn)
          (switch-player-in-turn)
          (handle-triggers :on-start-turn)
          (draw-card new-pid)
          (add-to-max-mana new-pid 1)
          (restore-mana new-pid)
          (update-hero-power new-pid :used false)
          (reset-minion-attack-this-turn new-pid)))))

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
           ; A minion cannot attack twice on the same turn so throws an error
           (is (as-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                    {:hero (create-hero "Rexxar")}]) $
                      (attack-with-minion $ "i" "h2")
                      (error? (attack-with-minion $ "i" "h2"))))
           ; Invalid attack throws an error
           (is (as-> (create-game [{:minions [(create-minion "Imp" :id "i")
                                             (create-minion "War Golem" :id "wg")]}]) $
                     (error? (attack-with-minion $ "i" "wg")))))}
  [state attacker-id target-id]
  (if (valid-attack? state (get-player-id-in-turn state) attacker-id target-id)
    (let [state (-> (update-minion state attacker-id :attacks-performed-this-turn 1)
                    (handle-triggers :on-attack target-id))
          attacker-attack (get-attack state attacker-id)]
      (if (hero? state target-id)
        (damage-hero state target-id attacker-attack)
        (let [target-attack (get-attack state target-id)]
          (-> (damage-minion state target-id attacker-attack)
              (damage-minion attacker-id target-attack)))))
    (error "Not valid attack-with-minion")))

(defn play-spell-card
  "Play a spell card from the hand if possible."
  {:test (fn []
           ; Play spell card that can target all minions
           (is= (as-> (create-game [{:hand    [(create-card "Bananas" :id "b1")]
                                     :minions [(create-minion "Imp" :id "i")]}]) $
                      (play-spell-card $ "p1" "b1" {:target-id "i"})
                      [(get-health $ "i") (get-attack $ "i")])
                [2 2])
           ; Play spell card that can only target enemy minions
           (is= (-> (create-game [{:hand [(create-card "Mind Control" :id "mc1")]}
                                  {:minions [(create-minion "Imp" :id "i")]}])
                    (play-spell-card "p1" "mc1" {:target-id "i"})
                    (get-owner "i"))
                "p1")
           ; Throw an error on misclick or hax-attempt
           (let [state (create-game [{:hand [(create-card "Mind Control" :id "mindgames")
                                             (create-card "Bananas" :id "bananas")
                                             (create-card "Snake Trap" :id "snakes")
                                             (create-card "Snake Trap" :id "snakes2")]
                                      :minions [(create-minion "Imp" :id "i1")]}
                                     {:minions [(create-minion "Imp" :id "i2")]}])]
             (is (error? (play-spell-card state "p1" "mindgames" {})))
             (is (error? (play-spell-card state "p1" "mindgames" {:target-id "h2"})))
             (is (error? (play-spell-card state "p1" "mindgames" {:target-id "i1"})))
             (is (error? (play-spell-card state "p1" "bananas" {:target-id "h1"})))
             (is (error? (play-spell-card state "p1" "bananas" {})))
             (is (error? (play-spell-card state "p1" "snakes" {:target-id "h2"})))
             (is (error? (-> state
                             (play-spell-card "p1" "snakes" {})
                             (play-spell-card "p1" "snakes2" {}))))))}
  [state player-id card-id {target-id :target-id}]
  (if (valid-play? state card-id target-id)
    (let [card (get-card-from-hand state card-id)]
      (-> (if (nil? target-id)
            ((get-spell-function card) state)
            ((get-spell-function card) state target-id))
          (consume-mana player-id (get-cost card))
          (remove-card-from-hand player-id card-id)))
    (error "You cannot play the spell like this you fool.\n")))

(defn play-minion-card
  "Play a minion card from the hand if possible."
  {:test (fn []
           ; Play minion
           (is= (-> (create-game [{:hand [(create-card "War Golem" :id "wg")]}])
                    (play-minion-card "p1" "wg" {:position 0}))
                (create-game [{:minions   ["War Golem"]
                               :used-mana (:mana-cost (get-definition "War Golem"))}]
                             :minion-ids-summoned-this-turn ["m1"]))
           ; Play battlecry minion when there is an available target
           (is= (-> (create-game [{:hand [(create-card "Big Game Hunter" :id "bgh")]}
                                  {:minions [(create-minion "War Golem" :id "wg")]}])
                    (play-minion-card "p1" "bgh" {:position 0 :target-id "wg"})
                    (get-minions "p2")
                    (count))
                0)
           ; Play battlecry minion when there are no available targets
           (is= (-> (create-game [{:hand [(create-card "Big Game Hunter" :id "bgh")]}])
                    (play-minion-card "p1" "bgh" {:position 0})
                    (get-minions "p1")
                    (count))
                1)
           ; Throw error (bad target)
           (is (error? (-> (create-game [{:hand [(create-card "Big Game Hunter" :id "bgh")]}
                                         {:minions [(create-minion "Imp" :id "i")]}])
                           (play-minion-card "p1" "bgh" {:position 0 :target-id "i"}))))
           ; Throw error (not enough mana)
           (is (error? (-> (create-game [{:hand [(create-card "Imp" :id "i")]
                                          :used-mana 10}])
                           (play-minion-card "p1" "i" {:position 0})))))}
  [state player-id card-id {position :position target-id :target-id}]
  (if (valid-play? state card-id target-id)
    (let [card (get-card-from-hand state card-id)
          battlecry-function (get-battlecry-function card)
          state (-> (consume-mana state player-id (get-cost card))
                    (summon-minion player-id card position)
                    (remove-card-from-hand player-id card-id))
          minion-id (-> (get-minion-ids-summoned-this-turn state)
                        (last))]
      (if battlecry-function
        (if target-id
          (battlecry-function state minion-id target-id)
          (if (battlecry-minion-with-target? card)
            state
            (battlecry-function state minion-id)))
        state))
    (error "Sorry, you cannot play this card.\n")))

(defn use-hero-power
  "Use the hero power of the hero belonging to the given player id."
  {:test (fn []
           ; Use a hero power that has no targets
           (is= (-> (create-game [{:hero "Uther Lightbringer"}])
                    (use-hero-power "p1" {})
                    (get-minions "p1")
                    (count))
                1)
           ; Use a hero power that can target everything on the board
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]}
                                  {:minions [(create-minion "Imp" :id "i2")]}])
                    (use-hero-power "p1" {:target-id "i2"})
                    (get-minions "p2")
                    (count))
                0)
           ; Throw an error on misclick or hax-attempt
           (is (error? (-> (create-game [{:used-mana 9}
                                         {:minions [(create-minion "Imp" :id "i2")]}])
                           (use-hero-power "p1" {:target-id "i2"}))))
           (is (error? (-> (create-game [{}
                                         {:minions [(create-minion "Imp" :id "i2")]}])
                           (use-hero-power "p1" {}))))
           (is (error? (-> (create-game [{:hero (create-hero "Uther Lightbringer")}
                                         {:minions [(create-minion "Imp" :id "i2")]}])
                           (use-hero-power "p1" {:target-id "i2"})))))}
  [state player-id {target-id :target-id}]
  (let [hero-power (get-hero-power-of-player state player-id)]
    (if (valid-play? state (:id hero-power) target-id)
      (-> (if (nil? target-id)
            ((get-hero-power-function hero-power) state)
            ((get-hero-power-function hero-power) state target-id))
          (consume-mana player-id (get-cost hero-power))
          (update-hero-power player-id :used true))
      (error "You cannot play your hero power like that.\n"))))
