(ns firestone.core
  (:require [ysera.test :refer [is is-not is= error?]]
            [ysera.collections :refer [seq-contains?]]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-card
                                         create-game
                                         create-hero
                                         create-minion
                                         get-deck
                                         get-cards-from-deck
                                         remove-card-from-deck
                                         fatigue-damage
                                         get-hand
                                         add-card-to-hand
                                         get-hero-id
                                         get-player
                                         get-heroes
                                         get-minion
                                         get-minions
                                         update-minion
                                         remove-minion
                                         update-hero
                                         get-character
                                         get-mana
                                         add-minion-to-board
                                         add-secret-to-player
                                         get-card-from-hand
                                         get-effects
                                         get-secrets
                                         create-secret]]))

(defn get-health
  "Returns the health of the character."
  {:test (fn []
           ; The health of minions
           (is= (get-health (create-minion "War Golem")) 7)
           (is= (get-health (create-minion "War Golem" :damage-taken 2)) 5)
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (get-health "i"))
                1)
           ; The health of heroes
           (is= (get-health (create-hero "Jaina Proudmoore")) 30)
           (is= (get-health (create-hero "Jaina Proudmoore" :damage-taken 2)) 28)
           (is= (-> (create-game [{:hero (create-hero "Jaina Proudmoore" :id "h1")}])
                    (get-health "h1"))
                30)
           ; The health of minions with extra-health
           (is= (get-health (create-minion "War Golem" :effects {:extra-health 3})) 10))}
  ([character]
   {:pre [(map? character) (contains? character :damage-taken)]}
   (let [definition (get-definition character)]
     (- (if (map? (:effects character))
          (+ (:health definition) (get-in character [:effects :extra-health]))
          (:health definition))
        (:damage-taken character))))
  ([state id]
   (get-health (get-character state id))))

(defn get-attack
  "Returns the attack of the minion with the given id."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (get-attack "i"))
                1)
           ; Minion with extra-attack effect
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg" :effects {:extra-attack 2})]}])
                    (get-attack "wg"))
                9))}
  [state id]
  (let [minion (get-minion state id)
        definition (get-definition (:name minion))]
    (+ (:attack definition) (get-in minion [:effects :extra-attack]))))

(defn get-cost
  "Returns the cost of the minion with the given name."
  {:test (fn []
           (is= (-> (create-card "Imp" :id "i")
                    (get-cost))
                1)
           (is= (-> (create-card "Dalaran Mage" :id "i")
                    (get-cost))
                3)
           (is= (-> (create-game [{:hand [(create-card "Dalaran Mage" :id "dm")]}])
                    (get-cost "dm"))
                3))}
  ([card]
   (get (get-definition (:name card)) :mana-cost))
  ([state id]
   (get-cost (get-card-from-hand state id))))

(defn get-card-type
  "Returns the type of the card with the given id or entity."
  {:test (fn []
           (is= (-> (create-card "Imp" :id "i")
                    (get-card-type))
                :minion)
           (is= (-> (create-card "Bananas" :id "i")
                    (get-card-type))
                :spell)
           (is= (-> (create-game [{:hand [(create-card "Dalaran Mage" :id "dm")]}])
                    (get-card-type "dm"))
                :minion))}
  ([card]
   (get (get-definition (:name card)) :type))
  ([state id]
   (get-card-type (get-card-from-hand state id))))

(defn get-owner
  "Returns the player-id of the owner of the character with the given id."
  {:test (fn []
           ; Get owner of hero
           (is= (-> (create-game [{:hero (create-hero "Rexxar" :id "h1")}])
                    (get-owner "h1"))
                "p1")
           ; Get owner of minion
           (is= (-> (create-game [{}
                                  {:minions [(create-minion "Imp" :id "imp")]}])
                    (get-owner "imp"))
                "p2")
           ; Get owner of non-existing character
           (is= (-> (create-game)
                    (get-owner "non-id"))
                nil)
           )}
  [state id]
  (:owner-id (get-character state id)))

(defn sleepy?
  "Checks if the minion with given id is sleepy."
  {:test (fn []
           (is (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}]
                                :minion-ids-summoned-this-turn ["i"])
                   (sleepy? "i")))
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                       (sleepy? "i"))))}
  [state id]
  (seq-contains? (:minion-ids-summoned-this-turn state) id))

