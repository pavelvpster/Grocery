/*
 * ItemService.java
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
import org.interactiverobotics.grocery.exception.ItemNotFoundException;
import org.interactiverobotics.grocery.form.ItemForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Item service.
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(final ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * Returns Item(s).
     */
    public List<Item> getItems() {
        final List<Item> items = new ArrayList<>();
        this.itemRepository.findAll().forEach(item -> items.add(item));
        return items;
    }

    /**
     * Returns Item by Id.
     */
    public Item getItemById(final Long itemId) {
        return Optional.ofNullable(this.itemRepository.findOne(itemId))
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    /**
     * Returns Item by Name.
     */
    public Item getItemByName(final String name) {
        return Optional.ofNullable(this.itemRepository.findOneByName(name))
                .orElseThrow(() -> new ItemNotFoundException(-1L));
    }

    /**
     * Creates Item.
     */
    public Item createItem(final ItemForm form) {
        return this.itemRepository.save(new Item(form.getName()));
    }

    /**
     * Updates Item.
     */
    public Item updateItem(final Long itemId, final ItemForm form) {
        final Item item = Optional.ofNullable(this.itemRepository.findOne(itemId))
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        item.setName(form.getName());
        return this.itemRepository.save(item);
    }

    /**
     * Deletes Item.
     */
    public void deleteItem(final Long itemId) {
        final Item item = Optional.ofNullable(this.itemRepository.findOne(itemId))
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        this.itemRepository.delete(item);
    }

}
