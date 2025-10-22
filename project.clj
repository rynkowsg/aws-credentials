(defproject pl.rynkowski.awscredentials/suite "0.1.1"
  ;; Required by Sonatype OSS’ (more: https://leiningen.org/deploy.html#deploying-to-maven-central)
  :description "Adapters for interoperability between AWS Java SDK v1, v2, and Cognitect AWS API credentials providers."
  :url "https://github.com/rynkowsg/aws-credentials"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"
            :distribution :repo}
  :scm {:name "git"
        :connection "scm:git:git://github.com/rynkowsg/aws-credentials.git"
        :developerConnection "scm:git:ssh://github.com/rynkowsg/aws-credentials.git"
        :url "https://github.com/rynkowsg/aws-credentials/tree/main"}
  :pom-addition '([:developers
                   [:developer
                    [:id "rynkowsg"]
                    [:name "Greg Rynkowski"]
                    [:email "greg@rynkowski.pl"]
                    [:url "https://rynkowski.pl"]
                    [:roles
                     [:role "developer"]
                     [:role "maintainer"]]]])

  :signing {:gpg-key "C316D49C3BAED11ADD144D68F210ADE0BD4D6626"} ;; 0xF210ADE0BD4D6626, 'releases' identity
  :repositories [;; https://central.sonatype.org/publish/publish-portal-maven/
                 ;; https://central.sonatype.com/repository/maven-snapshots/
                 ["clojars" {:url "https://repo.clojars.org"
                             :snapshots false
                             :releases true}]
                 ["sonatype-releases" {:url "https://central.sonatype.com"
                                       :snapshots false
                                       :releases true
                                       :creds :gpg}]
                 ["sonatype-snapshots" {:url "https://central.sonatype.com/repository/maven-snapshots/"
                                        :releases false
                                        :snapshots true
                                        :creds :gpg}]
                 ["local" {:url "file:///tmp/local-repo/"
                           :releases false
                           :snapshots true}]]
  :deploy-repositories [["releases" "sonatype-releases"]
                        ["snapshots" "sonatype-snapshots"]]
  :managed-dependencies [;; sorted
                         [org.clojure/clojure "1.12.3"]
                         [pl.rynkowski.awscredentials/aws-api-extras "0.1.1"]
                         [pl.rynkowski.awscredentials/aws-java-sdk-v1 "0.1.1"]
                         [pl.rynkowski.awscredentials/aws-java-sdk-v2 "0.1.1"]
                         [pl.rynkowski.awscredentials/faraday-extras "0.1.1"]]
  :plugins [;; sorted
            [lein-file-replace "0.1.0"] ;; https://github.com/jcrossley3/lein-file-replace/tags
            [lein-modules "0.3.11"] ;; https://github.com/jcrossley3/lein-modules/tags
            [lein-pprint "1.3.2"]]
  :modules {:subprocess nil}

  :aliases {"test" ["modules" "do" "test," "install"]
            "clean" ["do" ["modules" "clean"]]
            "pom" ["do" ["modules" "pom"]]
            "jar" ["do" ["modules" "jar"]]
            "install" ["do" ["modules" "install"]]
            "deploy:local" ["do" ["modules" "deploy" "local"]]
            "deploy:clojars" ["do" ["modules" "deploy" "clojars"]]
            "deploy:sonatype-releases" ["do" ["modules" "deploy" "sonatype-releases"]]
            "deploy:sonatype-snapshots" ["do" ["modules" "deploy" "sonatype-snapshots"]]
            ;; these two rely on Leiningen's built-in 'release' feature, and overloading the :release-tasks with profiles
            "bump-snapshot" ["with-profile" "release/bump-snapshot" "release"]
            "mark-stable" ["with-profile" "release/mark-stable" "release"]
            "commit" ["with-profile" "release/commit" "release"]
            "commit:only" ["with-profile" "release/commit-only" "release"]}

  :profiles {:provided {:dependencies [[org.clojure/clojure]]}
             :test {:modules {:subprocess "lein"}}
             ;;
             :release/bump-snapshot {:release-tasks
                                     [["change" "version" "leiningen.release/bump-version"]
                                      ["change" ":managed-dependencies:pl.rynkowski.awscredentials/aws-api-extras" "leiningen.release/bump-version"]
                                      ["change" ":managed-dependencies:pl.rynkowski.awscredentials/aws-java-sdk-v1" "leiningen.release/bump-version"]
                                      ["change" ":managed-dependencies:pl.rynkowski.awscredentials/aws-java-sdk-v2" "leiningen.release/bump-version"]
                                      ["change" ":managed-dependencies:pl.rynkowski.awscredentials/faraday-extras" "leiningen.release/bump-version"]
                                      ["modules" "change" "version" "leiningen.release/bump-version"]
                                      ["file-replace" "README.md" " \"" "\"]" "version"] ;; update Leiningen ref
                                      ["file-replace" "README.md" "version \"" "\"" "version"] ;; update deps.tools ref
                                      ["pom"] ;; update POMs so deps.edn users can depend via git URL
                                      #_:release-tasks-end]
                                     #_:profile-end}
             :release/mark-stable {:release-tasks
                                   [["change" "version" "leiningen.release/bump-version" "release"]
                                    ["change" ":managed-dependencies:pl.rynkowski.awscredentials/aws-api-extras" "leiningen.release/bump-version" "release"]
                                    ["change" ":managed-dependencies:pl.rynkowski.awscredentials/aws-java-sdk-v1" "leiningen.release/bump-version" "release"]
                                    ["change" ":managed-dependencies:pl.rynkowski.awscredentials/aws-java-sdk-v2" "leiningen.release/bump-version" "release"]
                                    ["change" ":managed-dependencies:pl.rynkowski.awscredentials/faraday-extras" "leiningen.release/bump-version" "release"]
                                    ["modules" "change" "version" "leiningen.release/bump-version" "release"] ;; update Leiningen ref
                                    ["file-replace" "README.md" " \"" "\"]" "version"] ;; update Leiningen ref
                                    ["file-replace" "README.md" "version \"" "\"" "version"] ;; update deps.tools ref
                                    ["pom"] ;; update POMs so deps.edn users can depend via git URL
                                    #_:release-tasks-end]
                                   #_:profile-end}
             :release/commit-only {;; Only commit. Do not create tag. Do not push.
                                   :release-tasks
                                   [["vcs" "commit" "chore(release): bump version to %s"]
                                    #_:release-tasks-end]
                                   #_:profile-end}
             :release/commit {:release-tasks
                              [["vcs" "commit" "chore(release): bump version to %s"]
                               ["vcs" "tag"]
                               ["vcs" "push"]
                               #_:release-tasks-end]
                              #_:profile-end}
             :release/deploy {:release-tasks [["vcs" "assert-committed"]
                                              ["jar"]
                                              ["deploy"]]}}
  #_:project)
