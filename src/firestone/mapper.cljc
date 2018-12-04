(ns firestone.mapper
  (:require [ysera.test :refer [is is=]]
            [clojure.spec.alpha :as spec]
            [clojure.set :refer [rename-keys union]]
            [firestone.spec]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-game
                                         get-player-id-in-turn
                                         get-players
                                         create-minion
                                         create-hero
                                         get-minions
                                         get-deck
                                         get-mana
                                         get-max-mana
                                         get-secrets
                                         get-hand
                                         get-hero
                                         get-hero-power
                                         get-hero-power-of-player]]
            [firestone.core :refer [get-attack
                                    get-health
                                    get-extra-health
                                    get-cost
                                    get-owner
                                    valid-plays
                                    valid-attacks
                                    frozen?
                                    deathrattle-minion?
                                    available-targets
                                    sleepy?]]))

(defn get-states
  "Returns the states of the character."
  {:test (fn []
           (is= (-> (create-minion "Loot Hoarder")
                    (get-states))
                #{"DEATHRATTLE"})
           (is= (-> (create-hero "Rexxar" :buffs [{:frozen true}])
                    (get-states))
                #{"FROZEN"})
           (is= (-> (create-minion "Loot Hoarder" :buffs [{:frozen true}])
                    (get-states))
                #{"DEATHRATTLE" "FROZEN"}))}
  [character]
  (as-> #{} $
        (if (frozen? character)
          (conj $ "FROZEN")
          $)
        (if (deathrattle-minion? character)
          (conj $ "DEATHRATTLE")
          $))
  )

(defn core-hero-power->client-hero-power
  {:test (fn []
           (is (spec/valid? :firestone.spec/hero-power
                            (let [state (create-game)
                                  hero-power (get-hero-power-of-player state "p1")]
                              (core-hero-power->client-hero-power state hero-power)))))}
  [state hero-power]
  (let [definition (get-definition hero-power)
        id (:id hero-power)
        owner-id (get-owner hero-power)]
    {:can-use            (contains? (valid-plays state) id)
     :owner-id           owner-id
     :entity-type        "hero-power"
     :has-used-your-turn (:used hero-power)
     :name               (:name definition)
     :description        (:description definition)
     :mana-cost          (get-cost hero-power)
     :original-mana-cost (:mana-cost definition)
     :valid-target-ids   (available-targets state owner-id id)}))

(defn core-hero->client-hero
  {:test (fn []
           (is (spec/valid? :firestone.spec/hero
                            (let [state (create-game)
                                  hero (get-hero state "p1")]
                              (core-hero->client-hero state hero)))))}
  [state hero]
  (let [definition (get-definition hero)
        id (:id hero)
        owner-id (get-owner hero)]
    {:name             (:name definition)
     :owner-id         owner-id
     :entity-type      "hero"
     :attack           0
     :can-attack       (contains? (valid-attacks state) id)
     :health           (get-health hero)
     :id               id
     :mana             (get-mana state owner-id)
     :max-health       (:health definition)
     :max-mana         (get-max-mana state owner-id)
     :states           (get-states hero)
     :valid-attack-ids (or (get (valid-attacks state) id)
                           [])
     :armor            0
     :class            (name (:class definition))
     :hero-power       (core-hero-power->client-hero-power state (get-hero-power-of-player state owner-id))
     }))

(defn core-card->client-card
  {:test (fn []
           (is (spec/valid? :firestone.spec/card
                            (let [state (create-game [{:hand ["Frostbolt"]}])
                                  card (-> (get-hand state "p1")
                                           (first))]
                              (core-card->client-card state card)))))}
  [state card]
  (let [definition (get-definition card)
        id (:id card)
        owner-id (get-owner card)]
    (merge
      {:entity-type      "card"
       :owner-id         owner-id
       :id               id
       :playable         (contains? (valid-plays state) id)
       :valid-target-ids (available-targets state owner-id id)}

      ; Values from definition
      (reduce-kv (fn [m k v]
                   (cond
                     (= :name k) (assoc m k v)
                     (= :description k) (assoc m k v)
                     (= :mana-cost k) (-> (assoc m k v)
                                          (assoc :original-mana-cost v))
                     (= :type k) (assoc m k (name v))
                     (= :class k) (assoc m k (name v))
                     (= :rarity k) (assoc m k (name v))
                     (= :attack k) (-> (assoc m k v)
                                       (assoc :original-attack v))
                     (= :health k) (-> (assoc m k v)
                                       (assoc :original-health v))
                     true m))
                 {}
                 definition))))

(defn core-secret->client-secret
  {:test (fn []
           (is (spec/valid? :firestone.spec/secret
                            (let [state (create-game [{:secrets ["Snake Trap"]}])
                                  secret (-> (get-secrets state)
                                             (first))]
                              (core-secret->client-secret state secret)))))}
  [state secret]
  (let [definition (get-definition secret)]
    {:name               (:name secret)
     :owner-id           (get-owner secret)
     :class              (name (:class definition))
     :id                 (:id secret)
     :entity-type        "secret"
     :rarity             (name (:rarity definition))
     :original-mana-cost (get-cost secret)
     :description        (:description definition)}))

(defn core-minion->client-minion
  {:test (fn []
           (is (spec/valid? :firestone.spec/minion
                            (let [state (create-game [{:minions ["Imp"]}])
                                  minion (-> (get-minions state)
                                             (first))]
                              (core-minion->client-minion state minion)))))}
  [state minion]
  (let [definition (get-definition minion)
        owner-id (get-owner minion)
        id (:id minion)]
    (merge
      {:attack           (get-attack minion)
       :can-attack       (contains? (valid-attacks state) id)
       :entity-type      "minion"
       :health           (get-health minion)
       :id               id
       :name             (:name minion)
       :mana-cost        (get-cost minion)
       :max-health       (+ (:health definition) (get-extra-health minion))
       :original-attack  (:attack definition)
       :original-health  (:health definition)
       :owner-id         owner-id
       :position         (:position minion)
       :set              (name (:set definition))
       :sleepy           (sleepy? state id)
       :states           (get-states minion)
       :valid-attack-ids (get (valid-attacks state) id)}

      ; Optional values from definition
      (reduce-kv (fn [m k v]
                   (cond
                     (= :description k) (assoc m k v)
                     (= :class k) (assoc m k (name v))
                     (= :rarity k) (assoc m k (name v))
                     true m))
                 {}
                 definition)
      )))

(defn core-player->client-player
  {:test (fn []
           (is (spec/valid? :firestone.spec/player
                            (let [state (create-game)
                                  player (-> (get-players state)
                                             (first))]
                              (core-player->client-player state player)))))}
  [state player]
  {:board-entities (map (fn [m]
                          (core-minion->client-minion state m))
                        (get-minions player))
   :active-secrets (map (fn [s]
                          (core-secret->client-secret state s))
                        (get-secrets player))
   :deck-size      (count (get-deck player))
   :hand           (map (fn [c]
                          (core-card->client-card state c))
                        (get-hand player))
   :hero           (core-hero->client-hero state (get-hero player))
   :id             (:id player)
   })

(defn core-game->client-game
  {:test (fn []
           (is (spec/valid? :firestone.spec/game-states
                             (-> (create-game [{:minions ["Imp" "Loot Hoarder" "Acolyte of Pain"]
                                                :hand    ["Snake Trap" "Imp"]
                                                :deck    ["Frostbolt"]}
                                               {:secrets ["Snake Trap"]}])
                                 (core-game->client-game "the game id")))))}
  [state id]
  [{:id             id
    :player-in-turn (get-player-id-in-turn state)
    :players        (map (fn [p]
                           (core-player->client-player state p))
                         (get-players state))}])