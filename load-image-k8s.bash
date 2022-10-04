#!/bin/bash

set -eux

NAME="datacater"
VERSION="alpha.11"
START_DIR=$PWD


echo "cd src/platform-api/srcf/main/resources"

cd platform-api/src/main/resources

echo "Generating certificates"

openssl genrsa -out rsaPrivateKey.pem 2048
openssl rsa -pubout -in rsaPrivateKey.pem -out publicKey.pem
openssl pkcs8 -topk8 -nocrypt -inform pem -in rsaPrivateKey.pem -outform pem -out privateKey.pem

echo "Back to root: cd $START_DIR"
cd "$START_DIR"


echo "Copy transforms and filters into jib builder"
# mkdir platform-api/src/main/jib if not exists
#cp -R filters platform-api/src/main/jib
#cp -R transforms platform-api/src/main/jib


echo "Building version $VERSION"

./gradlew :platform-api:build --info --no-daemon -x test\
  -Dquarkus.container-image.build=true\
  -Dquarkus.application.name=$NAME\
  -Dquarkus.application.version=$VERSION

echo "Build image datacater/$NAME:$VERSION"

minikube image load datacater/$NAME:$VERSION

echo "Loaded image datacater/$NAME:$VERSION"

 helm template ./helm-charts/datacater\
   --set image.tag=$VERSION --skip-tests\
   --set image.repository=datacater/$NAME --skip-tests\
    > k8s-manifests/minikube-with-postgres-ns-default.yaml

echo "Updated helm template."
