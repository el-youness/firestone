(ns firestone.mapper
  (:require [ysera.test :refer [is is=]]
            [clojure.spec.alpha :as spec]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-game
                                         get-player-id-in-turn
                                         get-players
                                         get-minions
                                         get-minion-effects]]
            [firestone.core :refer [get-attack
                                    get-health
                                    get-cost
                                    get-owner
                                    sleepy?
                                    valid-]]))


(defn core-secret->client-secret
  [state secret]
  {}
  )

(defn core-minion->client-minion
  {:test (fn []
           (is (spec/valid? :firestone.spec/minion
                            (let [state (create-game [:minions ["Imp"]])
                                  minion (-> (get-minions state)
                                             (first))]
                              (core-minion->client-minion state minion)))))}
  [state minion]
  (let [definition (get-definition minion)]
    {:attack           (get-attack minion)
     :can-attack       (:cannot-attack (get-minion-effects minion))
     :entity-type      "minion"
     :health           (get-health minion)
     :id               (:id minion)
     :name             (:name minion)
     :mana-cost        (get-cost minion)
     :max-health       (+ (:health definition) (:extra-health (get-minion-effects minion)))
     :original-attack  (:attack definition)
     :original-health  (:health definition)
     :owner-id         (get-owner minion)
     :position         (:position minion)
     :set              (:set definition)
     :sleepy           (sleepy? state (:id minion))
     :states           (get-minion-states minion)           ; TODO: Implement get-minions states in core
     :valid-attack-ids (get-valid-attacks state (:id minion))})) ; TODO: Implement get-valid-attacks in core

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
   :active-secrets []
   })

(defn core-game->client-game [state]
  ; TODO: map according to spec
  [{:id             "the-game-id"
    :player-in-turn (get-player-id-in-turn state)
    :players        (map (fn [p]
                           (core-player->client-player state p))
                         (get-players state))}])
