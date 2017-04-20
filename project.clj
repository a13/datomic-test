(defproject datomic-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :gpg}}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.datomic/datomic-pro "0.9.5561"]]
  :plugins [[lein-ring "0.9.7"]
            [cider/cider-nrepl "0.15.0-SNAPSHOT"]]
  :ring {:handler datomic-test.handler/app
         :nrepl {:start? true}
         :init datomic-test.handler/init
         :destroy datomic-test.handler/destroy}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
