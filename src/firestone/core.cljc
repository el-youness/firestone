(ns firestone.core
  (:require [ysera.test :refer [is is-not is= error?]]
            [ysera.collections :refer [seq-contains?]]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-card
                                         create-game
                                         create-hero
                                         create-minion
                                         get-heroes
                                         get-minion
                                         get-minions
                                         update-minion
                                         remove-minion
                                         update-hero
                                         get-character]]))

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
                30))}
  ([character]
   {:pre [(map? character) (contains? character :damage-taken)]}
   (let [definition (get-definition character)]
     (- (:health definition) (:damage-taken character))))
  ([state id]
   (get-health (get-character state id))))

(defn get-attack
  "Returns the attack of the minion with the given id."
  {:test (fn []
           (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i")]}])
                    (get-attack "i"))
                1))}
  [state id]
  (let [minion (get-minion state id)
        definition (get-definition (:name minion))]
    (:attack definition)))

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
  "Checks if the character with given id is a hero"
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
  "Checks if the attack is valid"
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
                    (get-minion "i"))
                nil))}
  [state id damage]
  (let [state (update-minion state id :damage-taken (+ damage (-> (get-minion state id)
                                                                  (:damage-taken))))]
    (if (> (get-health state id) 0)
      state
      (remove-minion state id)))
  )

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
  (let [state (update-hero state id  :damage-taken (+ damage (-> (get-character state id)
                                                                 (:damage-taken))))]
    (if (> (get-health state id) 0)
      state
      ; TODO: game should be over
      state))
  )