package com.namazustudios.socialengine.service.firebase;

import com.google.firebase.FirebaseApp;
import com.namazustudios.socialengine.model.application.Application;

import java.util.function.Function;

@FunctionalInterface
public interface FirebaseAppFactory extends Function<Application, FirebaseApp> {}
