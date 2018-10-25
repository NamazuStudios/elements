package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.Item;

import java.util.Set;

public interface ItemService {

    Item getItemByIdOrName(String identifier);

    Pagination<Item> getItems(int offset, int count, Set<String> tags, String query);

    Item updateItem(Item item);

    Item createItem(Item item);
}
