package io.datacater.core.authentication;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(AuthenticationEndpoint.class)
@TestProfile(AuthenticationEndpointTest.BuildTimeValueChangeTestProfile.class)
class AuthenticationEndpointTest {

  @ConfigProperty(name = "smallrye.jwt.new-token.lifespan")
  private int lifespan;

  @Test()
  void testGetTokenWithCredentials() throws JsonProcessingException {
    Response response = given().auth().preemptive().basic("admin", "admin").when().post();

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

  public static class BuildTimeValueChangeTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("datacater.authorization.basic", "true");
    }
  }
}
