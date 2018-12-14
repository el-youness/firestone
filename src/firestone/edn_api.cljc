(ns firestone.edn-api
  (:require [firestone.construct :as construct :refer [create-game]]
            [firestone.mapper :refer [core-game->client-game]]
            [firestone.definition.card :refer [card-definitions]]
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
  (core-game->client-game (reset! state-atom (let [cards (keys card-definitions)]
                                               (create-game [{:deck (drop 5 cards)
                                                              ;:hand (take 5 cards)}
                                                              :hand ["Booty Bay Bodyguard"
                                                                     "War Golem"
                                                                     "The Black Knight"]}
                                                             {:deck (drop 5 (reverse cards))
                                                              ; :hand (take 5 (reverse cards))
                                                              :hand ["Booty Bay Bodyguard"
                                                                     "War Golem"
                                                                     "The Black Knight"]
                                                              :hero "Rexxar"}])))
                          game-id))

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