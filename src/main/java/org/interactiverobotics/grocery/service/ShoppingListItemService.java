/*
 * ShoppingListItemService.java
 *
 * Copyright (C) 2016 Pavel Prokhorov (pavelvpster@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.interactiverobotics.grocery.service;

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.domain.ShoppingListItem;
import org.interactiverobotics.grocery.exception.ItemNotFoundException;
import org.interactiverobotics.grocery.exception.ShoppingListItemNotFoundException;
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ShoppingListItem service.
 */
@Service
public class ShoppingListItemService {

    private final ShoppingListRepository shoppingListRepository;
    private final ItemRepository itemRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;

    @Autowired
    public ShoppingListItemService(final ShoppingListRepository shoppingListRepository,
                                   final ItemRepository itemRepository,
                                   final ShoppingListItemRepository shoppingListItemRepository) {

        this.shoppingListRepository = shoppingListRepository;
        this.itemRepository = itemRepository;
        this.shoppingListItemRepository = shoppingListItemRepository;
    }

    public ShoppingListItem addItem(final Long shoppingListId, final Long itemId, final Long quantity) {
        final ShoppingList shoppingList = this.shoppingListRepository.findOne(shoppingListId);
        if (shoppingList == null) {
            throw new ShoppingListNotFoundException(shoppingListId);
        }
        final Item item = this.itemRepository.findOne(itemId);
        if (item == null) {
            throw new ItemNotFoundException(itemId);
        }
        return addItem(shoppingList, item, quantity);
    }

    public ShoppingListItem addItem(final ShoppingList shoppingList, final Item item, final Long quantity) {

        // Quantity must be > 0
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0!");
        }

        return this.shoppingListItemRepository.save(new ShoppingListItem(shoppingList, item, quantity));
    }

    public void removeItem(final Long shoppingListId, final Long itemId) {
        final ShoppingList shoppingList = this.shoppingListRepository.findOne(shoppingListId);
        if (shoppingList == null) {
            throw new ShoppingListNotFoundException(shoppingListId);
        }
        final Item item = this.itemRepository.findOne(itemId);
        if (item == null) {
            throw new ItemNotFoundException(itemId);
        }
        removeItem(shoppingList, item);
    }

    public void removeItem(final ShoppingList shoppingList, final Item item) {
        removeItem(Optional.ofNullable(this.shoppingListItemRepository
                .findOneByShoppingListAndItem(shoppingList, item))
                .orElseThrow(() -> new ShoppingListItemNotFoundException(-1L)));
    }

    public void removeItem(final ShoppingListItem shoppingListItem) {
        this.shoppingListItemRepository.delete(shoppingListItem);
    }

    public ShoppingListItem setQuantity(final Long shoppingListId, final Long itemId, final Long quantity) {
        final ShoppingList shoppingList = this.shoppingListRepository.findOne(shoppingListId);
        if (shoppingList == null) {
            throw new ShoppingListNotFoundException(shoppingListId);
        }
        final Item item = this.itemRepository.findOne(itemId);
        if (item == null) {
            throw new ItemNotFoundException(itemId);
        }
        return setQuantity(shoppingList, item, quantity);
    }

    public ShoppingListItem setQuantity(final ShoppingList shoppingList, final Item item, final Long quantity) {
        return setQuantity(Optional.ofNullable(this.shoppingListItemRepository
                .findOneByShoppingListAndItem(shoppingList, item))
                .orElseThrow(() -> new ShoppingListItemNotFoundException(-1L)), quantity);
    }

    public ShoppingListItem setQuantity(final ShoppingListItem shoppingListItem, final Long quantity) {

        // Quantity must be > 0
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0!");
        }

        shoppingListItem.setQuantity(quantity);
        return this.shoppingListItemRepository.save(shoppingListItem);
    }

}
