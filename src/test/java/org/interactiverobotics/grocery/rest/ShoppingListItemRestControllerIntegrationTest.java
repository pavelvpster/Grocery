/*
 * ShoppingListItemRestControllerIntegrationTest.java
 *
 * Copyright (C) 2016-2018 Pavel Prokhorov (pavelvpster@gmail.com)
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.domain.ShoppingListItem;
import org.interactiverobotics.grocery.form.ShoppingListItemCreateForm;
import org.interactiverobotics.grocery.form.ShoppingListItemUpdateForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ShoppingListItem REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@RunWith(SpringRunner.class)
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


    @Before
    public void setUp() throws Exception {
        shoppingListItemRepository.deleteAll();
    }


    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @Test
    public void testCreateShoppingListItem() {

        final ShoppingList existingShoppingList = shoppingListRepository
                .save(new ShoppingList(TEST_SHOPPING_LIST_NAME));
        final Item existingItem = itemRepository.save(new Item(TEST_ITEM_NAME));

        final ShoppingListItemCreateForm form =
                new ShoppingListItemCreateForm(existingShoppingList.getId(), existingItem.getId(), 1L);

        final ResponseEntity<ShoppingListItem> response = restTemplate
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
    public void testCreateShoppingListItemForWrongShoppingListId() {

        final Item existingItem = itemRepository.save(new Item(TEST_ITEM_NAME));

        final ShoppingListItemCreateForm form =
                new ShoppingListItemCreateForm(999L, existingItem.getId(), 1L);

        final ResponseEntity<ShoppingListItem> response = restTemplate
                .postForEntity(SHOPPING_LIST_ITEM_ENDPOINT, form, ShoppingListItem.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testCreateShoppingListItemForWrongItemId() {

        final ShoppingList existingShoppingList = shoppingListRepository
                .save(new ShoppingList(TEST_SHOPPING_LIST_NAME));

        final ShoppingListItemCreateForm form =
                new ShoppingListItemCreateForm(existingShoppingList.getId(), 999L, 1L);

        final ResponseEntity<ShoppingListItem> response = restTemplate
                .postForEntity(SHOPPING_LIST_ITEM_ENDPOINT, form, ShoppingListItem.class);

        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testCreateShoppingListItemForWrongQuantity() {

        final ShoppingList existingShoppingList = shoppingListRepository
                .save(new ShoppingList(TEST_SHOPPING_LIST_NAME));
        final Item existingItem = itemRepository.save(new Item(TEST_ITEM_NAME));

        final ShoppingListItemCreateForm form =
                new ShoppingListItemCreateForm(existingShoppingList.getId(), existingItem.getId(), 0L);

        final ResponseEntity<ShoppingListItem> response = restTemplate
                .postForEntity(SHOPPING_LIST_ITEM_ENDPOINT, form, ShoppingListItem.class);

        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testUpdateShoppingListItem() {

        final ShoppingList existingShoppingList = shoppingListRepository
                .save(new ShoppingList(TEST_SHOPPING_LIST_NAME));
        final Item existingItem = itemRepository.save(new Item(TEST_ITEM_NAME));
        final ShoppingListItem existingShoppingListItem = shoppingListItemRepository
                .save(new ShoppingListItem(existingShoppingList, existingItem, 1L));

        final ShoppingListItemUpdateForm form = new ShoppingListItemUpdateForm(2L);

        final ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity(SHOPPING_LIST_ITEM_ENDPOINT
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
    public void testUpdateShoppingListItemForWrongShoppingListItemId() {

        final ShoppingListItemUpdateForm form = new ShoppingListItemUpdateForm(2L);

        final ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity(SHOPPING_LIST_ITEM_ENDPOINT
                + new Long(999L), form, ShoppingListItem.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testUpdateShoppingListItemForWrongQuantity() {

        final ShoppingList existingShoppingList = shoppingListRepository
                .save(new ShoppingList(TEST_SHOPPING_LIST_NAME));
        final Item existingItem = itemRepository.save(new Item(TEST_ITEM_NAME));
        final ShoppingListItem existingShoppingListItem = shoppingListItemRepository
                .save(new ShoppingListItem(existingShoppingList, existingItem, 1L));

        final ShoppingListItemUpdateForm form = new ShoppingListItemUpdateForm(0L);

        final ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity(SHOPPING_LIST_ITEM_ENDPOINT
                        + existingShoppingListItem.getId(), form, ShoppingListItem.class);

        shoppingListItemRepository.delete(existingShoppingListItem);
        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeleteShoppingListItem() {

        final ShoppingList existingShoppingList = shoppingListRepository
                .save(new ShoppingList(TEST_SHOPPING_LIST_NAME));
        final Item existingItem = itemRepository.save(new Item(TEST_ITEM_NAME));
        final ShoppingListItem existingShoppingListItem = shoppingListItemRepository
                .save(new ShoppingListItem(existingShoppingList, existingItem, 1L));

        restTemplate.delete(SHOPPING_LIST_ITEM_ENDPOINT + existingShoppingListItem.getId());

        assertFalse(shoppingListItemRepository.findById(existingShoppingListItem.getId()).isPresent());

        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);
    }

    @Test
    public void testDeleteShoppingListItemForWrongShoppingListItemId() {

        final ResponseEntity<?> response = restTemplate
                .exchange(SHOPPING_LIST_ITEM_ENDPOINT + new Long(999L),
                        HttpMethod.DELETE, null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
