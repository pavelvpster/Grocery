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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * ShoppingListItem service.
 * todo: refactor API: use ShoppingListItem Id instead of ShoppingList Id and Item Id where possible
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

    /**
     * Returns ShoppingListItem(s).
     */
    public List<ShoppingListItem> getShoppingListItems(final Long shoppingListId) {
        return getShoppingListItems(Optional.ofNullable(this.shoppingListRepository.findOne(shoppingListId))
                .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId)));
    }

    /**
     * Returns ShoppingListItem(s).
     */
    public List<ShoppingListItem> getShoppingListItems(final ShoppingList shoppingList) {
        return this.shoppingListItemRepository.findAllByShoppingList(shoppingList);
    }

    /**
     * Returns page of ShoppingListItem(s).
     */
    public Page<ShoppingListItem> getShoppingListItems(Pageable pageable, final Long shoppingListId) {
        return getShoppingListItems(pageable, Optional.ofNullable(this.shoppingListRepository.findOne(shoppingListId))
                .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId)));
    }

    /**
     * Returns page of ShoppingListItem(s).
     */
    public Page<ShoppingListItem> getShoppingListItems(Pageable pageable, final ShoppingList shoppingList) {
        return this.shoppingListItemRepository.findAllByShoppingList(pageable, shoppingList);
    }

    /**
     * Returns ShoppingListItem by Id.
     */
    public ShoppingListItem getShoppingListItemById(final Long shoppingListItemId) {
        return Optional.ofNullable(this.shoppingListItemRepository.findOne(shoppingListItemId))
                .orElseThrow(() -> new ShoppingListItemNotFoundException(shoppingListItemId));
    }

    /**
     * Returns Item(s) not existing in ShoppingList's Item(s).
     */
    public List<Item> getNotAddedItems(final Long shoppingListId) {
        return getNotAddedItems(Optional.ofNullable(this.shoppingListRepository.findOne(shoppingListId))
                .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId)));
    }

    /**
     * Returns Item(s) not existing in ShoppingList's Item(s).
     */
    public List<Item> getNotAddedItems(final ShoppingList shoppingList) {
        return StreamSupport.stream(this.itemRepository.findAll().spliterator(), false)
                .filter(item ->
                        this.shoppingListItemRepository.findOneByShoppingListAndItem(shoppingList, item) == null)
                .collect(Collectors.toList());
    }

    /**
     * Add Item to ShoppingList.
     */
    public ShoppingListItem addItem(final Long shoppingListId, final Long itemId, final Long quantity) {
        return addItem(Optional.ofNullable(this.shoppingListRepository.findOne(shoppingListId))
                .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId)),
                        Optional.ofNullable(this.itemRepository.findOne(itemId))
                                .orElseThrow(() -> new ItemNotFoundException(itemId)), quantity);
    }

    /**
     * Add Item to ShoppingList.
     */
    public ShoppingListItem addItem(final ShoppingList shoppingList, final Item item, final Long quantity) {

        // Quantity must be > 0
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0!");
        }

        return this.shoppingListItemRepository.save(new ShoppingListItem(shoppingList, item, quantity));
    }

    /**
     * Deletes Item from ShoppingList.
     */
    public void deleteItem(final Long shoppingListId, final Long itemId) {
        deleteItem(Optional.ofNullable(this.shoppingListRepository.findOne(shoppingListId))
                .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId)),
                        Optional.ofNullable(this.itemRepository.findOne(itemId))
                                .orElseThrow(() -> new ItemNotFoundException(itemId)));
    }

    /**
     * Deletes Item from ShoppingList.
     */
    public ShoppingListItem deleteItem(final ShoppingList shoppingList, final Item item) {
        return deleteItem(Optional.ofNullable(this.shoppingListItemRepository
                .findOneByShoppingListAndItem(shoppingList, item))
                .orElseThrow(() -> new ShoppingListItemNotFoundException(-1L)));
    }

    /**
     * Deletes Item from ShoppingList.
     */
    public ShoppingListItem deleteItem(final Long shoppingListItemId) {
        return deleteItem(Optional.ofNullable(this.shoppingListItemRepository.findOne(shoppingListItemId))
                .orElseThrow(() -> new ShoppingListItemNotFoundException(shoppingListItemId)));
    }

    /**
     * Deletes Item from ShoppingList.
     */
    public ShoppingListItem deleteItem(final ShoppingListItem shoppingListItem) {
        this.shoppingListItemRepository.delete(shoppingListItem);
        return shoppingListItem;
    }

    /**
     * Updates Item's (assigned to ShoppingList) Quantity.
     */
    public ShoppingListItem setQuantity(final Long shoppingListId, final Long itemId, final Long quantity) {
        return setQuantity(Optional.ofNullable(this.shoppingListRepository.findOne(shoppingListId))
                .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId)),
                        Optional.ofNullable(this.itemRepository.findOne(itemId))
                                .orElseThrow(() -> new ItemNotFoundException(itemId)), quantity);
    }

    /**
     * Updates Item's (assigned to ShoppingList) Quantity.
     */
    public ShoppingListItem setQuantity(final ShoppingList shoppingList, final Item item, final Long quantity) {
        return setQuantity(Optional.ofNullable(this.shoppingListItemRepository
                .findOneByShoppingListAndItem(shoppingList, item))
                .orElseThrow(() -> new ShoppingListItemNotFoundException(-1L)), quantity);
    }

    /**
     * Updates Item's (assigned to ShoppingList) Quantity.
     */
    public ShoppingListItem setQuantity(final Long shoppingListItemId, final Long quantity) {
        return setQuantity(Optional.ofNullable(this.shoppingListItemRepository.findOne(shoppingListItemId))
                .orElseThrow(() -> new ShoppingListItemNotFoundException(shoppingListItemId)), quantity);
    }

    /**
     * Updates Item's (assigned to ShoppingList) Quantity.
     */
    public ShoppingListItem setQuantity(final ShoppingListItem shoppingListItem, final Long quantity) {

        // Quantity must be > 0
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0!");
        }

        shoppingListItem.setQuantity(quantity);
        return this.shoppingListItemRepository.save(shoppingListItem);
    }

}
