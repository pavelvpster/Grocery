/*
 * ShoppingListService.java
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
import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.form.ShoppingListForm;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ShoppingList service.
 */
@AllArgsConstructor
@Service
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;

    /**
     * Returns ShoppingList(s).
     */
    public List<ShoppingList> getShoppingLists() {
        List<ShoppingList> shoppingLists = new ArrayList<>();
        shoppingListRepository.findAll().forEach(shoppingList -> shoppingLists.add(shoppingList));
        return shoppingLists;
    }

    /**
     * Returns page of ShoppingList(s).
     */
    public Page<ShoppingList> getShoppingLists(Pageable pageable) {
        return shoppingListRepository.findAll(pageable);
    }

    /**
     * Returns ShoppingList by Id.
     */
    public ShoppingList getShoppingListById(Long shoppingListId) {
        return shoppingListRepository.findById(shoppingListId)
                .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId));
    }

    /**
     * Returns ShoppingList by Name.
     */
    public ShoppingList getShoppingListByName(String name) {
        return Optional.ofNullable(shoppingListRepository.findOneByName(name))
                .orElseThrow(() -> new ShoppingListNotFoundException(-1L));
    }

    /**
     * Creates ShoppingList.
     */
    public ShoppingList createShoppingList(ShoppingListForm form) {
        return shoppingListRepository.save(ShoppingList.builder().name(form.getName()).build());
    }

    /**
     * Updates ShoppingList.
     */
    public ShoppingList updateShoppingList(Long shoppingListId, ShoppingListForm form) {
        ShoppingList shoppingList = shoppingListRepository.findById(shoppingListId)
                .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId));
        shoppingList.setName(form.getName());
        return shoppingListRepository.save(shoppingList);
    }

    /**
     * Deletes ShoppingList.
     */
    public void deleteShoppingList(Long itemId) {
        ShoppingList shoppingList = shoppingListRepository.findById(itemId)
                .orElseThrow(() -> new ShoppingListNotFoundException(itemId));
        shoppingListRepository.delete(shoppingList);
    }
}
