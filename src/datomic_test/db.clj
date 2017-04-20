(ns datomic-test.db
  (require [datomic.api :as d]
           [taoensso.timbre :as log]))

;; Схема данных (для всех свойств кроме employer/employees, кардинальность один-к-одному.):
;; employer
;; first-name : string
;; second-name : string
;; employees = [employee ...] : ref

;; employee
;; first-name : string
;; second-name : string
;; email : string, unique

(def employer-schema
  [{:db/id (d/tempid :db.part/db)
    :db/ident :employer/first-name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Employer's first name"
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :employer/second-name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Employer's second name"
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :employer/employees
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc "Employer's employees"
    :db.install/_attribute :db.part/db}])

(def employee-schema
  [{:db/id (d/tempid :db.part/db)
    :db/ident :employee/first-name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Employee's first name"
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :employee/second-name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Employee's second name"
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :employee/email
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one
    :db/doc "Employee's e-mail"
    :db.install/_attribute :db.part/db}])

(def db-conn (atom nil))

(def uri "datomic:mem://datomic-test")

(defn init-db
  []
  (if (d/create-database uri)
    (log/info "DB created")
    (log/warn "DB already exists"))

  (swap! db-conn (constantly (d/connect uri)))
  (log/info "DB connection established")
  (d/transact @db-conn employer-schema)
  (d/transact @db-conn employee-schema)
  (log/info "Schema uploaded"))

(defn destroy-db
  []
  (d/delete-database uri)
  (swap! db-conn (constantly nil)))

(comment
  (init-db)
  (destroy-db)
  )

(defn- get-first-employer
  "First found employer."
  [{:keys [first-name second-name]}]
  (->
   (d/q '[:find [(pull ?e [:db/id])]
          :in $ [?first-name ?second-name]
          :where
          [?e :employer/first-name ?first-name]
          [?e :employer/second-name ?second-name]]
        (d/db @db-conn)
        [first-name second-name])
   first
   :db/id))

(defn- trans-gen
  "Генерируем запрос на создание/изменение employer"
  [employer employee]
  [{:db/id (or (get-first-employer employer)
               (d/tempid :db.part/user))
    :employer/first-name (:first-name employer)
    :employer/second-name (:second-name employer)
    :employer/employees [{:db/id (d/tempid :db.part/user)
                          :employee/first-name (:first-name employee)
                          :employee/second-name (:second-name employee)
                          :employee/email (:email employee)}]}])

(defn add-record
  [employer employee]
  (log/info "Add/Edit employee" employee "of employer" employer)
  (d/transact @db-conn (trans-gen employer employee)))

(def ^:private find-all-q
  '[:find
    (pull ?ees [:employee/first-name :employee/second-name :employee/email])
    (pull ?e [:employer/first-name :employer/second-name])
    :where
    [?e :employer/employees ?ees]])

(defn- employee-info
  [{:keys [employee/first-name employee/second-name employee/email]}]
  (str "name: " first-name " " second-name "\n"
       "email: " email))

(defn- employer-info
  [{:keys [employer/first-name employer/second-name]}]
  (str "employer: " first-name " " second-name "\n"))

(defn- convert-records
  [[employee employer]]
  (clojure.string/join
   "\n"
   [(employee-info employee)
    (employer-info employer)]))

(defn get-records
  []
  (let [rs (d/q find-all-q (d/db @db-conn))]
    (mapv convert-records rs)))

(comment
  (add-record {:first-name "123" :second-name "456"}
              {:first-name "qwe" :second-name "sad" :email "123456@test.com"})

  (get-records)
  )
