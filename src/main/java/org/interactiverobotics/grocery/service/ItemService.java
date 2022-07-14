/*
 * ItemService.java
 *
 * Copyright (C) 2016-2022 Pavel Prokhorov (pavelvpster@gmail.com)
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

import lombok.AllArgsConstructor;
import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.exception.ItemNotFoundException;
import org.interactiverobotics.grocery.form.ItemForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Item service.
 */
@AllArgsConstructor
@Service
public class ItemService {

    private static final Logger LOG = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;

    /**
     * Returns Item(s).
     */
    public List<Item> getItems() {
        List<Item> items = new ArrayList<>();
        itemRepository.findAll().forEach(item -> items.add(item));
        LOG.debug("{} Item(s) found", items.size());
        return items;
    }

    /**
     * Returns page of Item(s).
     */
    public Page<Item> getItems(Pageable pageable) {
        Page<Item> items = itemRepository.findAll(pageable);
        LOG.debug("{} Item(s) found for {}", items.getNumberOfElements(), pageable);
        return items;
    }

    /**
     * Returns Item by Id.
     */
    public Item getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        LOG.debug("Item found by Id #{}", itemId);
        return item;
    }

    /**
     * Returns Item by Name.
     */
    public Item getItemByName(String name) {
        Item item = Optional.ofNullable(itemRepository.findOneByName(name))
                .orElseThrow(() -> new ItemNotFoundException(-1L));
        LOG.debug("Item found by Name '{}'", name);
        return item;
    }

    /**
     * Creates Item.
     */
    public Item createItem(ItemForm form) {
        Item item = itemRepository.save(Item.builder().name(form.getName()).build());
        LOG.info("Item created: {}", item);
        return item;
    }

    /**
     * Updates Item.
     */
    public Item updateItem(Long itemId, ItemForm form) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        item.setName(form.getName());
        Item updatedItem = itemRepository.save(item);
        LOG.info("Item updated: {}", updatedItem);
        return updatedItem;
    }

    /**
     * Deletes Item.
     */
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        itemRepository.delete(item);
        LOG.info("Item deleted: {}", item);
    }
}
