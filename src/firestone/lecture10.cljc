(ns firestone.lecture10)

(comment

  ; Be able to create the state of the game
  ; ---------------------------------------

  (deftest create-game-test
           (let [state (create-game [{:board ["Imp" "Defender"]
                                      :hand  ["Dalaran Mage" "Ogre Magi"]
                                      :deck  ["War Golem"]
                                      :hero  "Rexxar"}
                                     {:board ["Dalaran Mage" "Ogre Magi"]
                                      :hand  ["Imp" "Defender"]
                                      :deck  ["War Golem" "War Golem"]
                                      :hero  "Jaina Proudmoore"}])]
             ;; Board
             (is= (get-board-names state)
                  [["Imp" "Defender"] ["Dalaran Mage" "Ogre Magi"]])

             ;; Hand
             (is= (->> (get-hand state "p1")
                       (map :name))
                  ["Dalaran Mage" "Ogre Magi"])
             ;; Deck
             (is= (->> (get-deck state "p2")
                       (map :name))
                  ["War Golem" "War Golem"])
             ;; Heroes
             (is= (:name (get-hero-by-player-id state "p1"))
                  "Rexxar")))



  ; Be able to play cards
  ; ---------------------

  (deftest play-minion-card-test
           (let [state (-> (create-game [{:hand [(create-card "Imp" :id "i")]}])
                           (play-minion-card "p1" "i" 0))]
             (is= (count (get-hand state "i"))
                  0)
             (is= (get-board-names state)
                  [["Imp"]])))

  (deftest play-spell-card-test
           (let [state (-> (create-game [{:hand [(create-card "Frostbolt" :id "f")]}])
                           (play-spell-card "p1" "f" "h2"))]
             (is= (count (get-hand state "i"))
                  0)
             (is= (get-board-names state)
                  [])
             (is= (get-health state (get-hero-by-player-id state "p2"))
                  27)))



  ; Attacking, take-control, sleepiness, end-turn
  ; ---------------------------------------------

  (deftest minion-attack-minion-test
           (is= (-> (create-game [{:board [(create-minion "Imp" :id "i")]}
                                  {:board [(create-minion "War Golem" :id "wg")]}])
                    (attack "p1" "i" "wg")
                    (get-board-names))
                [[] ["War Golem"]]))

  (deftest minions-without-charge-should-not-be-able-to-attack-when-played-test
           (let [state (-> (create-game [{:hand [(create-card "Imp" :id "i")]}])
                           (play-minion-card "p1" "i" 0))
                 imp (first (get-minions state "p1"))]
             (is-not (can-attack? state imp))
             (error? (attack state "p1" (:id imp) "h2"))))

  (deftest minions-should-be-able-to-attack-at-the-start-of-turn-test
           (let [state (as-> (create-game [{:hand [(create-card "Imp" :id "c-i")]}
                                           {:board [(create-minion "War Golem" :id "wg")]}]) $
                             (play-minion-card $ "p1" "c-i" 0)
                             (end-turn $ "p1")
                             (end-turn $ "p2")
                             (attack $ "p1" (:id (first (get-minions $ "p1"))) "wg"))]
             (is= (get-board-names state) [[] ["War Golem"]])))

  (deftest Take-Control-and-Sleepy-test
           ; When taking control of a minion, it should be sleepy.
           (let [state (-> (create-game [{:hand [(create-card "Mind Control" :id "mc")]}
                                         {:board [(create-minion "War Golem" :id "wg")]}])
                           (play-spell-card "p1" "mc" "wg"))]
             (is (sleepy? state (get-minion state "wg")))))



  ; Hand/Deck mechanics such as drawing cards and fatigue.
  ; ------------------------------------------------------

  (deftest can-only-have-10-cards-in-hand-test
           (let [state (-> (create-game [{:hand (repeat 10 "Imp")
                                          :deck (repeat 2 "Imp")}])
                           (end-turn "p1")
                           (end-turn "p2"))]
             (is= (count (get-hand state "p1")) 10)
             (is= (count (get-deck state "p1")) 1)
             (is= (get-health state (get-hero-by-player-id state "p1")) 30)))

  (deftest can-at-most-have-7-minions-on-the-board-test
           (error? (-> (create-game [{:board (repeat 7 "Imp")
                                      :hand [(create-card "Imp" :id "i")]}])
                       (play-minion-card "p1" "i" 0))))

  (deftest fatigue-test
           (as-> (create-game) $
                 (end-turn $ "p1")
                 (end-turn $ "p2")
                 (do (is= (get-health $ (get-hero-by-player-id $ "p1"))
                          29)
                     $)
                 (end-turn $ "p1")
                 (end-turn $ "p2")
                 (is= (get-health $ (get-hero-by-player-id $ "p1"))
                      27)))



  ; Frozen
  ; ------

  (deftest Frozen-minions-can-not-attack-test
           (as-> (create-game [{:board [(create-minion "War Golem" :id "wg")]
                                :hand [(create-card "Frostbolt" :id "f")]}]) $
                 (do (is (can-attack? $ (get-minion $ "wg")))
                     $)
                 (play-spell-card $ "p1" "f" "wg")
                 (do (is-not (can-attack? $ (get-minion $ "wg")))
                     $)
                 (end-turn $ "p1")
                 (end-turn $ "p2")
                 (is (can-attack? $ (get-minion $ "wg")))))




  )
