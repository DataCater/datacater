package io.datacater.core.authentication;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RolesTest {
  @Test
  void testValueOf() {
    Assertions.assertEquals("dev", Roles.DEV.role);
  }
}
