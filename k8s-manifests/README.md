# Create Plain DataCater Minikube Manifest

```shell
helm template helm-charts/datacater \
  --skip-tests \
  --set "postgres.enabled=false" \
  --set "postgres.username=datacater" > k8s-manifests/minikube-any-namespace.yaml
```

# Create and working with Redpanda manifest
Redpanda offers a Helm chart for a quick and easy installation.
We convert this helm chart to a Kubernetes Manifest to provide a quick and easy 
installation and integration with our Open-Core product.
If the redpanda version needs to be updated, the following steps can be done.

1. Add the RedPanda repository to your helm client
```
helm repo add redpanda https://charts.redpanda.com/
```

2. Generate the kubernetes manifest from the helm chart, optionally changing some default values
```
helm template redpanda redpanda/redpanda \
--set statefulset.replicas=1,\
resources.cpu.cores=1,\
storage.persistentVolume.size=5Gi,\
post_install_job.enabled=false,\
post_upgrade_job.enabled=false\
> redpanda.yaml
```
>We only need the generated ConfigMap, two Services, and StatefulSet.

>We can, and probably should, also reduce the memory resources used by the StatefulSet.
>Under `spec.template.spec.containers.command`, the `--memory` can be reduced from 2048M, to 1024M. *it must be at least 1GB.*
>Then, the `--reserve-memory` needs to be reduced from 205 to 0M.
>After that, the `spec.template.spec.containers.resources.limits.memory` should be reduced from 2.5Gi to 1Gi

3. Copy the Statefulset, `redpanda` and `redpanda-external` services, and ConfigMap to the Datacater kubernetes manifest.

## working with Redpanda in Cluster
To access the broker inside datacater, the `bootsrap.servers` 
should be set to `redpanda-0.redpanda.default.svc.cluster.local.:9093`.

### Working with [rpk](https://docs.redpanda.com/docs/platform/reference/rpk/) inside Cluster
Redpanda offers a command line tool, `rpk`, that can be used to interact with the broker 
from inside the kubernetes cluster. rpk can be used with the following command:
```
kubectl exec -it -n [NAMESPACE] redpanda-0 -- rpk [COMMAND] --brokers='redpanda-0.redpanda.default.svc.cluster.local.:9093'
```

## Working with RedPanda from outside the cluster
The broker can also be access from outside the kubernetes cluster, locally. 
In order for this to work with the configured advertised address, 
The address resolution needs to be changed in your systems `/etc/hosts` file.
append the line `127.0.0.1 redpanda-0.redpanda.default.svc.cluster.local.` to the `/etc/hosts` file

The Broker cann then be accessed by port-forwarding:
```
kubectl port-forward redpanda-0 9093:9093
```

