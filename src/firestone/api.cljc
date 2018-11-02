(ns firestone.api
  (:require [ysera.test :refer [is is-not is= error?]]
            [ysera.collections :refer [seq-contains?]]
            [firestone.definitions :refer [get-definition]]
            [firestone.construct :refer [create-card
                                        create-game
                                        create-hero
                                        create-minion
                                        get-heroes
                                        get-minion
                                        get-minions]]
            [firestone.core]))

; TODO: function "play-card"

; TODO: function "end-turn"
(comment
  "Taken from code base firestone.core"
  (defn end-turn
    [state player-id]
    (assoc state :player-id-in-turn "p2"))
  )
