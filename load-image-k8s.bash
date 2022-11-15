#!/bin/bash

set -eux

DAY_HOUR_MIN="$(date '+%d-%H-%M')"
NAME="datacater"
VERSION="$DAY_HOUR_MIN"
#VERSION="local.20"
START_DIR=$PWD


echo "Building version $VERSION"

./gradlew :platform-api:build --info --no-daemon -x test\
  -Dquarkus.container-image.build=true\
  -Dquarkus.application.name=$NAME\
  -Dquarkus.application.version=$VERSION

echo "Build image datacater/$NAME:$VERSION"

minikube image load datacater/$NAME:$VERSION

echo "Loaded image datacater/$NAME:$VERSION"

 helm template ./helm-charts/datacater\
   --set image.tag=$VERSION\
   --set image.repository=datacater/$NAME --skip-tests\
    > k8s-manifests/minikube-with-postgres-ns-default.yaml

echo "Updated helm template."
