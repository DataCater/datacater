package io.datacater.core.serde;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.datacater.core.exceptions.AvroSerializationException;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.kafka.common.serialization.Serializer;

public class AvroSerializer implements Serializer<GenericRecord> {
  private String schemaString;
  private KafkaAvroSerializer schemaSerializer;

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    boolean schemaExists = configs.containsKey("value.serializer.schema");
    boolean registryExists = configs.containsKey("schema.registry.url");
    if (!schemaExists && !registryExists) {
      throw new AvroSerializationException(
          "There has been no registry or schema defined. Cannot serialize AVRO");
    }
    if (schemaExists && registryExists) {
      throw new AvroSerializationException(
          "Both Schema Registry and Schema value have been defined. Please define only one to serialize AVRO values");
    }
    if (registryExists) {
      configureWithRegistry(configs, isKey);
    }
    if (schemaExists) {
      configureWithSchema(configs, isKey);
    }
  }

  private void configureWithRegistry(Map<String, ?> configs, boolean isKey) {
    schemaSerializer = new KafkaAvroSerializer();
    schemaSerializer.configure(configs, isKey);
  }

  private void configureWithSchema(Map<String, ?> configs, boolean isKey) {
    String propertyName = isKey ? "key.serializer.schema" : "value.serializer.schema";
    Object schemaValue = configs.get(propertyName);
    if (schemaValue == null) {
      schemaValue = configs.get("serializer.encoding");
    }
    if (schemaValue instanceof String schemaValueAsString) {
      schemaString = schemaValueAsString;
    }
  }

  @Override
  public byte[] serialize(final String topic, final GenericRecord data) {
    if (data == null) {
      return new byte[0];
    }
    if (schemaString != null) {
      return serializeWithSchema(topic, data);
    }
    return serializeWithRegistry(topic, data);
  }

  private byte[] serializeWithSchema(String topic, GenericRecord data) {
    try {
      GenericDatumWriter<GenericRecord> datumWriter =
          new GenericDatumWriter<>(new Schema.Parser().parse(schemaString));
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byteArrayOutputStream.reset();
      BinaryEncoder binaryEncoder = new EncoderFactory().binaryEncoder(byteArrayOutputStream, null);
      datumWriter.write(data, binaryEncoder);
      binaryEncoder.flush();
      byteArrayOutputStream.flush();
      return byteArrayOutputStream.toByteArray();
    } catch (Exception ex) {
      throw new AvroSerializationException(
          "Can't serialize data for topic '" + topic + "': " + ex.getMessage());
    }
  }

  private byte[] serializeWithRegistry(String topic, GenericRecord data) {
    return schemaSerializer.serialize(topic, data);
  }

  @Override
  public void close() {
    schemaSerializer.close();
  }
}
