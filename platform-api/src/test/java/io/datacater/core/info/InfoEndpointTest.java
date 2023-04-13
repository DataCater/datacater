package io.datacater.core.info;

import static io.restassured.RestAssured.given;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(InfoEndpoint.class)
public class InfoEndpointTest {
  @Test
  void testGetInfo() {
    given().get().then().statusCode(200);
  }
}
