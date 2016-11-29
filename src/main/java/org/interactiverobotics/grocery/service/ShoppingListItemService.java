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
import org.interactiverobotics.grocery.form.ShoppingListItemCreateForm;
import org.interactiverobotics.grocery.form.ShoppingListItemUpdateForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
@Service
public class ShoppingListItemService {

    private static final Logger LOG = LoggerFactory.getLogger(ShoppingListItemService.class);

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
        final List<ShoppingListItem> shoppingListItems = this.shoppingListItemRepository.findAllByShoppingList(
                Optional.ofNullable(this.shoppingListRepository.findOne(shoppingListId))
                        .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId)));
        LOG.debug("{} ShoppingListItem(s) found", shoppingListItems.size());
        return shoppingListItems;
    }

    /**
     * Returns page of ShoppingListItem(s).
     */
    public Page<ShoppingListItem> getShoppingListItems(Pageable pageable, final Long shoppingListId) {
        final Page<ShoppingListItem> shoppingListItems = this.shoppingListItemRepository.findAllByShoppingList(pageable,
                Optional.ofNullable(this.shoppingListRepository.findOne(shoppingListId))
                        .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId)));
        LOG.debug("{} ShoppingListItem(s) found for {}", shoppingListItems.getNumberOfElements(), pageable);
        return shoppingListItems;
    }

    /**
     * Returns ShoppingListItem by Id.
     */
    public ShoppingListItem getShoppingListItemById(final Long shoppingListItemId) {
        final ShoppingListItem shoppingListItem =
                Optional.ofNullable(this.shoppingListItemRepository.findOne(shoppingListItemId))
                        .orElseThrow(() -> new ShoppingListItemNotFoundException(shoppingListItemId));
        LOG.debug("ShoppingListItem found by Id #{}", shoppingListItemId);
        return shoppingListItem;
    }

    /**
     * Returns Item(s) not included to ShoppingList.
     */
    public List<Item> getNotAddedItems(final Long shoppingListId) {
        final ShoppingList shoppingList = Optional.ofNullable(this.shoppingListRepository.findOne(shoppingListId))
                .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId));
        return StreamSupport.stream(this.itemRepository.findAll().spliterator(), false)
                .filter(item ->
                        this.shoppingListItemRepository.findOneByShoppingListAndItem(shoppingList, item) == null)
                .collect(Collectors.toList());
    }

    /**
     * Creates ShoppingListItem.
     */
    public ShoppingListItem createShoppingListItem(final ShoppingListItemCreateForm form) {

        final ShoppingList shoppingList =
                Optional.ofNullable(this.shoppingListRepository.findOne(form.getShoppingList()))
                        .orElseThrow(() -> new ShoppingListNotFoundException(form.getShoppingList()));

        final Item item = Optional.ofNullable(this.itemRepository.findOne(form.getItem()))
                .orElseThrow(() -> new ItemNotFoundException(form.getItem()));

        // Quantity must be > 0
        final Long quantity = form.getQuantity();
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0!");
        }

        final ShoppingListItem shoppingListItem =
                this.shoppingListItemRepository.save(new ShoppingListItem(shoppingList, item, quantity));

        LOG.info("ShoppingListItem created: {}", shoppingListItem);
        return shoppingListItem;
    }

    /**
     * Updates ShoppingListItem.
     */
    public ShoppingListItem updateShoppingListItem(final Long shoppingListItemId,
                                                   final ShoppingListItemUpdateForm form) {

        final ShoppingListItem shoppingListItem =
                Optional.ofNullable(this.shoppingListItemRepository.findOne(shoppingListItemId))
                        .orElseThrow(() -> new ShoppingListItemNotFoundException(shoppingListItemId));

        // Quantity must be > 0
        final Long quantity = form.getQuantity();
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0!");
        }
        shoppingListItem.setQuantity(quantity);

        final ShoppingListItem updatedShoppingListItem = this.shoppingListItemRepository.save(shoppingListItem);

        LOG.info("ShoppingListItem updated: {}", updatedShoppingListItem);
        return updatedShoppingListItem;
    }

    /**
     * Deletes ShoppingListItem.
     */
    public void deleteShoppingListItem(final Long shoppingListItemId) {
        final ShoppingListItem shoppingListItem =
                Optional.ofNullable(this.shoppingListItemRepository.findOne(shoppingListItemId))
                        .orElseThrow(() -> new ShoppingListItemNotFoundException(shoppingListItemId));
        this.shoppingListItemRepository.delete(shoppingListItem);
        LOG.info("ShoppingListItem deleted: {}", shoppingListItem);
    }

}
