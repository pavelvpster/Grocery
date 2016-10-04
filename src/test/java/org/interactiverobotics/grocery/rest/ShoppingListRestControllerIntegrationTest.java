/*
 * ShoppingListRestControllerIntegrationTest.java
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

package org.interactiverobotics.grocery.rest;

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.domain.ShoppingListItem;
import org.interactiverobotics.grocery.form.ShoppingListForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * ShoppingList REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingListRestControllerIntegrationTest {

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
        shoppingListRepository.deleteAll();
    }


    @Test
    public void testGetShoppingLists() {

        final List<ShoppingList> existingShoppingLists = new ArrayList<>();
        shoppingListRepository.save(Arrays.asList(
                new ShoppingList("test-shopping-list-1"), new ShoppingList("test-shopping-list-2")))
                .forEach(shoppingList -> existingShoppingLists.add(shoppingList));

        final ResponseEntity<ShoppingList[]> response = restTemplate.getForEntity("/api/v1/shopping_list/",
                ShoppingList[].class);

        shoppingListRepository.delete(existingShoppingLists);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShoppingLists, Arrays.asList(response.getBody()));
    }

    @Test
    public void testGetShoppingListsPage() {

        final List<ShoppingList> existingShoppingLists = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShoppingLists.add(shoppingListRepository.save(new ShoppingList("test-shopping-list-" + i)));
        }

        final ParameterizedTypeReference<PageResponse<ShoppingList>> responseType =
                new ParameterizedTypeReference<PageResponse<ShoppingList>>() {};
        final ResponseEntity<PageResponse<ShoppingList>> response =
                restTemplate.exchange("/api/v1/shopping_list/list?page=1&size=10", HttpMethod.GET, null, responseType);

        shoppingListRepository.delete(existingShoppingLists);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShoppingLists.size(), response.getBody().getTotalElements());
        assertEquals(10, response.getBody().getTotalPages());
        assertEquals(10, response.getBody().getSize());
    }

    @Test
    public void testGetShoppingListById() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));

        final ResponseEntity<ShoppingList> response = restTemplate
                .getForEntity("/api/v1/shopping_list/" + existingShoppingList.getId(), ShoppingList.class);

        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShoppingList, response.getBody());
    }

    @Test
    public void testGetNotExistingShoppingListById() {

        final ResponseEntity<ShoppingList> response = restTemplate
                .getForEntity("/api/v1/shopping_list/" + new Long(999L), ShoppingList.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetShoppingListByName() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));

        final ResponseEntity<ShoppingList> response = restTemplate
                .getForEntity("/api/v1/shopping_list/search?name=" + existingShoppingList.getName(),
                        ShoppingList.class);

        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShoppingList, response.getBody());
    }

    @Test
    public void testGetNotExistingShoppingListByName() {

        final ResponseEntity<ShoppingList> response = restTemplate
                .getForEntity("/api/v1/shopping_list/search?name=test", ShoppingList.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testCreateShoppingList() {

        final ShoppingListForm form = new ShoppingListForm();
        form.setName("test-shopping-list");

        final ResponseEntity<ShoppingList> response = restTemplate
                .postForEntity("/api/v1/shopping_list/", form, ShoppingList.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        shoppingListRepository.delete(response.getBody());

        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void testUpdateShoppingList() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));

        final ShoppingListForm form = new ShoppingListForm();
        form.setName("updated-test-shopping-list");

        final ResponseEntity<ShoppingList> response = restTemplate
                .postForEntity("/api/v1/shopping_list/" + existingShoppingList.getId(), form, ShoppingList.class);

        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShoppingList.getId(), response.getBody().getId());
        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void testUpdateNotExistingShoppingList() {

        final ShoppingListForm form = new ShoppingListForm();
        form.setName("updated-test-shopping-list");

        final ResponseEntity<ShoppingList> response = restTemplate
                .postForEntity("/api/v1/shopping_list/" + new Long(999L), form, ShoppingList.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeleteShoppingList() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));

        restTemplate.delete("/api/v1/shopping_list/" + existingShoppingList.getId());

        assertNull(shoppingListRepository.findOne(existingShoppingList.getId()));
    }

    @Test
    public void testDeleteNotExistingShoppingList() {

        final ResponseEntity<?> response = restTemplate
                .exchange("/api/v1/shopping_list/" + new Long(999L), HttpMethod.DELETE, null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testAddItem() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));
        final Item existingItem = itemRepository.save(new Item("test-item"));

        final ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity("/api/v1/shopping_list/"
                + existingShoppingList.getId() + "/add/" + existingItem.getId() + "?quantity=1", null,
                ShoppingListItem.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        shoppingListItemRepository.delete(response.getBody());
        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);

        assertEquals(existingShoppingList, response.getBody().getShoppingList());
        assertEquals(existingItem, response.getBody().getItem());
        assertEquals(new Long(1L), response.getBody().getQuantity());
    }

    @Test
    public void testAddItemForWrongShoppingListId() {

        final Item existingItem = itemRepository.save(new Item("test-item"));

        final ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity("/api/v1/shopping_list/"
                + new Long(999L) + "/add/" + existingItem.getId() + "?quantity=1", null,
                ShoppingListItem.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testAddItemForWrongItemId() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));

        final ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity("/api/v1/shopping_list/"
                + existingShoppingList.getId() + "/add/" + new Long(999L) + "?quantity=1", null,
                ShoppingListItem.class);

        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testAddItemForWrongQuantity() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));
        final Item existingItem = itemRepository.save(new Item("test-item"));

        final ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity("/api/v1/shopping_list/"
                + existingShoppingList.getId() + "/add/" + existingItem.getId() + "?quantity=0", null,
                ShoppingListItem.class);

        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testRemoveItem() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));
        final Item existingItem = itemRepository.save(new Item("test-item"));
        final ShoppingListItem existingShoppingListItem = shoppingListItemRepository
                .save(new ShoppingListItem(existingShoppingList, existingItem, 1L));

        final ResponseEntity<?> response = restTemplate.postForEntity("/api/v1/shopping_list/"
                        + existingShoppingList.getId() + "/remove/" + existingItem.getId(), null, Object.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(shoppingListItemRepository.findOne(existingShoppingListItem.getId()));

        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);
    }

    @Test
    public void testRemoveItemForWrongShoppingListId() {

        final Item existingItem = itemRepository.save(new Item("test-item"));

        final ResponseEntity<?> response = restTemplate.postForEntity("/api/v1/shopping_list/"
                        + new Long(999L) + "/remove/" + existingItem.getId(), null, Object.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testRemoveItemForWrongItemId() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));

        final ResponseEntity<?> response = restTemplate.postForEntity("/api/v1/shopping_list/"
                        + existingShoppingList.getId() + "/remove/" + new Long(999L), null, Object.class);

        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testSetItemQuantity() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));
        final Item existingItem = itemRepository.save(new Item("test-item"));
        final ShoppingListItem existingShoppingListItem = shoppingListItemRepository
                .save(new ShoppingListItem(existingShoppingList, existingItem, 1L));

        final ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity("/api/v1/shopping_list/"
                + existingShoppingList.getId() + "/" + existingItem.getId() + "?quantity=2", null,
                ShoppingListItem.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        shoppingListItemRepository.delete(existingShoppingListItem);
        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);

        assertEquals(existingShoppingList, response.getBody().getShoppingList());
        assertEquals(existingItem, response.getBody().getItem());
        assertEquals(new Long(2L), response.getBody().getQuantity());
    }

    @Test
    public void testSetItemQuantityForWrongShoppingListId() {

        final Item existingItem = itemRepository.save(new Item("test-item"));

        final ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity("/api/v1/shopping_list/"
                        + new Long(999L) + "/" + existingItem.getId() + "?quantity=2", null, ShoppingListItem.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testSetItemQuantityForWrongItemId() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));

        final ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity("/api/v1/shopping_list/"
                + existingShoppingList.getId() + "/" + new Long(999L) + "?quantity=2", null,
                ShoppingListItem.class);

        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testSetItemQuantityForWrongQuantity() {

        final ShoppingList existingShoppingList = shoppingListRepository.save(new ShoppingList("test-shopping-list"));
        final Item existingItem = itemRepository.save(new Item("test-item"));
        final ShoppingListItem existingShoppingListItem = shoppingListItemRepository
                .save(new ShoppingListItem(existingShoppingList, existingItem, 1L));

        final ResponseEntity<ShoppingListItem> response = restTemplate.postForEntity("/api/v1/shopping_list/"
                + existingShoppingList.getId() + "/" + existingItem.getId() + "?quantity=0", null,
                ShoppingListItem.class);

        shoppingListItemRepository.delete(existingShoppingListItem);
        itemRepository.delete(existingItem);
        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
