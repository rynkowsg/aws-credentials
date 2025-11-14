;; Copyright (c) 2025 Greg Rynkowski
;; SPDX-License-Identifier: Apache-2.0

(ns pl.rynkowski.awscredentials.java-sdk-v1
  (:require
    [cognitect.aws.credentials])
  (:import
    (clojure.lang IPersistentMap)
    (com.amazonaws.auth
      AWSCredentials
      AWSCredentialsProvider
      AWSSessionCredentialsProvider
      AWSStaticCredentialsProvider
      BasicAWSCredentials
      BasicSessionCredentials)))

(defprotocol AwsSdkV1CredentialsProviderSource
  (^AWSCredentialsProvider ->credentials-provider [_]))

(extend-protocol AwsSdkV1CredentialsProviderSource

  ;; Creates a static Credentials Provider. The credentials never change.
  IPersistentMap
  (->credentials-provider [{:keys [access-key secret-key session-token]}]
    (assert (seq access-key) "access-key can not be empty")
    (assert (seq secret-key) "secret-key can not be empty")
    (if (seq session-token)
      (reify AWSSessionCredentialsProvider
        (getCredentials [_]
          (BasicSessionCredentials. access-key secret-key session-token))
        (refresh [_]))
      (AWSStaticCredentialsProvider. (BasicAWSCredentials. access-key secret-key))))

  ;; Creates a credentials provider delegating work to aws-api's credentials provider.
  ;; Preserves the source provider's nature: a static provider produces a static provider,
  ;; a dynamic provider produces a dynamic one.
  cognitect.aws.credentials.CredentialsProvider
  (->credentials-provider [^cognitect.aws.credentials.CredentialsProvider creds-provider]
    (let [session-creds-provider? (some? (:aws/session-token (.fetch creds-provider)))]
      (if session-creds-provider?
        (reify AWSSessionCredentialsProvider
          (getCredentials [_]
            (let [creds (.fetch creds-provider)]
              (reify AWSSessionCredentialsProvider
                (getAWSAccessKeyId [_] (:aws/access-key-id creds))
                (getAWSSecretKey [_] (:aws/secret-access-key creds))
                (getSessionToken [_] (:aws/session-token creds)))))
          (refresh [_]))
        (reify AWSCredentialsProvider
          (getCredentials [_]
            (let [creds (.fetch creds-provider)]
              (reify AWSCredentials
                (getAWSAccessKeyId [_] (:aws/access-key-id creds))
                (getAWSSecretKey [_] (:aws/secret-access-key creds)))))
          (refresh [_]))))))
