package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import org.bson.BsonDocument;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Pins the {@link MongoIndexConflictResolver} message parsing against the exact formats MongoDB produces for
 * index-creation conflicts:
 *
 * <ul>
 *     <li>error 85 ({@code IndexOptionsConflict}): same key spec, different name -- names the existing index
 *     bare and unquoted, e.g. {@code Index already exists with a different name: name_1};</li>
 *     <li>error 86 ({@code IndexKeySpecsConflict}): renders the existing index as a document whose quoted
 *     {@code name: "..."} is the last one in the message.</li>
 * </ul>
 */
public class MongoIndexConflictResolverTest {

    private final MongoIndexConflictResolver resolver = new MongoIndexConflictResolver();

    @Test
    public void testResolvesError85NamedBare() {

        final var collection = mock(MongoCollection.class);

        final var cause = cause(85, "Index already exists with a different name: name_1");

        assertTrue(resolver.resolve(collection, cause));
        verify(collection).dropIndex("name_1");

    }

    @Test
    public void testResolvesError85NamedWithQuotes() {

        final var collection = mock(MongoCollection.class);

        final var cause = cause(85, "Index already exists with a different name: \"name_1\"");

        assertTrue(resolver.resolve(collection, cause));
        verify(collection).dropIndex("name_1");

    }

    @Test
    public void testResolvesError86UsingExistingIndexDocument() {

        final var collection = mock(MongoCollection.class);

        // Note the requested index (name_1_sparse) is reported first and the existing on-disk index
        // (name_1) second; the resolver must drop the EXISTING one.
        final var message = "Index: name_1_sparse  Properties: { v: { $numberInt: \"2\" }, "
                + "key: { name: 1 }, name: \"name_1_sparse\", unique: true, sparse: true }  Conflicts with: "
                + "{ v: { $numberInt: \"2\" }, key: { name: 1 }, name: \"name_1\", unique: true }";

        final var cause = cause(86, message);

        assertTrue(resolver.resolve(collection, cause));
        verify(collection).dropIndex("name_1");

    }

    @Test
    public void testUnresolvableCodeReturnsFalse() {

        final var collection = mock(MongoCollection.class);
        final var cause = cause(11000, "E11000 duplicate key error: dup key");

        assertFalse(resolver.resolve(collection, cause));
        verifyNoInteractions(collection);

    }

    @Test
    public void testUnparseableMessageReturnsFalse() {

        final var collection = mock(MongoCollection.class);
        final var cause = cause(85, "something else entirely");

        assertFalse(resolver.resolve(collection, cause));
        verify(collection, never()).dropIndex(anyString());

    }

    private MongoCommandException cause(final int code, final String message) {
        final var cause = mock(MongoCommandException.class);
        when(cause.getErrorCode()).thenReturn(code);
        when(cause.getErrorMessage()).thenReturn(message);
        when(cause.getErrorCodeName()).thenReturn("TEST");
        when(cause.getResponse()).thenReturn(new BsonDocument());
        return cause;
    }

}