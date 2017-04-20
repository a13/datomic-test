(ns datomic-test.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer [response]]
            [datomic-test.db :as db]
            [taoensso.timbre :as log]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn- parse-name
  [n]
  (when n
    (let [[first-name second-name & junk] (clojure.string/split n #"\s")]
      (when-not (seq junk)
        {:first-name first-name
         :second-name second-name}))))

(defn- get-handler
  []
  (->> (db/get-records)
       (clojure.string/join "\n")
       response))

(defn- post-handler
  [name email employer]
  (log/info name email employer)
  (db/add-record
   (parse-name employer)
   (assoc (parse-name name) :email email)))

(defroutes app-routes
  (POST "/" {{name :name email :email employer :employer} :params}
        (post-handler name email employer))
  (GET "/" [] (get-handler))
  (GET "/test" [] "TEST")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes (assoc-in site-defaults [:security :anti-forgery] false)))

(def init db/init-db)

(def destroy db/destroy-db)
