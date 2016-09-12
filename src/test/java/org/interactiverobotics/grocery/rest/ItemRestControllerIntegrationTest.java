/*
 * ItemRestControllerIntegrationTest.java
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
import org.interactiverobotics.grocery.form.ItemForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Item REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItemRestControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ItemRepository itemRepository;


    @Test
    public void testGetItems() {

        final List<Item> existingItems = new ArrayList<>();
        itemRepository.save(Arrays.asList(new Item("test-item-1"), new Item("test-item-2")))
                .forEach(item -> existingItems.add(item));

        final ResponseEntity<Item[]> response = restTemplate.getForEntity("/api/v1/item/", Item[].class);

        itemRepository.delete(existingItems);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItems, Arrays.asList(response.getBody()));
    }

    @Test
    public void testGetItemById() {

        final Item existingItem = itemRepository.save(new Item("test-item"));

        final ResponseEntity<Item> response = restTemplate
                .getForEntity("/api/v1/item/" + existingItem.getId(), Item.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItem, response.getBody());
    }

    @Test
    public void testGetNotExistingItemById() {

        final ResponseEntity<Item> response = restTemplate.getForEntity("/api/v1/item/" + new Long(999L), Item.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetItemByName() {

        final Item existingItem = itemRepository.save(new Item("test-item"));

        final ResponseEntity<Item> response = restTemplate
                .getForEntity("/api/v1/item/search?name=" + existingItem.getName(), Item.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItem, response.getBody());
    }

    @Test
    public void testGetNotExistingItemByName() {

        final ResponseEntity<Item> response = restTemplate.getForEntity("/api/v1/item/search?name=test", Item.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testCreateItem() {

        final ItemForm form = new ItemForm();
        form.setName("test-item");

        final ResponseEntity<Item> response = restTemplate.postForEntity("/api/v1/item/", form, Item.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        itemRepository.delete(response.getBody());

        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void testUpdateItem() {

        final Item existingItem = itemRepository.save(new Item("test-item"));

        final ItemForm form = new ItemForm();
        form.setName("updated-test-item");

        final ResponseEntity<Item> response = restTemplate
                .postForEntity("/api/v1/item/" + existingItem.getId(), form, Item.class);

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
                .postForEntity("/api/v1/item/" + new Long(999L), form, Item.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeleteItem() {

        final Item existingItem = itemRepository.save(new Item("test-item"));

        restTemplate.delete("/api/v1/item/" + existingItem.getId());

        assertNull(itemRepository.findOne(existingItem.getId()));
    }

    @Test
    public void testDeleteNotExistingItem() {

        final ResponseEntity<?> response = restTemplate
                .exchange("/api/v1/item/" + new Long(999L), HttpMethod.DELETE, null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
