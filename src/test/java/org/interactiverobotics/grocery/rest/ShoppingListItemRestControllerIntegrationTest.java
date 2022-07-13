/*
 * ShoppingListItemRestControllerIntegrationTest.java
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

package org.interactiverobotics.grocery.rest;

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.domain.ShoppingListItem;
import org.interactiverobotics.grocery.form.ShoppingListItemCreateForm;
import org.interactiverobotics.grocery.form.ShoppingListItemUpdateForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ShoppingListItem REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingListItemRestControllerIntegrationTest {

    private static final String SHOPPING_LIST_ITEM_ENDPOINT = "/api/v1/shopping_list_item/";
    private static final String TEST_SHOPPING_LIST_NAME = "test-shopping-list";
    private static final String TEST_ITEM_NAME = "test-item";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ShoppingListItemRepository shoppingListItemRepository;


    @BeforeEach
    public void setUp() {
        shoppingListItemRepository.deleteAll();
    }


    @Test
    public void createShoppingListItem_createsAndReturnsShoppingListItem() {
        ShoppingList existingShoppingList = shoppingListRepository
                .save(ShoppingList.builder().name(TEST_SHOPPING_LIST_NAME).build());
        Item existingItem = itemRepository.save(Item.builder().name(TEST_ITEM_NAME).build());

        ShoppingListItemCreateForm form =
                new ShoppingListItemCreateForm(existingShoppingList.getId(), existingItem.getId(), 1L);

        ResponseEntity<ShoppingListItem> response = restTemplate
                .postForEntity(SHOPPING_LIST_ITEM_ENDPOINT, form, ShoppingListItem.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        shoppingListItemRepository.delete(response.getBody());
        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);

        assertEquals(existingShoppingList, response.getBody().getShoppingList());
        assertEquals(existingItem, response.getBody().getItem());
        assertEquals(Long.valueOf(1L), response.getBody().getQuantity());
    }

    @Test
    public void createShoppingListItem_whenShoppingListDoesNotExist_returnsError() {
        Item existingItem = itemRepository.save(Item.builder().name(TEST_ITEM_NAME).build());

        ShoppingListItemCreateForm form =
                new ShoppingListItemCreateForm(999L, existingItem.getId(), 1L);

        ResponseEntity<ShoppingListItem> response = restTemplate
                .postForEntity(SHOPPING_LIST_ITEM_ENDPOINT, form, ShoppingListItem.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void createShoppingListItem_whenItemDoesNotExist_returnsError() {
        ShoppingList existingShoppingList = shoppingListRepository
                .save(ShoppingList.builder().name(TEST_SHOPPING_LIST_NAME).build());

        ShoppingListItemCreateForm form =
                new ShoppingListItemCreateForm(existingShoppingList.getId(), 999L, 1L);

        ResponseEntity<ShoppingListItem> response = restTemplate
                .postForEntity(SHOPPING_LIST_ITEM_ENDPOINT, form, ShoppingListItem.class);

        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void createShoppingListItem_whenQuantityLessOrEqualToZero_returnsError() {
        ShoppingList existingShoppingList = shoppingListRepository
                .save(ShoppingList.builder().name(TEST_SHOPPING_LIST_NAME).build());
        Item existingItem = itemRepository.save(Item.builder().name(TEST_ITEM_NAME).build());

        ShoppingListItemCreateForm form =
                new ShoppingListItemCreateForm(existingShoppingList.getId(), existingItem.getId(), 0L);

        ResponseEntity<ShoppingListItem> response = restTemplate
                .postForEntity(SHOPPING_LIST_ITEM_ENDPOINT, form, ShoppingListItem.class);

        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void updateShoppingListItem_updatesAndReturnsShoppingListItem() {
        ShoppingList existingShoppingList = shoppingListRepository
                .save(ShoppingList.builder().name(TEST_SHOPPING_LIST_NAME).build());
        Item existingItem = itemRepository.save(Item.builder().name(TEST_ITEM_NAME).build());
        ShoppingListItem existingShoppingListItem = shoppingListItemRepository
                .save(ShoppingListItem.builder().shoppingList(existingShoppingList).item(existingItem).quantity(1L).build());

        ShoppingListItemUpdateForm form = new ShoppingListItemUpdateForm(2L);

        ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity(SHOPPING_LIST_ITEM_ENDPOINT
                        + existingShoppingListItem.getId(), form, ShoppingListItem.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        shoppingListItemRepository.delete(existingShoppingListItem);
        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);

        assertEquals(existingShoppingList, response.getBody().getShoppingList());
        assertEquals(existingItem, response.getBody().getItem());
        assertEquals(form.getQuantity(), response.getBody().getQuantity());
    }

    @Test
    public void updateShoppingListItem_whenShoppingListItemDoesNotExist_returnsError() {
        ShoppingListItemUpdateForm form = new ShoppingListItemUpdateForm(2L);

        ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity(SHOPPING_LIST_ITEM_ENDPOINT
                + Long.valueOf(999L), form, ShoppingListItem.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void updateShoppingListItem_whenQuantityLessOrEqualToZero_returnsError() {
        ShoppingList existingShoppingList = shoppingListRepository
                .save(ShoppingList.builder().name(TEST_SHOPPING_LIST_NAME).build());
        Item existingItem = itemRepository.save(Item.builder().name(TEST_ITEM_NAME).build());
        ShoppingListItem existingShoppingListItem = shoppingListItemRepository
                .save(ShoppingListItem.builder().shoppingList(existingShoppingList).item(existingItem).quantity(1L).build());

        ShoppingListItemUpdateForm form = new ShoppingListItemUpdateForm(0L);

        ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity(SHOPPING_LIST_ITEM_ENDPOINT
                        + existingShoppingListItem.getId(), form, ShoppingListItem.class);

        shoppingListItemRepository.delete(existingShoppingListItem);
        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void deleteShoppingListItem_deletesShoppingListItem() {
        ShoppingList existingShoppingList = shoppingListRepository
                .save(ShoppingList.builder().name(TEST_SHOPPING_LIST_NAME).build());
        Item existingItem = itemRepository.save(Item.builder().name(TEST_ITEM_NAME).build());
        ShoppingListItem existingShoppingListItem = shoppingListItemRepository
                .save(ShoppingListItem.builder().shoppingList(existingShoppingList).item(existingItem).quantity(1L).build());

        restTemplate.delete(SHOPPING_LIST_ITEM_ENDPOINT + existingShoppingListItem.getId());

        assertFalse(shoppingListItemRepository.findById(existingShoppingListItem.getId()).isPresent());

        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);
    }

    @Test
    public void deleteShoppingListItem_whenShoppingListItemDoesNotExist_returnsError() {
        ResponseEntity<?> response = restTemplate
                .exchange(SHOPPING_LIST_ITEM_ENDPOINT + Long.valueOf(999L),
                        HttpMethod.DELETE, null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
