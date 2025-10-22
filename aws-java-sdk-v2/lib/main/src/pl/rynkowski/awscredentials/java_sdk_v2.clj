;; Copyright (c) 2025 Greg Rynkowski
;; SPDX-License-Identifier: Apache-2.0

(ns pl.rynkowski.awscredentials.java-sdk-v2
  (:require
    [cognitect.aws.credentials :refer [CredentialsProvider fetch]])
  (:import
    (clojure.lang IPersistentMap)
    (software.amazon.awssdk.auth.credentials AwsCredentialsProvider)
    (software.amazon.awssdk.auth.credentials AwsBasicCredentials AwsCredentialsProvider AwsSessionCredentials
                                             StaticCredentialsProvider)))

(defmulti ->credentials-provider class)

(defmethod ->credentials-provider IPersistentMap
  [{:keys [access-key secret-key session-token]}]
  (assert (seq access-key) "access-key can not be empty")
  (assert (seq secret-key) "secret-key can not be empty")
  (if (seq session-token)
    (reify AwsCredentialsProvider
      (resolveCredentials [_]
        (AwsSessionCredentials/create access-key secret-key session-token)))
    (StaticCredentialsProvider/create
      (AwsBasicCredentials/create access-key secret-key))))

(defmethod ^AwsCredentialsProvider ->credentials-provider CredentialsProvider
  [^CredentialsProvider creds-provider]
  (let [{:aws/keys [access-key-id
                    secret-access-key
                    session-token]} (fetch creds-provider)]
    (->credentials-provider {:access-key access-key-id
                             :secret-key secret-access-key
                             :session-token session-token})))
