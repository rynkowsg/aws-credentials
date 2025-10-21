(defproject pl.rynkowski.awscredentials/faraday-extras "0.1.0-SNAPSHOT"
  :description "Extra utils for aws-api related to CredentialsProvider."
  :plugins [[lein-parent "0.3.9"] ;; https://github.com/achin/lein-parent/tags
            [lein-pprint "1.3.2"]]
  :parent-project {:path "../project.clj"
                   :inherit [[:scm :name]
                             [:scm :connection]
                             [:scm :developerConnection]
                             :license
                             :signing
                             :repositories
                             :deploy-repositories
                             :managed-dependencies]}
  :url "https://github.com/rynkowsg/aws-credentials/tree/main/faraday-extras"
  :scm {:url "https://github.com/rynkowsg/aws-credentials/tree/main/faraday-extras"}

  :source-paths ["lib/main/src"]
  :dependencies [[com.taoensso/faraday "1.12.3"]
                 [pl.rynkowski.awscredentials/aws-api-extras]]

  :profiles {:provided {:dependencies [[org.clojure/clojure]]}
             :dev {:source-paths []
                   :dependencies []}})
