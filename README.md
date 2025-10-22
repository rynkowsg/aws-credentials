# aws-credentials

## Installation

Add the following dependency to your deps.edn file:

```clojure
pl.rynkowski.awscredentials/aws-java-sdk-v1 {:mvn/version "0.1.1"}
pl.rynkowski.awscredentials/aws-java-sdk-v2 {:mvn/version "0.1.1"}
pl.rynkowski.awscredentials/aws-api-extras {:mvn/version "0.1.1"}
pl.rynkowski.awscredentials/faraday-extras {:mvn/version "0.1.1"}
```

Or to your Leiningen project file:

```clojure
[pl.rynkowski.awscredentials/aws-java-sdk-v1 "0.1.1"]
[pl.rynkowski.awscredentials/aws-java-sdk-v2 "0.1.1"]
[pl.rynkowski.awscredentials/aws-api-extras "0.1.1"]
[pl.rynkowski.awscredentials/faraday-extras "0.1.1"]
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

## Copyright and License

Copyright © 2025 Greg Rynkowski

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