(defn hero?
  "Checks if the character with given id is a hero."
  {:test (fn []
           (is (-> (create-game [{:hero (create-hero "Rexxar" :id "h1")}])
                   (hero? "h1")))
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "imp")]}])
                       (hero? "imp"))))}
  [state id]
  (= (-> (get-character state id)
         (get :entity-type))
     :hero))

(defn valid-attack?
  "Checks if the attack is valid."
  {:test (fn []
           ; Should be able to attack an enemy minion
           (is (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                 {:minions [(create-minion "War Golem" :id "wg")]}])
                   (valid-attack? "p1" "i" "wg")))
           ; Should be able to attack an enemy hero
           (is (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                   (valid-attack? "p1" "i" "h2")))
           ; Should not be able to attack your own minions
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i")
                                                (create-minion "War Golem" :id "wg")]}])
                       (valid-attack? "p1" "i" "wg")))
           ; Should not be able to attack if it is not your turn
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                     {:minions [(create-minion "War Golem" :id "wg")]}]
                                    :player-id-in-turn "p2")
                       (valid-attack? "p1" "i" "wg")))
           ; Should not be able to attack if you are sleepy
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}
                                     {:minions [(create-minion "War Golem" :id "wg")]}]
                                    :minion-ids-summoned-this-turn ["i"])
                       (valid-attack? "p1" "i" "wg")))
           ; Should not be able to attack if you already attacked this turn
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i" :attacks-performed-this-turn 1)]}
                                     {:minions [(create-minion "War Golem" :id "wg")]}])
                       (valid-attack? "p1" "i" "wg"))))}
  [state player-id attacker-id target-id]
  (let [attacker (get-minion state attacker-id)
        target (get-character state target-id)]
    (and (= (:player-id-in-turn state) player-id)
         (< (:attacks-performed-this-turn attacker) 1)
         (not (sleepy? state attacker-id))
         (not= (:owner-id attacker) (:owner-id target)))))

(defn handle-triggers
  "Handle the triggers of multiple event listeners."
  {:test (fn []
           (is= (-> (create-game)
                    (handle-triggers :on-damage))
                (create-game))
           (is= (-> (create-game [{:deck ["Imp"] :minions [(create-minion "Acolyte of Pain" :id "m1")]}])
                    (handle-triggers :on-damage "m1")
                    (get-hand "p1")
                    (count))
                1))}
  [state event & args]
  (->> (concat (get-minions state) (get-secrets state))
       (reduce (fn [state entity]
                 (let [effects (get-effects entity)]
                   (if (contains? effects event)
                     ((get-definition (effects event)) state (:id entity) args)
                     state)))
               state)))

(defn change-minion-board-side
  "Causes a minion on the board to switch board side and owner."
  {:test (fn []
           (is= (as-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}]) $
                      (change-minion-board-side $ "wg")
                      [(get-owner $ "wg")
                       (count (get-minions $ "p1"))
                       (count (get-minions $ "p2"))])
                ["p2" 0 1]))}
  [state id]
  (let [minion (get-minion state id)
        new-owner-id (if (= (get-owner state id) "p1")
                       "p2"
                       "p1")]
    (-> (remove-minion state id)
        (add-minion-to-board {:player-id new-owner-id
                              :minion    minion
                              :position  0}))))

(defn destroy-minion
  "Causes a minion on the board to die. Should trigger deathrattles and other on death effects."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "wg")]}])
                    (destroy-minion "wg")
                    (get-minions))
                [])
           (is= (-> (create-game [{:minions [(create-minion "Loot Hoarder" :id "lh")] :deck ["Imp"]}])
                    (destroy-minion "lh")
                    (get-hand "p1")
                    (count))
                1))}
  [state id]
  (-> (let [effects (get-effects (get-minion state id))]
        (if (contains? effects :deathrattle)
          (let [deathrattle (get-definition (effects :deathrattle))
                owner-id (get-owner state id)]
            (-> (remove-minion state id)
                (deathrattle owner-id)))
          (remove-minion state id)))))

