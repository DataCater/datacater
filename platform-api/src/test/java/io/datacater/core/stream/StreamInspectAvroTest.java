package io.datacater.core.stream;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;
import javax.inject.Inject;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(StreamEndpoint.class)
class StreamInspectAvroTest {
  Producer<GenericRecord, GenericRecord> producerWithSchema;

  @Inject
  @Channel("testStreamTopicOutAvro")
  Emitter<ProducerRecord<GenericRecord, GenericRecord>> producerWithRegistry;

  private final String schemaPath =
      ClassLoader.getSystemClassLoader().getResource("streamTestFiles/testSchema.avsc").getFile();
  private Schema schema;

  UUID uuid;

  @Test
  void testAvroDeserializerWithRegistry() throws IOException, InterruptedException {
    startWithRegistry();

    for (int i = 0; i <= 300; i++) {
      producerWithRegistry.send(
          new ProducerRecord<>(
              "streamTestWithRegistry",
              buildRecord("test" + i, 1000 + i),
              buildRecord("test" + i, 2000 + i)));
    }

    // wait on records to finish
    Thread.sleep(1000);

    Response response =
        given().pathParam("uuid", uuid.toString()).queryParam("limit", "3").get("/{uuid}/inspect");

    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertTrue(response.body().asString().contains("{\"title\":\"test"));
  }

  @Test
  void testAvroDeserializerWithSchema() throws IOException, InterruptedException {
    startWithoutRegistry();

    for (int i = 1; i <= 300; i++) {
      producerWithSchema.send(
          new ProducerRecord(
              "streamTest", buildRecord("test" + i, 1000 + i), buildRecord("test" + i, 2000 + i)));
    }

    // wait on records to finish
    Thread.sleep(1000);

    Response response =
        given().pathParam("uuid", uuid.toString()).queryParam("limit", "3").get("/{uuid}/inspect");

    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertTrue(response.body().asString().contains("{\"title\":\"test"));
  }

  void startWithoutRegistry() throws IOException {
    schema = new Schema.Parser().parse(new File(schemaPath));

    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-inspect-avro-schema.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given();
    String json = streamJson.toString();
    json = json.replace("SchemaPlaceholder", schema.toString().replace("\"", "\\\""));
    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.post();
    StreamEntity se = mapper.readValue(response.body().asString(), StreamEntity.class);
    uuid = se.getId();

    Properties props = new Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("value.serializer", io.datacater.core.serde.AvroSerializer.class);
    props.put("value.serializer.schema", schema.toString());
    props.put("key.serializer", io.datacater.core.serde.AvroSerializer.class);
    props.put("key.serializer.schema", schema.toString());

    producerWithSchema = new KafkaProducer<>(props);
  }

  private GenericRecord buildRecord(String title, int year) {
    GenericData.Record record = new GenericData.Record(schema);
    record.put("title", title);
    record.put("year", year);

    return record;
  }

  void startWithRegistry() throws IOException {
    schema = new Schema.Parser().parse(new File(schemaPath));

    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-inspect-avro-registry.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given();
    String json = streamJson.toString();
    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.post();
    StreamEntity se = mapper.readValue(response.body().asString(), StreamEntity.class);
    uuid = se.getId();
  }
}
