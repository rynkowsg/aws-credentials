(defproject pl.rynkowski.awscredentials/aws-api-extras "0.1.3"
  :description "Extra utils for aws-api related to CredentialsProvider."
  :plugins [[lein-parent "0.3.9"] ;; https://github.com/achin/lein-parent/tags
            [lein-pprint "1.3.2"]]
  :parent-project {:path "../project.clj"
                   :inherit [:url
                             [:scm :name]
                             [:scm :connection]
                             [:scm :developerConnection]
                             [:scm :url]
                             [:scm :tag]
                             :license
                             :signing
                             :repositories
                             :deploy-repositories
                             :managed-dependencies]}

  :source-paths ["lib/main/src"]
  :dependencies [[com.amazonaws/aws-java-sdk-core "1.12.792"]
                 [com.cognitect.aws/api "0.8.774"]]

  :profiles {:provided {:dependencies [[org.clojure/clojure]]}
             :dev {:source-paths []
                   :dependencies []}})
