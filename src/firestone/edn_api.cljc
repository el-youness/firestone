(ns firestone.edn-api
  (:require [firestone.construct :as construct :refer [create-game]]
            [firestone.mapper :refer [core-game->client-game]]))

(defonce state-atom (atom nil))

(defn get-player-id-in-turn
  "This function is NOT pure!"
  []
  (-> (deref state-atom)
      (construct/get-player-id-in-turn)))

(defn create-game! []
  (core-game->client-game (reset! state-atom (create-game))))

(defn attack! [game-id player-id attacker-id target-id]
  (core-game->client-game (swap! state-atom identity)))

(defn end-turn! [game-id player-id]
  (core-game->client-game (swap! state-atom identity)))

(defn play-minion-card! [game-id player-id card-id position target-id]
  (core-game->client-game (swap! state-atom identity)))

(defn play-spell-card! [game-id player-id card-id target-id]
  (core-game->client-game (swap! state-atom identity)))

(defn use-hero-power! [game-id player-id target-id]
  (core-game->client-game (swap! state-atom identity)))