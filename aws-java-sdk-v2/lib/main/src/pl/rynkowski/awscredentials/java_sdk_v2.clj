;; Copyright (c) 2025 Greg Rynkowski
;; SPDX-License-Identifier: Apache-2.0

(ns pl.rynkowski.awscredentials.java-sdk-v2
  (:require
    [cognitect.aws.credentials])
  (:import
    (clojure.lang IPersistentMap)
    (software.amazon.awssdk.auth.credentials
      AwsBasicCredentials
      AwsCredentialsProvider
      AwsSessionCredentials
      StaticCredentialsProvider)))

(defprotocol AwsSdkV2CredentialsProviderSource
  (^AwsCredentialsProvider ->credentials-provider [_]))

(extend-protocol AwsSdkV2CredentialsProviderSource

  ;; Creates a static Credentials Provider. The credentials never change.
  IPersistentMap
  (->credentials-provider [{:keys [access-key secret-key session-token]}]
    (assert (seq access-key) "access-key can not be empty")
    (assert (seq secret-key) "secret-key can not be empty")
    (if (seq session-token)
      (reify AwsCredentialsProvider
        (resolveCredentials [_]
          (AwsSessionCredentials/create access-key secret-key session-token)))
      (StaticCredentialsProvider/create
        (AwsBasicCredentials/create access-key secret-key))))

  ;; Creates a credentials provider delegating work to aws-api's credentials provider.
  ;; Preserves the source provider's nature: a static provider produces a static provider,
  ;; a dynamic provider produces a dynamic one.
  cognitect.aws.credentials.CredentialsProvider
  (->credentials-provider [^cognitect.aws.credentials.CredentialsProvider creds-provider]
    (let [session-creds-provider? (some? (:aws/session-token (.fetch creds-provider)))]
      (if session-creds-provider?
        (reify AwsCredentialsProvider
          (resolveCredentials [_]
            (let [{:aws/keys [access-key-id secret-access-key session-token expiration]} (.fetch creds-provider)
                  builder (cond-> (-> (AwsSessionCredentials/builder)
                                      (.accessKeyId access-key-id)
                                      (.secretAccessKey secret-access-key)
                                      (.sessionToken session-token))
                            expiration (.expirationTime expiration))]
              (.build builder))))
        (reify AwsCredentialsProvider
          (resolveCredentials [_]
            (let [{:aws/keys [access-key-id secret-access-key]} (.fetch creds-provider)]
              (AwsBasicCredentials/create access-key-id secret-access-key))))))))
