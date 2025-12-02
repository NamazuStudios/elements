package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.ucode.UniqueCodeNotFoundException;
import dev.getelements.elements.sdk.model.ucode.UniqueCode;

import java.util.Optional;

/**
 * DAO for generating unique codes with a given prefix. The underlying implementation is responsible for ensuring that
 * the generated codes are unique within the specified timeout period or until the code is explicitly released.
 *
 * The codes intended to be easily to read and recall, so the default length is kept short (4 characters). If a higher
 * degree of uniqueness is required, the length can be increased. The implementation must generate codes that are
 * unambiguous and avoid characters that can be easily confused (e.g., 'O' and '0', 'I' and '1'). Additionally, the
 * generated codes avoid offensive or inappropriate combinations suitable for all audiences.
 *
 * Even for games or products that are intended for adult audiences, the codes should remain appropriate as the goal is
 * to facilitate easy sharing and recall among users.
 *
 * @author patrickt
 */
public interface UniqueCodeDao {

    /**
     * The default length for generated codes.
     */
    int DEFAULT_CODE_LENGTH = 4;

    /**
     * The default timeout for which the generated code will linger after being released. 5 minutes in milliseconds.
     */
    long DEFAULT_LINGER_MS = 300 * 1000;

    /**
     * The default timeout for which the generated code should remain unique. 1 hour in milliseconds.
     */
    long DEFAULT_TIMEOUT_MS = 3600 * 1000;

    /**
     * TGenerates a unique code with the default length and timeout.
     *
     * @return the generated unique code
     */
    default UniqueCode generateCode() {
        return generateCode(GenerationParameters.defaults());
    }

    /**
     * Generates a unique code with the default length.
     *
     * @param parameters the generation parameters
     * @return the generated unique code
     */
    UniqueCode generateCode(GenerationParameters parameters);

    /**
     * Gets the UniqueCode object for the specified code. Can be used to check if a code is still valid.
     * @param code the code
     * @return the UniqueCode object, never null
     * @throws UniqueCodeNotFoundException if the code is not found
     */
    default UniqueCode getCode(final String code) {
        return findCode(code).orElseThrow(UniqueCodeNotFoundException::new);
    }

    /**
     * Finds the UniqueCode object for the specified code.
     * @param code the code or an empty Optional if not found
     * @return the UniqueCode object wrapped in an Optional
     */
    Optional<UniqueCode> findCode(String code);

    /**
     * Resets the timeout for the specified code, extending its uniqueness period.
     *
     * @param code the code to reset the timeout for
     */
    void resetTimeout(String code, long timeout);

    /**
     * Releases the specified code, allowing it to be reused after a short linger period.
     * @param code the code
     */
    void releaseCode(String code);

    /**
     * Attempts to release the specified code within the given timeout period. Once released, the code can be reused
     * after a short linger period.
     *
     * @param code the code
     * @return true if the code was successfully released, false otherwise
     */
    boolean tryReleaseCode(String code);

    /**
     * Generation parameters for generating unique codes.
     * @param timeout the timeout, in milliseconds, for the code
     * @param linger the linger, in milliseconds, after the code is released before being made available for reuse
     * @param length the length of the generated code. Implementations may force specific rules on the length but must
     *               support a minimum of 4 characters and a maximum of 8 characters.
     */
    record GenerationParameters(
            long timeout,
            long linger,
            int length) {

        /**
         * Constructor that sets default values for any parameters that are zero.
         * @param timeout the timeout time
         * @param linger the linger time
         * @param length the length of the code
         */
        public GenerationParameters {
            linger = linger == 0 ? DEFAULT_LINGER_MS : linger;
            length = length == 0 ? DEFAULT_CODE_LENGTH : length;
            timeout = timeout == 0 ? DEFAULT_TIMEOUT_MS : timeout;
        }

        /**
         * Returns the default generation parameters.
         *
         * @return the default generation parameters
         */
        static GenerationParameters defaults() {
            return new GenerationParameters(0,0, 0);
        }

    }

}
