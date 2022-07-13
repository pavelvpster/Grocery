/*
 * ShoppingListRestControllerIntegrationTest.java
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

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.form.ShoppingListForm;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
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
 * ShoppingList REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingListRestControllerIntegrationTest {

    private static final String SHOPPING_LIST_ENDPOINT = "/api/v1/shopping_list/";
    private static final String TEST_SHOPPING_LIST_NAME = "test-shopping-list";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShoppingListRepository shoppingListRepository;


    @BeforeEach
    public void setUp() {
        shoppingListRepository.deleteAll();
    }


    @Test
    public void getShoppingLists_returnsShoppingLists() {
        List<ShoppingList> existingShoppingLists = new ArrayList<>();
        shoppingListRepository.saveAll(List.of(
                ShoppingList.builder().name("test-shopping-list-1").build(), ShoppingList.builder().name("test-shopping-list-2").build()))
                .forEach(shoppingList -> existingShoppingLists.add(shoppingList));

        ResponseEntity<ShoppingList[]> response = restTemplate.getForEntity(SHOPPING_LIST_ENDPOINT,
                ShoppingList[].class);

        shoppingListRepository.deleteAll(existingShoppingLists);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShoppingLists, List.of(response.getBody()));
    }

    @Test
    public void getShoppingListsPage_returnsPageOfShoppingLists() {
        List<ShoppingList> existingShoppingLists = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShoppingLists.add(shoppingListRepository.save(ShoppingList.builder().name("test-shopping-list-" + i).build()));
        }

        ParameterizedTypeReference<PageResponse<ShoppingList>> responseType =
                new ParameterizedTypeReference<>() {};
        ResponseEntity<PageResponse<ShoppingList>> response =
                restTemplate.exchange(SHOPPING_LIST_ENDPOINT + "list?page=1&size=10",
                        HttpMethod.GET, null, responseType);

        shoppingListRepository.deleteAll(existingShoppingLists);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShoppingLists.size(), response.getBody().getTotalElements());
        assertEquals(10, response.getBody().getTotalPages());
        assertEquals(10, response.getBody().getSize());
    }

    @Test
    public void getShoppingListById_returnsShoppingList() {
        ShoppingList existingShoppingList = shoppingListRepository
                .save(ShoppingList.builder().name(TEST_SHOPPING_LIST_NAME).build());

        ResponseEntity<ShoppingList> response = restTemplate
                .getForEntity(SHOPPING_LIST_ENDPOINT + existingShoppingList.getId(), ShoppingList.class);

        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShoppingList, response.getBody());
    }

    @Test
    public void getShoppingListById_whenShoppingListDoesNotExist_returnsError() {
        ResponseEntity<ShoppingList> response = restTemplate
                .getForEntity(SHOPPING_LIST_ENDPOINT + Long.valueOf(999L), ShoppingList.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void getShoppingListByName_returnsShoppingList() {
        ShoppingList existingShoppingList = shoppingListRepository
                .save(ShoppingList.builder().name(TEST_SHOPPING_LIST_NAME).build());

        ResponseEntity<ShoppingList> response = restTemplate
                .getForEntity(SHOPPING_LIST_ENDPOINT + "search?name=" + existingShoppingList.getName(),
                        ShoppingList.class);

        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShoppingList, response.getBody());
    }

    @Test
    public void getShoppingListByName_whenShoppingListDoesNotExist_returnsError() {
        ResponseEntity<ShoppingList> response = restTemplate
                .getForEntity(SHOPPING_LIST_ENDPOINT + "search?name=test", ShoppingList.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void createShoppingList_createsAndReturnsShoppingList() {
        ShoppingListForm form = new ShoppingListForm();
        form.setName(TEST_SHOPPING_LIST_NAME);

        ResponseEntity<ShoppingList> response = restTemplate
                .postForEntity(SHOPPING_LIST_ENDPOINT, form, ShoppingList.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        shoppingListRepository.delete(response.getBody());

        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void updateShoppingList_updatesAndReturnsShoppingList() {
        ShoppingList existingShoppingList = shoppingListRepository
                .save(ShoppingList.builder().name(TEST_SHOPPING_LIST_NAME).build());

        ShoppingListForm form = new ShoppingListForm();
        form.setName("updated-test-shopping-list");

        ResponseEntity<ShoppingList> response = restTemplate
                .postForEntity(SHOPPING_LIST_ENDPOINT + existingShoppingList.getId(), form, ShoppingList.class);

        shoppingListRepository.delete(existingShoppingList);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShoppingList.getId(), response.getBody().getId());
        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void updateShoppingList_whenShoppingListDoesNotExist_returnsError() {
        ShoppingListForm form = new ShoppingListForm();
        form.setName("updated-test-shopping-list");

        ResponseEntity<ShoppingList> response = restTemplate
                .postForEntity(SHOPPING_LIST_ENDPOINT + Long.valueOf(999L), form, ShoppingList.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void deleteShoppingList_deletesShoppingList() {
        ShoppingList existingShoppingList = shoppingListRepository
                .save(ShoppingList.builder().name(TEST_SHOPPING_LIST_NAME).build());

        restTemplate.delete(SHOPPING_LIST_ENDPOINT + existingShoppingList.getId());

        assertFalse(shoppingListRepository.findById(existingShoppingList.getId()).isPresent());
    }

    @Test
    public void deleteShoppingList_whenShoppingListDoesNotExist_returnsError() {
        ResponseEntity<?> response = restTemplate
                .exchange(SHOPPING_LIST_ENDPOINT + Long.valueOf(999L),
                        HttpMethod.DELETE, null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
