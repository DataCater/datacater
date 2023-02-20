# Create Minikube manifest

```shell
helm template helm-charts/datacater \
  --skip-tests \
  --set "postgres.enabled=false" \
  --set "postgres.username=datacater" > k8s-manifests/minikube-any-namespace.yaml
```

# Create Minikube manifest for installing DataCater into the default namespace

```shell
helm template helm-charts/datacater -ndefault \
  --skip-tests \
  --set "postgres.enabled=true" \
  --set "datacater.database.username=postgres" \
  --set "postgres.username=postgres" > k8s-manifests/minikube-with-postgres-ns-default.yaml
```

# Create Minikube manifest with DataCater and Redpanda

Redpanda offers a Helm chart for a quick and easy installation.
We convert this Helm chart to a Kubernetes Manifest to provide a quick and easy 
installation and integration with our open-core product.
If you want to update Redpanda, please perform the following steps:

1. Add the Redpanda repository to your local Helm client.
```
helm repo add redpanda https://charts.redpanda.com/
```

2. Generate the Kubernetes manifest from the Helm chart. Optionally, adjust configuration values.
```
helm template redpanda redpanda/redpanda \
  --set "statefulset.replicas=1" \
  --set "resources.cpu.cores=1" \
  --set "storage.persistentVolume.size=5Gi" \
  --set "post_install_job.enabled=false" \
  --set "post_upgrade_job.enabled=false"  > redpanda.yaml
```
>We only need the generated ConfigMap, two Services, and StatefulSet.

>We can, and probably should, also reduce the memory resources used by the StatefulSet.
>Under `spec.template.spec.containers.command`, the `--memory` can be reduced from 2048M, to 1024M. *it must be at least 1GB.*
>Then, the `--reserve-memory` needs to be reduced from 205 to 0M.
>After that, the `spec.template.spec.containers.resources.limits.memory` should be reduced from 2.5Gi to 1Gi

3. Copy the StatefulSet, the Services `redpanda` and `redpanda-external`, and the ConfigMap to DataCater's Kubernetes manifest, which you generated as describe above.

## Accessing Redpanda from within the cluster

To access the Redpanda broker from inside the Kubernetes cluster, e.g.,
from DataCater, you can set `bootstrap.servers` to `redpanda-0.redpanda.default.svc.cluster.local.:9093`.

### Working with [rpk](https://docs.redpanda.com/docs/platform/reference/rpk/) inside the cluster

Redpanda offers a command line tool, `rpk`, that can be used to administrate the Redpanda broker.
You can use `rpk` with the following command:
```
kubectl exec -it -n [NAMESPACE] redpanda-0 -- rpk [COMMAND] --brokers='redpanda-0.redpanda.default.svc.cluster.local.:9093'
```

## Accessing Redpanda from outside the cluster

The Redpanda broker can also be accessed from outside the Kubernetes cluster, i.e., from your host machine.
To this end, you need to perform two steps.

1. Please append the line `127.0.0.1 redpanda-0.redpanda.default.svc.cluster.local.` to the `/etc/hosts` file of your host OS, such that the advertised listener is correctly working when accessing Redpanda.

2. Create a port forward to allow accessing Redpanda from your host:
```
kubectl port-forward redpanda-0 9093:9093
```
