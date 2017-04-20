(ns datomic-test.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [datomic-test.handler :refer :all]))

;;
;; curl -X POST -d "name=Ivan Ivanov" -d "email=ivan@test.com" -d "employer=Foo Bar" http://localhost:3000
;; curl -X GET http://localhost:3000
(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/test"))]
      (is (= (:status response) 200))
      (is (= (:body response) "TEST"))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
