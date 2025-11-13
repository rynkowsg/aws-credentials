;; Copyright (c) 2025 Greg Rynkowski
;; SPDX-License-Identifier: Apache-2.0

(ns pl.rynkowski.awscredentials.aws-crt-java
  (:require
    [cognitect.aws.credentials])
  (:import
    (clojure.lang IPersistentMap)
    (java.nio.charset StandardCharsets)
    (software.amazon.awssdk.crt.auth.credentials
      CredentialsProvider
      StaticCredentialsProvider$StaticCredentialsProviderBuilder)))

(defprotocol ConvertibleToAwsCrtCredentialsProvider
  (^CredentialsProvider ->credentials-provider [_]))

(extend-protocol ConvertibleToAwsCrtCredentialsProvider
  IPersistentMap
  (->credentials-provider [{:keys [access-key secret-key session-token]}]
    (assert (seq access-key) "access-key can not be empty")
    (assert (seq secret-key) "secret-key can not be empty")
    (let [builder (cond-> (-> (StaticCredentialsProvider$StaticCredentialsProviderBuilder.)
                              (.withAccessKeyId (.getBytes ^String access-key StandardCharsets/UTF_8))
                              (.withSecretAccessKey (.getBytes ^String secret-key StandardCharsets/UTF_8)))
                    session-token (.withSessionToken (.getBytes ^String session-token StandardCharsets/UTF_8)))]
      (.build builder)))

  cognitect.aws.credentials.CredentialsProvider
  (->credentials-provider [^cognitect.aws.credentials.CredentialsProvider creds-provider]
    (let [{:aws/keys [access-key-id
                      secret-access-key
                      session-token]} (.fetch creds-provider)]
      (->credentials-provider {:access-key access-key-id
                               :secret-key secret-access-key
                               :session-token session-token}))))
