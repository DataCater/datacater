package io.datacater.core.utilities;

import org.jboss.logging.Logger;

public class LoggerUtilities {

  private LoggerUtilities() {}

  /**
   * Log an exception thrown with an added context
   *
   * @param logger The JBoss logger instance used for logging
   * @param methodName The method name from which the exception occurred
   * @param exceptionMessage The exception message to log
   */
  public static void logExceptionMessage(
      Logger logger, String methodName, String exceptionMessage) {
    logger.error(
        "An error occurred while executing the '" + methodName + "' method: " + exceptionMessage);
  }

  /**
   * Get the cause of an exception message. This method is used when an exception is encapsulated
   * inside another exception to receive the original cause thrown.
   *
   * @param ex Exception to extract the cause from
   * @return the cause of an exception if it exists, otherwise return the exception message.
   */
  public static String getExceptionCauseIfAvailable(Exception ex) {
    if (ex.getCause() == null) {
      return ex.getMessage();
    }
    return ex.getCause().getMessage();
  }
}
