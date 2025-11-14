;; Copyright (c) 2025 Greg Rynkowski
;; SPDX-License-Identifier: Apache-2.0

(ns pl.rynkowski.awscredentials.java-sdk-v1
  (:require
    [cognitect.aws.credentials])
  (:import
    (clojure.lang IPersistentMap)
    (com.amazonaws.auth
      AWSCredentialsProvider
      AWSSessionCredentialsProvider
      AWSStaticCredentialsProvider
      BasicAWSCredentials
      BasicSessionCredentials)))

(set! *warn-on-reflection* true)

(defprotocol AwsSdkV1CredentialsProviderSource
  (^AWSCredentialsProvider ->credentials-provider [_]))

(extend-protocol AwsSdkV1CredentialsProviderSource
  IPersistentMap
  (->credentials-provider [{:keys [access-key secret-key session-token]}]
    (assert (seq access-key) "access-key can not be empty")
    (assert (seq secret-key) "secret-key can not be empty")
    (if (seq session-token)
      (reify AWSSessionCredentialsProvider
        (getCredentials [_]
          (BasicSessionCredentials. access-key secret-key session-token)))
      (AWSStaticCredentialsProvider. (BasicAWSCredentials. access-key secret-key))))

  cognitect.aws.credentials.CredentialsProvider
  (->credentials-provider [^cognitect.aws.credentials.CredentialsProvider creds-provider]
    (let [{:aws/keys [access-key-id
                      secret-access-key
                      session-token]} (.fetch creds-provider)]
      (->credentials-provider {:access-key access-key-id
                               :secret-key secret-access-key
                               :session-token session-token}))))
