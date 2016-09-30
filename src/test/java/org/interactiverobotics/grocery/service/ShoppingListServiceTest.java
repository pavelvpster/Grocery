package org.interactiverobotics.grocery.service;

import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

/**
 * ShoppingList service test.
 * Tests Service class with mocked Repository.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    private ShoppingListService shoppingListService;


    @Before
    public void setUp() throws Exception {
        shoppingListService = new ShoppingListService(shoppingListRepository);
    }


    @Test
    public void testGetShoppingLists() {

        final List<ShoppingList> existingShoppingLists = Arrays.asList(
                new ShoppingList(1L, "shopping-list-1"), new ShoppingList(2L, "shopping-list-2"));
        when(shoppingListRepository.findAll()).thenReturn(existingShoppingLists);

        final List<ShoppingList> shoppingLists = shoppingListService.getShoppingLists();

        assertEquals(existingShoppingLists, shoppingLists);
    }

}
