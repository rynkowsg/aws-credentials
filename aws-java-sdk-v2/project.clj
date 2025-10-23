(defproject pl.rynkowski.awscredentials/aws-java-sdk-v2 "0.1.2-SNAPSHOT"
  :description "An adapter providing an AWS Java SDK v2 credentials."
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
  :dependencies [[com.cognitect.aws/api "0.8.774"]
                 [software.amazon.awssdk/auth "2.33.10"]]

  :profiles {:provided {:dependencies [[org.clojure/clojure]]}
             :dev {:source-paths []
                   :dependencies []}})
