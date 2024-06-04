package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.user.User;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class MongoProfileTestOperations {

    private UserTestFactory userTestFactory;

    private ApplicationTestFactory applicationTestFactory;

    private List<User> users;

    private List<Application> applications;

    public void createUsersAndApplications(final Class<?> testClass, int applicationCount, int userCount) {

        users = IntStream.range(0, userCount)
                .mapToObj(i -> getUserTestFactory().createTestUser())
                .collect(toList());

        applications = IntStream.range(0, applicationCount)
                .mapToObj(i -> getApplicationTestFactory().createMockApplication(testClass))
                .collect(toList());

    }

    public Object[][] applicationsAndUsers() {
        return applications
                .stream()
                .flatMap(application -> users.stream().map(user -> new Object[]{application, user}))
                .toArray(Object[][]::new);
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public ApplicationTestFactory getApplicationTestFactory() {
        return applicationTestFactory;
    }

    @Inject
    public void setApplicationTestFactory(ApplicationTestFactory applicationTestFactory) {
        this.applicationTestFactory = applicationTestFactory;
    }

}
