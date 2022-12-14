# One-click installation with kubectl

```shell

kubectl apply -f https://install.datacater.io/minikube
```

# Advanced installation with Helm

If you decide to install DataCater and related services into its own namespace
you should change set the environment variable `DATACATER_NAMESPACE` accordingly before proceeding with the installation:


```shell
export DATACATER_NAMESPACE="default"
```

1. Install Postgres via

```shell
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install postgres bitnami/postgresql -n $DATACATER_NAMESPACE
```

This will install a single PostgreSQL database instance.
You can access the database password of the default user, `postgresql`,
as follows:

```shell
export POSTGRES_PASSWORD=$(kubectl get secret --namespace $DATACATER_NAMESPACE postgres-postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)
```

2. Initiate tables and policies for DataCater via

*NOTE*: You need [psql](https://www.postgresql.org/docs/current/app-psql.html)
installed for this. Feel free to use any other database client.

Open a second terminal and open a connection to the PostgreSQL instance
in your Kubernetes cluster as follows:

```shell
kubectl port-forward --namespace $DATACATER_NAMESPACE svc/postgres-postgresql 5432:5432
```

Seed the database schema with the following command:

```shell
PGPASSWORD="$POSTGRES_PASSWORD" psql --host 127.0.0.1 -U postgres -d postgres -p 5432 -f platform-api/src/main/resources/init-db.sql
```

3. Install DataCater via Helm

*NOTE* We `--set` the value to the `postgres-postgresql` service that runs in your Kubernetes namespace. Please change `datacater.database.host` if you use a different host, port, or database name.

```shell
helm -n $DATACATER_NAMESPACE upgrade datacater helm-charts/datacater --set "datacater.database.host=postgres-postgresql:5432/postgres"
```

4. Access the DataCater UI

Create a `port-forward` to access the UI pod of DataCater:

```shell
kubectl -n $DATACATER_NAMESPACE port-forward svc/datacater-ui 8080:80
```

You can now access the UI in your browser by navigating to
`http://localhost:8080` and sign in using the default credentials `admin:admin`.
