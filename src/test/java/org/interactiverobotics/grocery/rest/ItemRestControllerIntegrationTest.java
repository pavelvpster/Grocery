/*
 * ItemRestControllerIntegrationTest.java
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
import org.interactiverobotics.grocery.form.ItemForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Item REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItemRestControllerIntegrationTest {

    private static final String ITEM_ENDPOINT = "/api/v1/item/";
    private static final String TEST_ITEM_NAME = "test-item";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ItemRepository itemRepository;


    @Before
    public void setUp() throws Exception {
        itemRepository.deleteAll();
    }


    @Test
    public void testGetItems() {

        final List<Item> existingItems = new ArrayList<>();
        itemRepository.saveAll(Arrays.asList(new Item("test-item-1"), new Item("test-item-2")))
                .forEach(item -> existingItems.add(item));

        final ResponseEntity<Item[]> response = restTemplate.getForEntity(ITEM_ENDPOINT, Item[].class);

        itemRepository.deleteAll(existingItems);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItems, Arrays.asList(response.getBody()));
    }

    @Test
    public void testGetItemsPage() {

        final List<Item> existingItems = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingItems.add(itemRepository.save(new Item("test-item-" + i)));
        }

        final ParameterizedTypeReference<PageResponse<Item>> responseType =
                new ParameterizedTypeReference<PageResponse<Item>>() {};
        final ResponseEntity<PageResponse<Item>> response = restTemplate.exchange(ITEM_ENDPOINT + "list?page=1&size=10",
                HttpMethod.GET, null, responseType);

        itemRepository.deleteAll(existingItems);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItems.size(), response.getBody().getTotalElements());
        assertEquals(10, response.getBody().getTotalPages());
        assertEquals(10, response.getBody().getSize());
    }

    @Test
    public void testGetItemById() {

        final Item existingItem = itemRepository.save(new Item(TEST_ITEM_NAME));

        final ResponseEntity<Item> response = restTemplate
                .getForEntity(ITEM_ENDPOINT + existingItem.getId(), Item.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItem, response.getBody());
    }

    @Test
    public void testGetNotExistingItemById() {

        final ResponseEntity<Item> response = restTemplate.getForEntity(ITEM_ENDPOINT + new Long(999L), Item.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetItemByName() {

        final Item existingItem = itemRepository.save(new Item(TEST_ITEM_NAME));

        final ResponseEntity<Item> response = restTemplate
                .getForEntity(ITEM_ENDPOINT + "search?name=" + existingItem.getName(), Item.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItem, response.getBody());
    }

    @Test
    public void testGetNotExistingItemByName() {

        final ResponseEntity<Item> response = restTemplate.getForEntity(ITEM_ENDPOINT + "search?name=test", Item.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @Test
    public void testCreateItem() {

        final ItemForm form = new ItemForm();
        form.setName("test-item");

        final ResponseEntity<Item> response = restTemplate.postForEntity(ITEM_ENDPOINT, form, Item.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        itemRepository.delete(response.getBody());

        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void testUpdateItem() {

        final Item existingItem = itemRepository.save(new Item(TEST_ITEM_NAME));

        final ItemForm form = new ItemForm();
        form.setName("updated-test-item");

        final ResponseEntity<Item> response = restTemplate
                .postForEntity(ITEM_ENDPOINT + existingItem.getId(), form, Item.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItem.getId(), response.getBody().getId());
        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void testUpdateNotExistingItem() {

        final ItemForm form = new ItemForm();
        form.setName("updated-test-item");

        final ResponseEntity<Item> response = restTemplate
                .postForEntity(ITEM_ENDPOINT + new Long(999L), form, Item.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeleteItem() {

        final Item existingItem = itemRepository.save(new Item(TEST_ITEM_NAME));

        restTemplate.delete(ITEM_ENDPOINT + existingItem.getId());

        assertFalse(itemRepository.findById(existingItem.getId()).isPresent());
    }

    @Test
    public void testDeleteNotExistingItem() {

        final ResponseEntity<?> response = restTemplate
                .exchange(ITEM_ENDPOINT + new Long(999L), HttpMethod.DELETE, null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
