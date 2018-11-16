(ns firestone.all
  (:require [ysera.test :refer [deftest is]]
            [clojure.test :refer [successful? run-tests]]
            [firestone.definitions]
            [firestone.definitions-loader]
            [firestone.construct]
            [firestone.core]
            [firestone.api]
            [firestone.definition.card]
            [firestone.definition.hero]
            [firestone.definition.effect]
            [firestone.card_test]))

(deftest test-all
         "Bootstrapping with the required namespaces, finds all the firestone.* namespaces (except this one),
         requires them, and runs all their tests."
         (let [namespaces (->> (all-ns)
                               (map str)
                               (filter (fn [x] (re-matches #"firestone\..*" x)))
                               (remove (fn [x] (= "firestone.all" x)))
                               (map symbol))]
           (is (successful? (time (apply run-tests namespaces))))))

