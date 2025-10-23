;; Copyright (c) Cognitect, Inc.
;; Copyright (c) 2025 Greg Rynkowski
;; SPDX-License-Identifier: Apache-2.0

(ns pl.rynkowski.awscredentials.aws-api-extras
  (:require
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [cognitect.aws.client.api :as aws]
    [cognitect.aws.config :as config]
    [cognitect.aws.credentials :as creds :refer [CredentialsProvider
                                                 Stoppable
                                                 -stop
                                                 cached-credentials-with-auto-refresh
                                                 calculate-ttl
                                                 valid-credentials]]
    [cognitect.aws.ec2-metadata-utils :as ec2]
    [cognitect.aws.util :as u])
  (:import (java.io File)
           (java.time Duration Instant ZoneOffset ZonedDateTime)
           (java.time.format DateTimeFormatter)
           (java.util Date)))

(def fetch
  "Public alias to cognitect.aws.credentials/fetch (protocol fn)."
  creds/fetch)

;; time helpers

(defn- ->instant
  "Takes various types representing a time value and returns an Instant
  object of the same instant in time. The supported conversions are:
  java.util.Date, CharSequence, Number, and Instant objects, which are
  returned outright.

  It is an exact copy of `cognitect.aws.credentials/->instant`."
  [t]
  (cond
    (instance? Date t) (.toInstant ^Date t)
    (instance? CharSequence t) (Instant/parse t)
    (number? t) (Instant/ofEpochMilli (.longValue ^Number t))
    :else t))

(defn format-duration
  [^Instant now ^Instant expiration]
  (let [duration (Duration/between now expiration)
        total-seconds (.getSeconds duration)
        hours (quot total-seconds 3600)
        minutes (quot (rem total-seconds 3600) 60)
        seconds (rem total-seconds 60)]
    (format "%dh %dm %ds" hours minutes seconds)))
#_(let [start (t/instant "2025-09-15T10:00:00Z")
        end (t/instant "2025-09-15T12:34:56Z")]
    (println (format-duration start end)))

