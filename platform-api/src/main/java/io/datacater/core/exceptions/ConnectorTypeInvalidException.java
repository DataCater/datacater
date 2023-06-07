package io.datacater.core.exceptions;

public class ConnectorTypeInvalidException extends RuntimeException {
  public ConnectorTypeInvalidException(String message) {
    super(message);
  }

  public ConnectorTypeInvalidException(String message, Throwable cause) {
    super(message, cause);
  }
}
