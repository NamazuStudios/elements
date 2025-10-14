package dev.getelements.elements.sdk.model.exception;

import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.match.MultiMatchStatus;

import static java.util.Arrays.*;

public class InvalidMultiMatchPhaseException extends InvalidDataException {

  private final MultiMatchStatus actual;

  private final MultiMatchStatus[] expected;

  public InvalidMultiMatchPhaseException(final MultiMatchStatus actual,
                                         final MultiMatchStatus ... expected) {

    super("Invalid MultiMatch status: " + actual +
          ", expected: " + (expected.length == 0 ? "any" : String.join(", ",
              stream(expected).map(Enum::name).toList()))
    );

    this.actual = actual;
    this.expected = expected;

  }

  /**
   * Gets the actual status of the {@link MultiMatch}.
   *
   * @return the status
   */
  public MultiMatchStatus getActual() {
    return actual;
  }

  /**
   * Gets the expected status of the {@link MultiMatch}. That is to say the status which was required for the operation
   * to succeed.
   *
   * @return the expected status
   */
  public MultiMatchStatus[] getExpected() {
    return expected.clone();
  }

}