(defn damage-minion
  "Deals damage to the minion with the given id."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "i")]}])
                    (damage-minion "i" 3)
                    (get-health "i"))
                (as-> (get-definition "War Golem") $
                      (- ($ :health) 3)))
           (is= (-> (create-game [{:minions [(create-minion "War Golem" :id "i" :damage-taken 1)]}])
                    (damage-minion "i" 1)
                    (get-health "i"))
                (as-> (get-definition "War Golem") $
                      (- ($ :health) 2)))
           ; Remove minion if dead
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (damage-minion "i" 1)
                    (get-minions))
                []))}
  [state id damage]
  (let [state (update-minion state id :damage-taken (partial + damage))]
    (let [state (handle-triggers state :on-damage id)]
      (if (> (get-health state id) 0)
        state
        (destroy-minion state id)))))

(defn damage-hero
  "Deals damage to the hero with the given id."
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Rexxar")}
                                  {:hero (create-hero "Uther Lightbringer")}])
                    (damage-hero "h1" 5)
                    (get-health "h1"))
                (as-> (get-definition "Rexxar") $
                      (- ($ :health) 5)))
           (is= (-> (create-game [{:hero (create-hero "Rexxar" :damage-taken 2)}
                                  {:hero (create-hero "Uther Lightbringer")}])
                    (damage-hero "h1" 5)
                    (get-health "h1"))
                (as-> (get-definition "Rexxar") $
                      (- ($ :health) 7))))}
  [state id damage]
  (let [state (update-hero state id :damage-taken (partial + damage))]
    (if (> (get-health state id) 0)
      state
      ; TODO: game should be over
      state))
  )

(defn heal-hero
  "Reduces damage taken of a hero by the given amount."
  {:test (fn []
           (is= (-> (create-game [{:hero (create-hero "Rexxar" :id "h1" :damage-taken 10)}])
                    (heal-hero "h1" 5)
                    (get-health "h1"))
                25)
           (is= (-> (create-game [{:hero (create-hero "Rexxar" :id "h1" :damage-taken 10)}])
                    (heal-hero "h1" 20)
                    (get-health "h1"))
                30))}
  [state id amount]
  (update-hero state id :damage-taken (fn [x] (max (- x amount) 0))))

(defn summon-minion
  "Plays a minion card-"
  {:test (fn []
           ; Play minion card on empty board
           (is= (-> (create-game)
                    (summon-minion "p1" (create-card "Imp" :id "c1")))
                (create-game [{:minions ["Imp"]}] :minion-ids-summoned-this-turn ["m1"]))
           ; Play a minion card on a board with one minion
           (is= (-> (create-game [{:minions ["War Golem"]}])
                    (summon-minion "p1" (create-card "Imp" :id "c1") 1))
                (create-game [{:minions ["War Golem" "Imp"]}] :minion-ids-summoned-this-turn ["m2"]))
           ; No state change if board is already full
           (is= (-> (create-game [{:minions [(create-minion "War Golem")
                                             (create-minion "War Golem")
                                             (create-minion "War Golem")
                                             (create-minion "War Golem")
                                             (create-minion "War Golem")
                                             (create-minion "War Golem")
                                             (create-minion "War Golem")]}])
                    (summon-minion "p1" (create-card "Imp" :id "c1")))
                (create-game [{:minions [(create-minion "War Golem")
                                         (create-minion "War Golem")
                                         (create-minion "War Golem")
                                         (create-minion "War Golem")
                                         (create-minion "War Golem")
                                         (create-minion "War Golem")
                                         (create-minion "War Golem")]}]))
           )}
  ([state player-id card position]
   (if (< (count (get-minions state player-id)) 7)
     (let [minion (create-minion (if (string? card)
                                   card
                                   (:name card)))]
       (add-minion-to-board state {:player-id player-id :minion minion :position position}))
     state))
  ([state player-id card]
   (summon-minion state player-id card 0)))

