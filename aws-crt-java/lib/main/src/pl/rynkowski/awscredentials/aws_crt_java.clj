;; Copyright (c) 2025 Greg Rynkowski
;; SPDX-License-Identifier: Apache-2.0

(ns pl.rynkowski.awscredentials.aws-crt-java
  (:require
    [cognitect.aws.credentials])
  (:import
    (clojure.lang IPersistentMap)
    (java.nio.charset StandardCharsets)
    (software.amazon.awssdk.crt.auth.credentials
      Credentials
      CredentialsProvider
      DelegateCredentialsHandler
      DelegateCredentialsProvider$DelegateCredentialsProviderBuilder
      StaticCredentialsProvider$StaticCredentialsProviderBuilder)))

(defprotocol AwsCrtCredentialsProviderSource
  (^CredentialsProvider ->credentials-provider [_]))

(extend-protocol AwsCrtCredentialsProviderSource
  ;; Creates a static Credentials Provider. The credentials never change.
  IPersistentMap
  (->credentials-provider [{:keys [access-key secret-key session-token]}]
    (assert (seq access-key) "access-key can not be empty")
    (assert (seq secret-key) "secret-key can not be empty")
    (let [builder (cond-> (-> (StaticCredentialsProvider$StaticCredentialsProviderBuilder.)
                              (.withAccessKeyId (.getBytes ^String access-key StandardCharsets/UTF_8))
                              (.withSecretAccessKey (.getBytes ^String secret-key StandardCharsets/UTF_8)))
                    session-token (.withSessionToken (.getBytes ^String session-token StandardCharsets/UTF_8)))]
      (.build builder)))

  ;; Creates a credentials provider delegating work to aws-api's credentials provider.
  ;; Preserves the source provider's nature: a static provider produces a static provider,
  ;; a dynamic provider produces a dynamic one.
  cognitect.aws.credentials.CredentialsProvider
  (->credentials-provider [^cognitect.aws.credentials.CredentialsProvider creds-provider]
    (let [handler (reify DelegateCredentialsHandler
                    (getCredentials []
                      (let [creds (.fetch creds-provider)]
                        (Credentials.
                          (.getBytes ^String (:aws/access-key-id creds) StandardCharsets/UTF_8)
                          (.getBytes ^String (:aws/secret-access-key creds) StandardCharsets/UTF_8)
                          (.getBytes ^String (:aws/session-token creds) StandardCharsets/UTF_8)))))]
      (-> (DelegateCredentialsProvider$DelegateCredentialsProviderBuilder)
          (.withHandler handler)
          (.build)))))
