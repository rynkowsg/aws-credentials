# aws-credentials

## Installation

Add the following dependency to your deps.edn file:

```clojure
pl.rynkowski.awscredentials/aws-java-sdk-v1 {:mvn/version "0.1.0-SNAPSHOT"}
pl.rynkowski.awscredentials/aws-java-sdk-v2 {:mvn/version "0.1.0-SNAPSHOT"}
pl.rynkowski.awscredentials/aws-api-extras {:mvn/version "0.1.0-SNAPSHOT"}
pl.rynkowski.awscredentials/faraday-extras {:mvn/version "0.1.0-SNAPSHOT"}
```

Or to your Leiningen project file:

```clojure
[pl.rynkowski.awscredentials/aws-java-sdk-v1 "0.1.0-SNAPSHOT"]
[pl.rynkowski.awscredentials/aws-java-sdk-v2 "0.1.0-SNAPSHOT"]
[pl.rynkowski.awscredentials/aws-api-extras "0.1.0-SNAPSHOT"]
[pl.rynkowski.awscredentials/faraday-extras "0.1.0-SNAPSHOT"]
```

List of artifacts:

- `pl.rynkowski.awscredentials/aws-java-sdk-v1` - construction of credentials provider
  native to AWS Java SDK V1, from bare values or aws-api CredentialsProvider

- `pl.rynkowski.awscredentials/aws-java-sdk-v2` - construction of credentials provider
  native to AWS Java SDK V2, from bare values or aws-api CredentialsProvider

- `pl.rynkowski.awscredentials/aws-api-extras` - modifications and extension of
  credentials providers available in aws-api, including:
  - manually refreshable and resettable CredentialsProvider
  - CredentialsProvider from STS assumed role

- `pl.rynkowski.awscredentials/faraday-extras` - util functions to create client opts
  from aws-api's CredentialsProvider or even AWS role alone

## License

Copyright © 2025 Greg Rynkowski

Distributed under the Eclipse Public License version 2.0