(defn draw-card
  "Draw a card from a player's deck and put it in the hand. This is only done if the hand is not full
  and there are cards in the deck."
  {:test (fn []
           ; Test to draw a card when the player has a card in the deck
           (is= (-> (create-game [{:deck [(create-card "Imp")]}])
                    (draw-card "p1"))
                (create-game [{:hand [(create-card "Imp")]}]))
           ; Test to draw a card when the player's hand is full
           (is= (-> (create-game [{:hand [(create-card "Imp") (create-card "Imp") (create-card "Imp") (create-card "Imp")
                                          (create-card "Imp") (create-card "Imp") (create-card "Imp") (create-card "Imp")
                                          (create-card "Imp") (create-card "Imp")]
                                   :deck [(create-card "War Golem")]}])
                    (draw-card "p1"))
                (-> (create-game [{:hand [(create-card "Imp") (create-card "Imp") (create-card "Imp") (create-card "Imp")
                                          (create-card "Imp") (create-card "Imp") (create-card "Imp") (create-card "Imp")
                                          (create-card "Imp") (create-card "Imp")]}])
                    (update :counter inc)))
           ; Test that a player takes fatigue damage if there are no cards in the deck
           (is= (-> (create-game)
                    (draw-card "p1"))
                (create-game [{:fatigue 2 :hero (create-hero "Jaina Proudmoore" :damage-taken 1)}]))
           ; Test that the player takes increased damage when drawing multiple times from an empty deck
           (is= (-> (create-game)
                    (draw-card "p1")
                    (draw-card "p1"))
                (create-game [{:fatigue 3 :hero (create-hero "Jaina Proudmoore" :damage-taken 3)}]))
           )}
  ([state player-id]
   {:pre [(map? state) (string? player-id)]}
    ; Check if there are cards in the deck
   (if (empty? (get-deck state player-id))
     (let [[state damage] (fatigue-damage state player-id)]
       (damage-hero state (get-hero-id state player-id) damage))
     (let [card (first (get-cards-from-deck state player-id 1))]
       (let [state (remove-card-from-deck state player-id (:id card))]
         ; Check that the hand is not full
         (if (< (count (get-hand state player-id)) 10)
           (add-card-to-hand state {:player-id player-id :card card})
           state))))))

(defn mulligan
  "Take x cards from player 1's deck and y cards from player 2's deck. The cards are removed from the
  player's decks and put into their hands."
  {:test (fn []
           ; Test mulligan with the same amount of cards as in the players' decks
           (is= (-> (create-game [{:deck [(create-card "Imp")]}
                                  {:deck [(create-card "Imp")]}])
                    (mulligan 1 1))
                (create-game [{:hand [(create-card "Imp")]}
                              {:hand [(create-card "Imp")]}]))
           ; Test mulligan with more than the amount of cards in the players' decks
           (is= (-> (create-game [{:deck [(create-card "Imp")]}
                                  {:deck [(create-card "Imp")]}])
                    (mulligan 2 2))
                (create-game [{:hand [(create-card "Imp")]}
                              {:hand [(create-card "Imp")]}]))
           ; Test mulligan when the players' decks are empty
           (is= (-> (create-game)
                    (mulligan 1 1))
                (create-game))
           )}
  ([state x y]
   {:pre [(map? state) (number? x) (number? y)]}
   (reduce (fn [state {player-id :player-id cards :cards}]
             (reduce (fn [state card]
                       (-> (add-card-to-hand state {:player-id player-id :card card})
                           (remove-card-from-deck player-id (:id card))))
                     state
                     cards)
             )
           state
           (map-indexed (fn [index amount]
                          (let [player-id (str "p" (inc index))]
                            {:player-id player-id
                             :cards     (get-cards-from-deck state player-id amount)}))
                        [x y])

           )))

(defn playable?
  "Checks if a card is playable on the board for a specific player"
  {:test (fn []
           (is (-> (create-game [{:hand [(create-card "Imp" :id "c1")] :max-mana 1}])
                   (playable? "p1" "c1"))
               )
           (is-not (-> (create-game [{:hand [(create-card "Imp" :id "c1")] :max-mana 0}])
                       (playable? "p1" "c1"))
                   )
           (is-not (-> (create-game [{:max-mana 5 :hand [(create-card "Imp" :id "c1")]
                                      :minions  ["War Golem" "War Golem" "War Golem" "War Golem" "War Golem" "War Golem" "War Golem"]}])
                       (playable? "p1" "c1"))
                   )
           )}
  [state player-id card-id]
  (let [available-mana (get-mana state player-id)
        card-cost (get-cost state card-id)
        minions-on-board (get-minions state player-id)
        card-type (get-card-type state card-id)]
    (and (<= card-cost available-mana)
         (if (= card-type :minion)
           (< (count minions-on-board) 7)
           true))))

