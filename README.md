# DataCater

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=DataCater_datacater&metric=alert_status&token=64e2de6e7a588f2317c38b1536395e6a4d565108)](https://sonarcloud.io/summary/new_code?id=DataCater_datacater)

[<p align="center"><img src="logo.png" alt="DataCater logo" width="350"/></p>](https://datacater.io)

Welcome to the real-time, cloud-native data pipeline platform based on Apache Kafka® and Kubernetes® that enables data and developer teams to unlock the full value of their data faster.

DataCater is a simple yet powerful approach to building modern, real-time data pipelines. According to reports of our users, data and dev teams save 40% of the time spent on crafting data pipelines and go from zero to production in a matter of minutes.

Users can choose from an extensive repository of [filter functions](https://github.com/DataCater/datacater/tree/main/filters), apply [transformations](https://github.com/DataCater/cloud-platform/tree/main/transforms), or code their own transforms in Python® to build their streaming data pipelines.

You find each component in this repository. See the [File Structure section](#file-structure) for orientation.

## Use Cases

### DataCater excels at

- Making real-time ETL pipelines accessible to data and developer teams
- Supporting Python-based transforms for ETL and streaming use cases
- Applying cloud-native principles to data development
- Supporting a declarative pipeline definition, which enables DataOps and Continuous Delivery
- Enabling the interactive development of ETL pipelines with minimal time to production

### DataCater is not built for

- EL or ELT pipelines with post-load transforms
- Analytics use cases that make use of aggregations or multiple joins
- Traditional batch processing

----

## File Structure
```
├── .github            - Workflows for GitHub
├── filters            - Pre-defined filters
├── gradle             - Build configuration based on Gradle (https://spring.io/guides/gs/gradle/)
├── helm-charts        - Source code for public Helm Charts
│   ├── ct.yaml        - Chart Testing Configuration File (https://github.com/helm/chart-testing)
│   └── datacater      - The official DataCater Helm Chart
├── k8s-manifests      - Kubernetes (K8) resources
├── licenses           - Overview of the licenses of our dependencies
├── pipeline           - Reference implementation of a pipeline
├── platform-api       - The main application for DataCater's API
├── python-runner      - Our runner for Python-based filters and transforms
├── serde              - Our (de)serializers
├── transforms         - Pre-defined transforms
├── ui                 - A ReactJS application built on top of DataCater's API.
├── CONTRIBUTING.md    - Describes how you can contribute to the project
├── gradle.properties  - Build properties
├── gradlew            - Build Wrapper Script (https://docs.gradle.org/current/userguide/gradle_wrapper.html)
├── README.md          - The file you are reading
└── settings.gradle    - Build tool properties
```

## Requirements

Make sure you have the following readily available before you proceed installing DataCater:

- [A running Kubernetes Cluster](https://kubernetes.io/docs/setup/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)

## To start using DataCater

For the time being, we provide the following approach to start using DataCater in your infrastructure:

1. [Via kubectl](#via-kubectl)

### Via kubectl

> **WARNING**: Installation uses the `default` namespace!
>
> The installation via `kubectl` uses the `default` namespace. If you wish to use a custom namespace, we recommend to install DataCater via Helm Chart or create the namespace upfront as described [here](#how-do-i-install-datacater-into-a-dedicated-namespace).

```
kubectl apply -f k8s-manifests/minikube-with-postgres-ns-default.yaml
```
3. Wait until all services are running
```
kubectl get all --all-namespaces
```
4. Port-forward to service
```
kubectl port-forward ui 8080:8080
```
5. Browse to `localhost:8080` in your browser. The default login credentials are `admin:admin`.

## Uninstalling DataCater

If you ever want to remove DataCater or want to start over again, e.g. during development, we recommend the following steps depending on the installation routine you've chosen:

> **WARNING**: We recommend to backup your data before proceeding

### Via kubectl
```
kubectl delete -f k8s-manifests/minikube-with-postgres-ns-default.yaml
```

## FAQ

### How do I install DataCater into a dedicated namespace?

1. Create the namespace
```
kubectl create namespace datacater
```
2. Apply manifests with `namespace` option
```
kubectl apply --namespace=datacater -f <url>
```

### How can I integrate DataCater with external data systems, like MySQL?

The open-core version of DataCater supports only Apache Kafka topics as sources and sinks for pipelines.
If you need to integrate your pipelines with external data systems,
please consider our Enterprise version, which offers connectors based on
Kafka Connect. We can offer a
[trial](#do-you-offer-a-trial-for-the-enterprise-version) to you.

### How can I extend the list of transforms and filters?

You can introduce new transforms and filters by adding a folder to the
directory `transforms` or `filters`. The new folder must contain a
`spec.yml` and a `transform.py` or `filter.py`.

DataCater automatically loads all transforms and filters from these
directories at startup time.

Please see [our documentation](https://docs.datacater.io) for further information.

### How can I contribute code changes?

Please have a look at our [guide for contributors](https://github.com/DataCater/datacater/blob/main/CONTRIBUTING.md).

### How can I submit feature requests?

Please open an issue in our GitHub repository. We will have a look at it
to see whether it fits our product roadmap.

### Do you offer a trial for the enterprise version?

Yes, please reach out to [support@datacater.io](mailto:support@datacater.io) to discuss options for a PoC project.

### What are the features in Open Core vs. Enterprise version?

| Feature                          | Open Core  | Enterprise  |
| :----------                      | :--------- | :---------- |
| API                              | ✅         |             |
| Interactive pipeline designer    | ✅         |             |
| Pre-defined transforms           | ✅         |             |
| Custom Python transforms         | ✅         |             |
| Pre-defined filters              | ✅         |             |
| Custom Python filters            | ✅         |             |
| Declarative pipeline definitions | ✅         |             |
| User authentication              | ✅         |             |
| CLI (coming soon)                | ✅         |             |
| Collaboration and projects       |            | ✅          |
| Plug & play connectors           |            | ✅          |
| Data masking                     |            | ✅          |
| SAML/SSO                         |            | ✅          |
| RBAC                             |            | ✅          |
| Audit log                        |            | ✅          |
| Health notifications             |            | ✅          |

## Support

We provide support and help in our [Community Slack](https://join.slack.com/t/datacater/shared_invite/zt-17cga6jg3-rGdgQZU6iX~mJGC8j~UNlw).

## License

DataCater is source-available and [licensed under the BSL 1.1](https://github.com/DataCater/cloud-platform/blob/main/LICENSE), converting to the open-source Apache 2.0 license 4 years after the release.
