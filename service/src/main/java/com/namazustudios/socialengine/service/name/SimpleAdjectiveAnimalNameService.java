package com.namazustudios.socialengine.service.name;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.service.NameService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

public class SimpleAdjectiveAnimalNameService implements NameService {

    private static final List<String> ANIMALS = load("/animals_en_US.txt");

    private static final List<String> ADJECTIVES = load("/adjectives_en_US.txt");

    private static List<String> load(final String file) {
        try (final InputStream is = SimpleAdjectiveAnimalNameService.class.getResourceAsStream(file);
             final BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            final List<String> values = new ArrayList<>();

            String value;
            while ((value = reader.readLine()) != null) values.add(value);

            return unmodifiableList(values);

        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public String generateRandomName() {
        final Random random = ThreadLocalRandom.current();
        final String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        final String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        return format("%s%s", adjective, animal);
    }

}
