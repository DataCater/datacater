package io.datacater.core.deployment;

public record DeploymentConfig(
    String streamInTopic,
    String streamInDeserializerClass,
    String streamInBootstrapServer,
    String streamOutTopic,
    String streamOutDeserializerClass,
    String streamOutBootstrapServer) {}
