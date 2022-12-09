package io.datacater;

import org.eclipse.microprofile.config.ConfigProvider;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PipelineConfig {

    private PipelineConfig() {}

    static final Integer DATACATER_PYTHONRUNNER_PORT =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.python-runner.port", Integer.class)
                    .orElse(50000);

    static final String DATACATER_PYTHONRUNNER_HOST =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.python-runner.host", String.class)
                    .orElse("localhost");

    static final Integer DATACATER_PYTHONRUNNER_TIMEOUT =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.python-runner.timeout", Integer.class)
                    .orElse(60);

    static final String STREAM_IN = "stream-in";
    static final String STREAM_OUT = "stream-out";
    static final String ENDPOINT = "/batch";
    static final String HEADER = "Content-Type";
    static final String HEADER_TYPE = "application/json";
    static final String KEY = "key";
    static final String VALUE = "value";
    static final String METADATA = "metadata";
    static final String ERROR = "error";
    static final String OFFSET = "offset";
    static final String PARTITION = "partition";
    static final String PIPELINE_ERROR_MSG =
            "Pipeline could not process record.\n Key: %s\n Value: %s\n Metadata: %s\n Error: %s";
    /*
     * Number of retries in case of a failed connection attempt
     */
    static final Integer CONNECTION_RETRIES = 10;
    /**
     * Number of milliseconds to wait between retrying to connect
     */
    static final Integer CONNECTION_RETRY_WAIT = 1000;
}
