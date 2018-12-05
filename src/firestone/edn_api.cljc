(ns firestone.edn-api
  (:require [firestone.construct :as construct :refer [create-game]]
            [firestone.mapper :refer [core-game->client-game]]
            [firestone.api :refer [attack-with-minion
                                   play-minion-card
                                   play-spell-card
                                   use-hero-power
                                   end-turn]]))

(defonce state-atom (atom nil))

(defn get-player-id-in-turn
  "This function is NOT pure!"
  []
  (-> (deref state-atom)
      (construct/get-player-id-in-turn)))

(defn create-game! [game-id]
  (core-game->client-game (reset! state-atom (create-game [{:hand ["Imp" "War Golem" "Sylvanas Windrunner"]
                                                            :deck ["Acolyte of Pain" "Snake Trap" "Snake Trap" "Big Game Hunter"]}
                                                           {:hand ["War Golem" "Frostbolt" "Eater of Secrets"]
                                                            :deck ["Sneed's Old Shredder" "Frostbolt" "Dalaran Mage"]}])) game-id))

(defn attack! [game-id player-id attacker-id target-id]
  (core-game->client-game (swap! state-atom attack-with-minion attacker-id target-id) game-id))

(defn end-turn! [game-id player-id]
  (core-game->client-game (swap! state-atom end-turn) game-id))

(defn play-minion-card! [game-id player-id card-id position target-id]
  (core-game->client-game (swap! state-atom play-minion-card player-id card-id {:position position :target-id target-id}) game-id))

(defn play-spell-card! [game-id player-id card-id target-id]
  (core-game->client-game (swap! state-atom play-spell-card player-id card-id {:target-id target-id}) game-id))

(defn use-hero-power! [game-id player-id target-id]
  (core-game->client-game (swap! state-atom use-hero-power player-id {:target-id target-id}) game-id))