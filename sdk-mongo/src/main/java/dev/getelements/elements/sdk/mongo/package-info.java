@ElementPublic
@ElementDefinition
@ElementService(SslSettings.class)
@ElementService(MongoClient.class)
@ElementService(MongoDatabase.class)
@GuiceElementModule(MongoSdkModule.class)
@GuiceElementModule(MongoCoreModule.class)
package dev.getelements.elements.sdk.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.SslSettings;
import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementService;
import dev.getelements.elements.sdk.mongo.guice.MongoCoreModule;
import dev.getelements.elements.sdk.mongo.guice.MongoSdkModule;
import dev.getelements.elements.sdk.spi.guice.annotations.GuiceElementModule;