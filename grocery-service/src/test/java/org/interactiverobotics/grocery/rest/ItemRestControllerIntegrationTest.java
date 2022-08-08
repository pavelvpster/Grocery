/*
 * ItemRestControllerIntegrationTest.java
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
import org.interactiverobotics.grocery.form.ItemForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Item REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItemRestControllerIntegrationTest {

    private static final String ITEM_ENDPOINT = "/api/v1/item/";
    private static final String TEST_ITEM_NAME = "test-item";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ItemRepository itemRepository;


    @BeforeEach
    public void setUp() {
        itemRepository.deleteAll();
    }


    @Test
    public void getItems_returnsItems() {
        List<Item> existingItems = new ArrayList<>();
        itemRepository.saveAll(List.of(Item.builder().name("test-item-1").build(), Item.builder().name("test-item-2").build()))
                .forEach(item -> existingItems.add(item));

        ResponseEntity<Item[]> response = restTemplate.getForEntity(ITEM_ENDPOINT, Item[].class);

        itemRepository.deleteAll(existingItems);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItems, List.of(response.getBody()));
    }

    @Test
    public void getItemsPage_returnsPageOfItems() {
        List<Item> existingItems = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingItems.add(itemRepository.save(Item.builder().name("test-item-" + i).build()));
        }

        ParameterizedTypeReference<PageResponse<Item>> responseType =
                new ParameterizedTypeReference<>() {};
        ResponseEntity<PageResponse<Item>> response = restTemplate.exchange(ITEM_ENDPOINT + "list?page=1&size=10",
                HttpMethod.GET, null, responseType);

        itemRepository.deleteAll(existingItems);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItems.size(), response.getBody().getTotalElements());
        assertEquals(10, response.getBody().getTotalPages());
        assertEquals(10, response.getBody().getSize());
    }

    @Test
    public void getItemById_returnsItem() {
        Item existingItem = itemRepository.save(Item.builder().name(TEST_ITEM_NAME).build());

        ResponseEntity<Item> response = restTemplate
                .getForEntity(ITEM_ENDPOINT + existingItem.getId(), Item.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItem, response.getBody());
    }

    @Test
    public void getItemById_whenItemDoesNotExist_returnsError() {
        ResponseEntity<Item> response = restTemplate.getForEntity(ITEM_ENDPOINT + Long.valueOf(999L), Item.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void getItemByName_returnsItem() {
        Item existingItem = itemRepository.save(Item.builder().name(TEST_ITEM_NAME).build());

        ResponseEntity<Item> response = restTemplate
                .getForEntity(ITEM_ENDPOINT + "search?name=" + existingItem.getName(), Item.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItem, response.getBody());
    }

    @Test
    public void getItemByName_whenItemDoesNotExist_returnsError() {
        ResponseEntity<Item> response = restTemplate.getForEntity(ITEM_ENDPOINT + "search?name=test", Item.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void createItem_createsAndReturnsItem() {
        ItemForm form = ItemForm.builder().name("test-item").build();

        ResponseEntity<Item> response = restTemplate.postForEntity(ITEM_ENDPOINT, form, Item.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        itemRepository.delete(response.getBody());

        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void updateItem_updateaAndReturnsItem() {
        Item existingItem = itemRepository.save(Item.builder().name(TEST_ITEM_NAME).build());

        ItemForm form = ItemForm.builder().name("updated-test-item").build();

        ResponseEntity<Item> response = restTemplate
                .postForEntity(ITEM_ENDPOINT + existingItem.getId(), form, Item.class);

        itemRepository.delete(existingItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingItem.getId(), response.getBody().getId());
        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void updateItem_whenItemDoesNotExist_returnsError() {
        ItemForm form = ItemForm.builder().name("updated-test-item").build();

        final ResponseEntity<Item> response = restTemplate
                .postForEntity(ITEM_ENDPOINT + Long.valueOf(999L), form, Item.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void deleteItem_deletesItem() {
        Item existingItem = itemRepository.save(Item.builder().name(TEST_ITEM_NAME).build());

        restTemplate.delete(ITEM_ENDPOINT + existingItem.getId());

        assertFalse(itemRepository.findById(existingItem.getId()).isPresent());
    }

    @Test
    public void deleteItem_whenItemDoesNotExist_returnsError() {
        ResponseEntity<?> response = restTemplate
                .exchange(ITEM_ENDPOINT + Long.valueOf(999L), HttpMethod.DELETE, null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
