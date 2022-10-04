package io.datacater.core.exceptions;

import static io.restassured.RestAssured.given;

import io.datacater.core.filter.FilterEndpoint;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(FilterEndpoint.class)
class UnauthorizedExceptionMapperTest {
  // extra test should be added once other roles are introduced.
  // This currently throws a 401, but not our custom exception.
  @Test
  void testUnauthorizedException() {
    Response response = given().headers("Authorization", "Bearer 1234").get();
    Assertions.assertEquals(401, response.getStatusCode());
  }
}
