package io.datacater.core.stream;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The representation of an external data stream.
 *
 * <p>The user of this interface can perform actions on the external data stream, such as getting
 * its metadata/config, creating the external data stream, and inspecting the external data stream.
 */
public interface StreamService {
  /**
   * Creates the stream unless it already exists.
   *
   * @return Metadata of the stream, where keys are the config names and values are the config
   *     values.
   */
  StreamSpec apply(StreamSpec spec);

  /**
   * Get the metadata of a stream.
   *
   * @return Metadata of the stream, where keys are the config names and values are the config
   *     values.
   */
  Map<String, String> getMetadata();

  /**
   * Inspect (or retrieve) the most recent events of a stream.
   *
   * @param limit Number of records to retrieve.
   * @return
   */
  List<StreamMessage> inspect(Stream stream, long limit);

  /**
   * Create a Topic with the given specifications.
   *
   * @param spec specification of the stream to be created.
   * @return the created Stream specification
   */
  StreamSpec createStream(StreamSpec spec);

  /** Close the instance of the used Streaming Client. */
  void close();

  /**
   * Update an already existing Topic.
   *
   * @param spec specification of the stream to be updated.
   * @return the updated Stream specification
   */
  StreamSpec updateStream(StreamSpec spec);

  /** Deletes the defined stream. */
  CompletableFuture<Void> deleteStream();

  Boolean isValidConfig(Map<String, String> config);
}
