;; Copyright (c) 2025 Greg Rynkowski
;; SPDX-License-Identifier: Apache-2.0

(ns pl.rynkowski.awscredentials.faraday-extras
  (:require
    [pl.rynkowski.awscredentials.aws-api-extras :as credse]
    [pl.rynkowski.awscredentials.java-sdk-v1 :as java-sdk-v1])
  (:import
    (cognitect.aws.credentials CredentialsProvider)))

(defn client
  ([]
   (client {}))
  ([{:keys [access-key secret-key session-token region]
     :or {region (or (System/getenv "AWS_REGION") (System/getenv "AWS_DEFAULT_REGION"))}}]
   (assert (seq region) "region can not be empty")
   (assert (seq access-key) "access-key can not be empty")
   (assert (seq secret-key) "secret-key can not be empty")
   {:endpoint (format "http://dynamodb.%s.amazonaws.com" region)
    :provider (java-sdk-v1/->credentials-provider {:access-key access-key
                                                   :secret-key secret-key
                                                   :session-token session-token})
    :region region}))

(defn client-from-creds-provider
  [{:keys [^CredentialsProvider creds-provider ^String region]
    :or {region (System/getenv "AWS_REGION")}}]
  (assert (seq region) "region can not be empty")
  {:endpoint (format "http://dynamodb.%s.amazonaws.com" region)
   :provider (java-sdk-v1/->credentials-provider creds-provider)
   :region region})

(defn client-from-role
  [{:keys [role-arn region]
    :or {region (System/getenv "AWS_REGION")}}]
  (assert (seq region) "region can not be empty")
  (assert (seq role-arn) "role-arn can not be empty")
  {:endpoint (format "http://dynamodb.%s.amazonaws.com" region)
   :provider (java-sdk-v1/->credentials-provider (credse/assumed-role-credentials-provider {:role-arn role-arn}))
   :region region})

(comment
  (require
    '[taoensso.faraday :as far])

  ;; Faraday client using IAM user credentials (access key + secret key)
  #_1 (def far-client {:secret-key (System/getenv "AWS_SECRET_ACCESS_KEY") :access-key (System/getenv "AWS_ACCESS_KEY_ID") :region (System/getenv "AWS_REGION")})
  #_2 (def far-client (client {:secret-key (System/getenv "AWS_SECRET_ACCESS_KEY") :access-key (System/getenv "AWS_ACCESS_KEY_ID")}))
  ;; Faraday client using aws-api’s CredentialsProvider
  #_3 (def far-client (client-from-creds-provider {:creds-provider (credse/environment-credentials-provider)}))
  ;; Faraday client assuming an IAM role
  #_4 (def far-client (client-from-role {:role-arn (format "arn:aws:iam::%s:role/%s" "<account-id>" "<role-name>")}))

  (far/list-tables far-client)

  ;; All four methods work with IAM user credentials.
  ;; The third and fourth also work when running under an assumed role.
  ;; The fourth requires the current identity (user or role) to have permission to assume the target role.

  :comment)
