  (ns firestone.mapper
  (:require [ysera.test :refer [is is=]]
            [clojure.spec.alpha :as spec]
            [firestone.construct :refer [create-game
                                         get-player-id-in-turn
                                         get-players]]))


(defn core-secret->client-secret
  [state secret]
  {}
  )

(defn core-player->client-player
  {:test (fn []
           (is (spec/valid? :firestone.spec/player
                            (let [state (create-game)
                                  player (-> (get-players state)
                                             (first))]
                              (core-player->client-player state player)))))}
  [state player]
  {:board-entities []
   :active-secrets []
   })

(defn core-game->client-game [state]
  ; TODO: map according to spec
  [{:id "the-game-id"
    :player-in-turn (get-player-id-in-turn state)
    :players (map (fn [p]
                    (core-player->client-player state p))
                  (get-players state))}])
