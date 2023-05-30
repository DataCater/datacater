package io.datacater.core.pipeline;

public class StaticConfig {
  private StaticConfig() {}

  static final String PIPELINE_NOT_FOUND = "The referenced Pipeline UUID could not be found";
  static final String PIPELINE_NODE_TEXT = "pipeline";
  static final String STREAM_IN_TEXT = "stream-in";
  static final String PIPELINE_PATH = "/pipeline";
  static final String PAYLOAD_SENT_UPDATE_MSG = "Will send following payload after spec update";
  static final String PREVIEW_PATH = "/preview";

  static class FormattedMessages {
    private FormattedMessages() {}

    static final String PYTHON_RUNNER_TIMEOUT_FORMATTED_MSG =
        "Calling the Python runner exceeded the timeout of datacater.pythonrunner.preview.timeout=%d.";
    static final String RECEIVED_RESPONSE_FORMATTED_MSG =
        "Received response from %s with status %d";
  }
}
