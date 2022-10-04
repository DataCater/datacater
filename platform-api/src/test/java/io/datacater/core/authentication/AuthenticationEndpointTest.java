package io.datacater.core.authentication;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(AuthenticationEndpoint.class)
class AuthenticationEndpointTest {

  @ConfigProperty(name = "smallrye.jwt.new-token.lifespan")
  private int lifespan;

  @Test()
  void testGetTokenWithCredentials() throws JsonProcessingException {
    Response response = given().auth().preemptive().basic("superman", "1derW0man.").when().post();

    Assertions.assertEquals(200, response.getStatusCode());

    ObjectMapper objectMapper = new ObjectMapper();
    Token token = objectMapper.readValue(response.getBody().asString(), Token.class);

    Assertions.assertEquals("Bearer", token.tokenType());
    Assertions.assertEquals(lifespan, token.expiresIn());
  }

  @Test()
  void testGetTokenWithoutCredentials() {
    given().post().then().statusCode(401);
  }

  @Test()
  void testGetTokenWithIncorrectCredentials() {
    given().auth().preemptive().basic("admin", "test").post().then().statusCode(401);
  }
}