(defn log-expiration
  [creds]
  (try
    (let [now (Instant/now)
          expiration (#'->instant (:Expiration creds))
          expiration-str (-> expiration
                             (ZonedDateTime/ofInstant ZoneOffset/UTC)
                             (.format DateTimeFormatter/ISO_ZONED_DATE_TIME))]
      (log/debugf "Credentials expire in %s (at %s)."
                  (format-duration now expiration)
                  expiration-str))
    (catch Throwable t
      (log/error t "Error logging credentials expiry."))))
#_(log-expiration {:Expiration (-> (Instant/now) (.plus (Duration/ofHours 1)))})

;;
;; Revisited aws-api's credentials providers
;;
;; Basically all credentials providers were stipped off
;; the cached-credentials-with-auto-refresh used in each of them.
;;

(defn chain-credentials-provider
  "Exact copy of `cognitect.aws.credentials/chain-credentials-provider`/

  This variant of chain-credentials-provider contains a mandatory cache of provider.
  It can't be disabled."
  [providers]
  (let [cached-provider (atom nil)]
    (reify
      CredentialsProvider
      (fetch [_]
        (valid-credentials
          (if @cached-provider
            (fetch @cached-provider)
            (some (fn [provider]
                    (when-let [creds (fetch provider)]
                      (reset! cached-provider provider)
                      creds))
                  providers))))
      Stoppable
      (-stop [_] (run! -stop providers)))))

(defn environment-credentials-provider
  "Copy of `cognitect.aws.credentials/environment-credentials-provider` without caching credentials."
  []
  (reify CredentialsProvider
    (fetch [_]
      (valid-credentials
        {:aws/access-key-id (u/getenv "AWS_ACCESS_KEY_ID")
         :aws/secret-access-key (u/getenv "AWS_SECRET_ACCESS_KEY")
         :aws/session-token (u/getenv "AWS_SESSION_TOKEN")}
        "environment variables"))))

(defn system-property-credentials-provider
  "Copy of `cognitect.aws.credentials/system-property-credentials-provider` without caching credentials & refresh."
  []
  (reify CredentialsProvider
    (fetch [_]
      (valid-credentials
        {:aws/access-key-id (u/getProperty "aws.accessKeyId")
         :aws/secret-access-key (u/getProperty "aws.secretKey")
         :aws/session-token (u/getProperty "aws.sessionToken")}
        "system properties"))))

(defn profile-credentials-provider
  "Copy of `cognitect.aws.credentials/profile-credentials-provider` without caching credentials."
  ([]
   (profile-credentials-provider (or (u/getenv "AWS_PROFILE")
                                     (u/getProperty "aws.profile")
                                     "default")))
  ([profile-name]
   (profile-credentials-provider profile-name (or (some-> (u/getenv "AWS_SHARED_CREDENTIALS_FILE") io/file) ;; aws-cli and java sdk v2
                                                  (some-> (u/getenv "AWS_CREDENTIAL_PROFILES_FILE") io/file) ;; java sdk v1
                                                  (io/file (u/getProperty "user.home") ".aws" "credentials"))))
  ([profile-name ^File f]
   (reify CredentialsProvider
     (fetch [_]
       (when (.exists f)
         (try
           (let [profile (get (config/parse f) profile-name)]
             (valid-credentials
               {:aws/access-key-id (get profile "aws_access_key_id")
                :aws/secret-access-key (get profile "aws_secret_access_key")
                :aws/session-token (get profile "aws_session_token")}
               "aws profiles file"))
           (catch Throwable t
             (log/error t "Error fetching credentials from aws profiles file"))))))))

(defn container-credentials-provider
  "Copy of `cognitect.aws.credentials/container-credentials-provider` without caching credentials."
  [http-client]
  (reify CredentialsProvider
    (fetch [_]
      (when-let [creds (ec2/container-credentials http-client)]
        (let [valid-creds (valid-credentials
                            {:aws/access-key-id (:AccessKeyId creds)
                             :aws/secret-access-key (:SecretAccessKey creds)
                             :aws/session-token (:Token creds)
                             :cognitect.aws.credentials/ttl (calculate-ttl creds)}
                            "ecs container")]
          (when valid-creds (log-expiration creds))
          valid-creds)))))

(defn ^:deprecated instance-profile-credentials-provider
  "Copy of `cognitect.aws.credentials/instance-profile-credentials-provider` without caching credentials."
  [http-client]
  (reify CredentialsProvider
    (fetch [_]
      (when-let [creds (ec2/instance-credentials http-client)]
        (let [valid-creds (valid-credentials
                            {:aws/access-key-id (:AccessKeyId creds)
                             :aws/secret-access-key (:SecretAccessKey creds)
                             :aws/session-token (:Token creds)
                             :cognitect.aws.credentials/ttl (calculate-ttl creds)}
                            "ec2 instance")]
          (when valid-creds (log-expiration creds))
          valid-creds)))))

(defn instance-profile-IMDSv2-credentials-provider
  "Copy of `cognitect.aws.credentials/instance-profile-IMDSv2-credentials-provider` without caching credentials."
  [http-client]
  (reify CredentialsProvider
    (fetch [_]
      (when-let [IMDSv2-token (ec2/IMDSv2-token http-client)]
        (when-let [creds (ec2/instance-credentials http-client IMDSv2-token)]
          (let [valid-creds (valid-credentials
                              {:aws/access-key-id (:AccessKeyId creds)
                               :aws/secret-access-key (:SecretAccessKey creds)
                               :aws/session-token (:Token creds)
                               :cognitect.aws.credentials/ttl (calculate-ttl creds)}
                              "IMDSv2 ec2 instance")]
            (when valid-creds (log-expiration creds))
            valid-creds))))))

;;
;; custom credentials provider
;;

(defn chain-credentials-provider-v2
  "Variant of chain-credentials-provider that allows to disable provider caching."
  [{:keys [providers cache-provider?] :or {cache-provider? true}}]
  (let [cached-provider (atom nil)]
    (reify
      CredentialsProvider
      (fetch [_]
        (valid-credentials
          (if (and cache-provider? @cached-provider)
            (fetch @cached-provider)
            (some (fn [provider]
                    (when-let [creds (fetch provider)]
                      (reset! cached-provider provider)
                      creds))
                  providers))))
      Stoppable
      (-stop [_] (run! -stop providers)))))

;; Helper: build the “default-v2” chain from a config map (no side effects)
(defn- build-default-provider*
  [{:keys [cache-provider? cache-credentials? providers]}]
  (let [creds-cache-fn (if cache-credentials? cached-credentials-with-auto-refresh identity)]
    (chain-credentials-provider-v2 {:providers (->> providers (map creds-cache-fn))
                                    :cache-provider? cache-provider?})))

(defn default-credentials-provider-v2
  "Variant of `cognitect.aws.credentials/default-credentials-provider` allowing to toggle caching.
  Both credentials cache and providers cache can be enabled or disabled."
  [{:keys [http-client cache-provider? cache-credentials?]}]
  (build-default-provider* {:http-client http-client
                            :cache-provider? cache-provider?
                            :cache-credentials? cache-credentials?}))

(defprotocol ReconfigurableCredentials
  (-reconfigure! [this cfg] "Refresh provider using given cfg."))

(defprotocol RefreshableCredentials
  (-refresh! [this] "Refresh provider using previously set config."))

(defn reconfigurable-credentials-provider
  [{:keys [http-client cache-provider? cache-credentials? providers] :as _cfg
    :or {cache-provider? true cache-credentials? true}}]
  (let [cfg' {:http-client (or http-client (aws/default-http-client))
              :cache-provider? cache-provider?
              :cache-credentials? cache-credentials?
              :providers (or providers [(environment-credentials-provider)
                                        (system-property-credentials-provider)
                                        (profile-credentials-provider)
                                        (container-credentials-provider http-client)
                                        (instance-profile-IMDSv2-credentials-provider http-client)
                                        #_{:clj-kondo/ignore [:deprecated-var]}
                                        (instance-profile-credentials-provider http-client)])}
        state* (atom {:cfg cfg'
                      :prov (build-default-provider* cfg')})]
    (reify
      CredentialsProvider
      (fetch [_]
        (when-let [p (:prov @state*)]
          (fetch p)))

      RefreshableCredentials
      (-refresh! [_]
        (let [[old _] (swap-vals! state* (fn [s] (let [cfg (:cfg s)
                                                       np (build-default-provider* cfg)]
                                                   {:cfg cfg :prov np})))]
          (when-let [op (:prov old)]
            (try
              (-stop ^Stoppable op)
              (catch Throwable t
                (log/warn t "Error stopping previous credentials provider"))))
          :ok))

      ReconfigurableCredentials
      (-reconfigure! [_ new-cfg]
        (if (= new-cfg (:cfg @state*))
          :no-op
          (let [[old _] (swap-vals! state* (fn [old] (let [cfg' (merge (:cfg old) new-cfg)
                                                           np (build-default-provider* cfg')]
                                                       {:cfg cfg' :prov np})))]
            (when-let [op (:prov old)]
              (try
                (-stop ^Stoppable op)
                (catch Throwable t
                  (log/warn t "Error stopping previous credentials provider"))))
            :ok)))

      Stoppable
      (-stop [_]
        (let [[old _] (swap-vals! state* (fn [s] (assoc s :prov nil)))]
          (when-let [op (:prov old)]
            (try
              (-stop ^Stoppable op)
              (catch Throwable t
                (log/warn t "Error during provider stop"))))))
      #_:end)))

(comment
  (def provider (reconfigurable-credentials-provider {:http-client (aws/default-http-client)
                                                      :cache-credentials? false
                                                      :cache-provider? false}))
  (aws/invoke (aws/client {:api :ssm :credentials-provider provider}) {:op :DescribeParameters})
  (aws/invoke (aws/client {:api :sts :credentials-provider provider}) {:op :GetCallerIdentity}))

(defn assumed-role-credentials-provider
  "Returns a CredentialsProvider that calls STS:AssumeRole using `source-provider`.
   Wrapped with cached auto-refresh so creds renew before expiration."
  [{:keys [source-provider role-arn http-client session-name]
    :or {session-name (str (System/currentTimeMillis))
         http-client (aws/default-http-client)}}]
  (let [sts-request (cond-> {:api :sts
                             :http-client http-client}
                      source-provider (assoc :credentials-provider source-provider))
        sts-client (aws/client sts-request)]
    (reify
      CredentialsProvider
      (fetch [_]
        (let [req {:op :AssumeRole
                   :request {:RoleArn role-arn
                             :RoleSessionName session-name}}
              resp (aws/invoke sts-client req)
              _ (when (:cognitect.anomalies/category resp)
                  (throw (ex-info (format "AWS %s failed %s"
                                          (name (:op req))
                                          (or (:cognitect.anomalies/message resp)
                                              (some-> resp :cognitect.anomalies/category name)
                                              "unknown error"))
                                  {:origin resp})))
              creds (:Credentials resp)
              valid-creds (valid-credentials
                            {:aws/access-key-id (:AccessKeyId creds)
                             :aws/secret-access-key (:SecretAccessKey creds)
                             :aws/session-token (:SessionToken creds)
                             :cognitect.aws.credentials/ttl (calculate-ttl creds)}
                            "assumed role")]
          (when valid-creds (log-expiration creds))
          valid-creds))
      Stoppable
      (-stop [_]
        (-stop sts-client)))))
