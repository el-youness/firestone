(ns firestone.definition.hero-tests
  (:require [clojure.test :refer [function?
                                  deftest]]
            [ysera.test :refer [is is-not is= error?]]
            [firestone.construct :refer :all]
            [firestone.core :refer :all]
            [firestone.api :refer :all]))


(deftest fireblast-test
         (is= (-> (create-game [{:minions [(create-minion "Imp" :id "i1")]}
                                {:minions [(create-minion "Imp" :id "i2")]}])
                  (use-hero-power "p1" {:target-id "i2"})
                  (get-minions "p2")
                  (count))
              0))

(deftest lesser-heal-test
  (is= (-> (create-game [{:hero (create-hero "Anduin Wrynn" :damage-taken 2)
                          :minions [(create-minion "Imp" :id "i1")]}])
           (use-hero-power "p1" {:target-id "h1"})
           (get-health "h1"))
       30))

(deftest reinforce-test
  (is= (-> (create-game [{:hero "Uther Lightbringer"}])
           (use-hero-power "p1" {})
           (get-minions "p1")
           (count))
       1))

(deftest steady-shot-test
  (is= (-> (create-game [{:hero "Rexxar"}])
           (use-hero-power "p1" {})
           (get-health "h2"))
       28))
