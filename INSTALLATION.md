# One Click Installation

```shell

kubectl apply -f https://install.datacater.io/minikube
```

# Advanced Installation

If you decide to install datacater and related services into its own namespace
you should change `default` accordingly:


```shell
export DATACATER_NAMESPACE="default"
```

1. Install Postgres via

```shell
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install postgres bitnami/postgresql -n $DATACATER_NAMESPACE
```

This will install a single instance PostgreSQL database instance and with the
following command you can acces the password.

```shell
export POSTGRES_PASSWORD=$(kubectl get secret --namespace $DATACATER_NAMESPACE postgres-postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)
```

2. Initiate tables and policies for datacater via

*NOTE*: You need [psql](https://www.postgresql.org/docs/current/app-psql.html)
installed for this. Feel free to use any other database client.

Open a second terminal and open a connection into the cluster via:

```shell
kubectl port-forward --namespace $DATACATER_NAMESPACE svc/postgres-postgresql 5432:5432
```

Load the required relations and policies with:

```shell
PGPASSWORD="$POSTGRES_PASSWORD" psql --host 127.0.0.1 -U postgres -d postgres -p 5432 -f platform-api/src/main/resources/init-db.sql
```

3. Install datacater via helm

*NOTE* we `--set` the value to the `postgres-postgresql` service within the
namespace datacater was installed in. Change this to you port host, port, and
schema as you like.

```shell
helm -n $DATACATER_NAMESPACE upgrade datacater helm-charts/datacater --set "datacater.database.host=postgres-postgresql:5432/postgres"
```

4. Access the datacater ui

Port-forward you traffic into your kubernetes cluster to get a quick access
into the ui, your default password ist `admin:admin`

```shell
kubectl -n $DATACATER_NAMESPACE port-forward svc/datacater-ui 8080:80
```
