package org.interactiverobotics.grocery.rest;

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ShoppingList REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingListControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShoppingListRepository shoppingListRepository;


    @Before
    public void setUp() throws Exception {
        shoppingListRepository.deleteAll();
    }


    @Test
    public void testGetShoppingLists() {

        final List<ShoppingList> existingShoppingLists = new ArrayList<>();
        shoppingListRepository.save(Arrays.asList(
                new ShoppingList("shopping-list-1"), new ShoppingList("shopping-list-2")))
                .forEach(shoppingList -> existingShoppingLists.add(shoppingList));

        final ResponseEntity<ShoppingList[]> response = restTemplate.getForEntity("/api/v1/shopping_list/",
                ShoppingList[].class);

        shoppingListRepository.delete(existingShoppingLists);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShoppingLists, Arrays.asList(response.getBody()));
    }

}
