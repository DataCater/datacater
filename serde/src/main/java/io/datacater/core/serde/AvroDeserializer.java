package io.datacater.core.serde;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.datacater.core.exceptions.AvroDeserializationException;
import java.util.Map;

import io.vertx.core.json.JsonObject;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.kafka.common.serialization.Deserializer;

@SuppressWarnings("unchecked")
public class AvroDeserializer implements Deserializer<Map<String, Object>> {
  private String schemaString;

  private KafkaAvroDeserializer schemaDeserializer;

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    boolean schemaExists = configs.containsKey("value.deserializer.schema");
    boolean registryExists = configs.containsKey("schema.registry.url");

    if (!schemaExists && !registryExists) {
      throw new AvroDeserializationException(
          "There has been no registry or schema defined. Cannot deserialize AVRO");
    }
    if (schemaExists && registryExists) {
      throw new AvroDeserializationException(
          "Both Schema Registry and Schema value have been defined. PLease define only one to Deserialize AVRO values");
    }
    if (registryExists) {
      configureWithRegistry(configs, isKey);
    }
    if (schemaExists) {
      configureWithSchema(configs, isKey);
    }
  }

  private void configureWithRegistry(Map<String, ?> configs, boolean isKey) {
    schemaDeserializer = new KafkaAvroDeserializer();
    schemaDeserializer.configure(configs, isKey);
  }

  private void configureWithSchema(Map<String, ?> configs, boolean isKey) {
    String propertyName = isKey ? "key.deserializer.schema" : "value.deserializer.schema";
    Object schemaValue = configs.get(propertyName);
    if (schemaValue == null) {
      schemaValue = configs.get("deserializer.encoding");
    }
    if (schemaValue instanceof String schemaValueAsString) {
      schemaString = schemaValueAsString;
    }
  }

  @Override
  public Map<String, Object> deserialize(String topic, byte[] data) {
    if (data == null) {
      return null;
    }

    GenericRecord record = null;
    if (schemaString != null) {
      record = deserializeWithSchema(topic, data);
    } else {
      record = deserializeWithRegistry(topic, data);
    }

    if (record == null) {
      return null;
    } else {
      return new JsonObject(record.toString()).getMap();
    }
  }

  private GenericRecord deserializeWithSchema(String topic, byte[] data) {
    try {
      DatumReader<GenericRecord> datumReader =
          new GenericDatumReader<>(new Schema.Parser().parse(schemaString));
      Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
      return datumReader.read(null, decoder);
    } catch (Exception ex) {
      throw new AvroDeserializationException(
          "Can't deserialize data from topic '" + topic + "': " + ex.getMessage());
    }
  }

  private GenericRecord deserializeWithRegistry(String topic, byte[] data) {
    return (GenericRecord) schemaDeserializer.deserialize(topic, data);
  }

  @Override
  public void close() {
    if (schemaDeserializer != null) {
      schemaDeserializer.close();
    }
  }
}
