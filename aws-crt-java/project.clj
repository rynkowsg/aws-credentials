(defproject pl.rynkowski.awscredentials/aws-crt-java "0.1.3-SNAPSHOT"
  :description "An adapter providing an AWS CRT for Java credentials."
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
                 [software.amazon.awssdk.crt/aws-crt "0.39.4"]]

  :profiles {:provided {:dependencies [[org.clojure/clojure]]}
             :dev {:source-paths []
                   :dependencies []}})
