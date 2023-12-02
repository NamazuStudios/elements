package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.ItemDao;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.goods.ItemCategory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public class ItemTestFactory {

    private static final AtomicInteger suffix = new AtomicInteger();

    private ItemDao itemDao;

    public Item createTestItem(final ItemCategory category, final boolean isPublic) {
        return createTestItem(category, "test", isPublic);
    }

    public Item createTestItem(final ItemCategory category, final String name, final boolean isPublic) {
        return createTestItem(category, name, "Integration Testing Item.", isPublic);
    }

    public Item createTestItem(final ItemCategory category, final String name, final String description, final boolean isPublic) {
        return createTestItem(category, name, description, List.of(), isPublic);
    }

    public Item createTestItem(final ItemCategory category,
                               final String name,
                               final String description,
                               final List<String> tags,
                               final boolean isPublic) {
        return createTestItem(category, name, description, tags, Map.of(), isPublic);
    }

    private Item createTestItem(
            final ItemCategory category,
            final String name,
            final String description,
            final List<String> tags,
            final Map<String, Object> metadata,
            final boolean isPublic) {
        final var item = new Item();
        final var fullyQualifiedName = format("%s%d", name, suffix.getAndIncrement());
        item.setName(fullyQualifiedName);
        item.setDisplayName(fullyQualifiedName);
        item.setDescription(description);
        item.setTags(tags);
        item.setMetadata(metadata);
        item.setCategory(category);
        item.setPublicVisible(isPublic);
        return getItemDao().createItem(item);
    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    @Inject
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

}
