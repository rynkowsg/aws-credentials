;; Copyright (c) 2025 Greg Rynkowski
;; SPDX-License-Identifier: Apache-2.0

(ns pl.rynkowski.awscredentials.java-sdk-v1
  (:require
    [cognitect.aws.credentials :refer [CredentialsProvider fetch]])
  (:import
    (clojure.lang IPersistentMap)
    (com.amazonaws.auth AWSCredentialsProvider AWSSessionCredentialsProvider AWSStaticCredentialsProvider
                        BasicAWSCredentials BasicSessionCredentials)))

(set! *warn-on-reflection* true)

(defmulti ^AWSCredentialsProvider ->credentials-provider class)

(defmethod ^AWSCredentialsProvider ->credentials-provider IPersistentMap
  [{:keys [access-key secret-key session-token]}]
  (assert (seq access-key) "access-key can not be empty")
  (assert (seq secret-key) "secret-key can not be empty")
  (if (seq session-token)
    (reify AWSSessionCredentialsProvider
      (getCredentials [_]
        (BasicSessionCredentials. access-key secret-key session-token)))
    (AWSStaticCredentialsProvider. (BasicAWSCredentials. access-key secret-key))))

(defmethod ^AWSCredentialsProvider ->credentials-provider CredentialsProvider
  [^CredentialsProvider creds-provider]
  (let [{:aws/keys [access-key-id
                    secret-access-key
                    session-token]} (fetch creds-provider)]
    (->credentials-provider {:access-key access-key-id
                             :secret-key secret-access-key
                             :session-token session-token})))
