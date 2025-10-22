(defproject pl.rynkowski.awscredentials/aws-java-sdk-v2 "0.1.0"
  :description "An adapter providing an AWS Java SDK v2 credentials."
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
  :url "https://github.com/rynkowsg/aws-credentials/tree/main/aws-java-sdk-v2"
  :scm {:url "https://github.com/rynkowsg/aws-credentials/tree/main/aws-java-sdk-v2"}

  :source-paths ["lib/main/src"]
  :dependencies [[com.cognitect.aws/api "0.8.774"]
                 [software.amazon.awssdk/auth "2.33.10"]]

  :profiles {:provided {:dependencies [[org.clojure/clojure]]}
             :dev {:source-paths []
                   :dependencies []}})