(defn valid-target?
  "Checks if the target of a card is valid"
  {:test (fn []
           ; A card with :target-type :all-minions can target minion
           (is (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]
                                  :hand    [(create-card "Bananas" :id "c1")]}])
                   (valid-target? "p1" "c1" "i1")))
           ; A card with :target-type :all-minions cannot target hero
           (is-not (-> (create-game [{:hand [(create-card "Bananas" :id "c1")]
                                      :hero (create-hero "Anduin Wrynn")}])
                       (valid-target? "p1" "c1" "h1")))
           ; A card with :target-type :enemy-minions can target enemy minion
           (is (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]}
                                 {:hand [(create-card "Mind Control" :id "c1")]}])
                   (valid-target? "p2" "c1" "i1")))
           ; A card with :target-type :enemy-minions cannot target friendly minion
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]
                                      :hand    [(create-card "Mind Control" :id "c1")]}])
                       (valid-target? "p1" "c1" "i1")))
           ; A card with no :target-type cannot have a valid target
           (is-not (-> (create-game [{:minions [(create-minion "Imp" :id "i1") (create-minion "Imp" :id "i2")]}])
                       (valid-target? "p1" "i1" "i2"))))}
  [state player-id card-id target-id]
  (let [card (get-card-from-hand state card-id)
        target-type (:target-type card)]
    (cond (nil? target-type)
          false

          (nil? (get-minion state target-id))
          false

          (= target-type :all-minions)
          true

          (= target-type :enemy-minions)
          (if (= (get-owner state target-id) player-id)
            false
            true)

          ; TODO: Add checks for other target-type
          :else
          false)))

(defn play-secret
  "Puts a secret into play if there is space."
  {:test (fn []
           (is= (-> (create-game)
                    (play-secret "p1" (create-secret "Snake Trap"))
                    (get-secrets)
                    (count))
                1)
           ; Cannot have more than 5 secrets in play
           (is= (-> (create-game [{:secrets ["Snake Trap" "Snake Trap" "Snake Trap" "Snake Trap" "Snake Trap"]}])
                    (play-secret "p1" (create-secret "Snake Trap"))
                    (get-secrets)
                    (count))
                5))}
  [state player-id secret]
  (println secret)
  (if (< (count (get-secrets state player-id)) 5)
    (add-secret-to-player state player-id secret)
    state))

(defn get-spell-function
  "Get the spell function in the definition of a card."
  {:test (fn []
           (is= (as-> (create-game [{:minions [(create-minion "Imp" :id "i1")]}]) $
                      ((get-spell-function (create-card "Bananas")) $ "i1")
                      [(get-attack $ "i1") (get-health $ "i1")])
                [2 2])
           (is= (as-> (create-game []) $
                      ((get-spell-function (create-card "Snake Trap")) $)
                      (get-secrets $ "p1")
                      (count $))
                1))}
  [card]
  (if (= (:subtype card) :secret)
    (fn [state] (play-secret state (:player-id-in-turn state) (create-secret (:name card))))
    (:spell (get-definition card))))

(defn get-battlecry-function
  "Get the battlecry function in the definition of a card."
  {:test (fn []
           (is= (as-> (create-game [{:minions [(create-minion "Eater of Secrets" :id "es")]}
                                    {:secrets ["Snake Trap"]}]) $
                      ((get-battlecry-function (create-card "Eater of Secrets")) $ "es")
                      (get-secrets $)
                      (count $))
                0))}
  [card]
  (:battlecry (get-definition card)))

(defn consume-mana
  "Consume a given amount of a player's mana."
  {:test (fn []
           (is= (-> (create-game [{:used-mana 2}])
                    (consume-mana "p1" 5)
                    (get-mana "p1"))
                3))}
  [state player-id amount]
  (update-in state [:players player-id :used-mana] (partial + amount)))

(defn restore-mana
  "resets the consumed amount of a player's mana."
  {:test (fn []
           (is= (-> (create-game [{:used-mana 3}])
                    (restore-mana "p1")
                    (get-mana "p1"))
                10))}
  [state player-id]
  (assoc-in state [:players player-id :used-mana] 0))

(defn add-to-max-mana
  "Adds a given amount of mana to the max-mana pool of a player (doesn't affect consumed mana)"
  {:test (fn []
           (is= (-> (create-game [{:max-mana 3}])
                    (add-to-max-mana "p1" 2)
                    (get-mana "p1"))
                5))}
  [state player-id amount]
  (update-in state [:players player-id :max-mana] (partial + amount)))