package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity(value = "session", useDiscriminator = false)
public class MongoGoogleSignInSession extends MongoSession {

}
