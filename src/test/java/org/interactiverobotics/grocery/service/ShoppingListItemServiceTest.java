/*
 * ShoppingListItemServiceTest.java
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

package org.interactiverobotics.grocery.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.domain.ShoppingListItem;
import org.interactiverobotics.grocery.exception.ItemNotFoundException;
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * ShoppingListItem service test.
 * Tests Service class with mocked Repository.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShoppingListItemServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ShoppingListItemRepository shoppingListItemRepository;

    private ShoppingListItemService shoppingListItemService;

    private ShoppingList shoppingList;

    private Item item;


    @Before
    public void setUp() throws Exception {

        shoppingListItemService = new ShoppingListItemService(shoppingListRepository, itemRepository,
                shoppingListItemRepository);

        shoppingList = new ShoppingList(1L, "test-shopping-list");

        item = new Item(1L, "test-item");

        when(shoppingListRepository.findOne(shoppingList.getId())).thenReturn(shoppingList);
        when(itemRepository.findOne(item.getId())).thenReturn(item);
    }


    public static class SaveAndReturnShoppingListItemAnswer implements Answer<ShoppingListItem> {

        private ShoppingListItem shoppingListItem;

        public ShoppingListItem getShoppingListItem() {
            return shoppingListItem;
        }

        @Override
        public ShoppingListItem answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            shoppingListItem = invocation.getArgumentAt(0, ShoppingListItem.class);
            if (shoppingListItem.getId() == null) {
                shoppingListItem.setId(1L);
            }
            return shoppingListItem;
        }
    }


    @Test
    public void testAddItem1() throws Exception {

        final SaveAndReturnShoppingListItemAnswer saveAndReturnShoppingListItemAnswer =
                new SaveAndReturnShoppingListItemAnswer();
        when(shoppingListItemRepository.save(any(ShoppingListItem.class))).then(saveAndReturnShoppingListItemAnswer);

        final Long quantity = 1L;

        final ShoppingListItem shoppingListItem = shoppingListItemService
                .addItem(shoppingList.getId(), item.getId(), quantity);

        // Check that Service returns what was saved
        final ShoppingListItem savedShoppingListItem = saveAndReturnShoppingListItemAnswer.getShoppingListItem();
        assertEquals(savedShoppingListItem, shoppingListItem);

        // Check response content
        assertEquals(shoppingList, shoppingListItem.getShoppingList());
        assertEquals(item, shoppingListItem.getItem());
        assertEquals(quantity, shoppingListItem.getQuantity());
    }

    @Test
    public void testAddItem2() throws Exception {

        final SaveAndReturnShoppingListItemAnswer saveAndReturnShoppingListItemAnswer =
                new SaveAndReturnShoppingListItemAnswer();
        when(shoppingListItemRepository.save(any(ShoppingListItem.class))).then(saveAndReturnShoppingListItemAnswer);

        final Long quantity = 1L;

        final ShoppingListItem shoppingListItem = shoppingListItemService.addItem(shoppingList, item, quantity);

        // Check that Service returns what was saved
        final ShoppingListItem savedShoppingListItem = saveAndReturnShoppingListItemAnswer.getShoppingListItem();
        assertEquals(savedShoppingListItem, shoppingListItem);

        // Check response content
        assertEquals(shoppingList, shoppingListItem.getShoppingList());
        assertEquals(item, shoppingListItem.getItem());
        assertEquals(quantity, shoppingListItem.getQuantity());
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testAddItemForWrongShoppingListId() throws Exception {
        shoppingListItemService.addItem(999L, item.getId(), 1L);
    }

    @Test(expected = ItemNotFoundException.class)
    public void testAddItemForWrongItemId() throws Exception {
        shoppingListItemService.addItem(shoppingList.getId(), 999L, 1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddItemForWrongQuantity() throws Exception {
        shoppingListItemService.addItem(shoppingList.getId(), item.getId(), 0L);
    }

    @Test
    public void testRemoveItem1() throws Exception {

        final ShoppingListItem existingShoppingListItem = new ShoppingListItem(1L, shoppingList, item, 1L);
        when(shoppingListItemRepository.findOneByShoppingListAndItem(shoppingList, item))
                .thenReturn(existingShoppingListItem);

        shoppingListItemService.deleteItem(shoppingList.getId(), item.getId());

        verify(shoppingListItemRepository).delete(existingShoppingListItem);
    }

    @Test
    public void testRemoveItem2() throws Exception {

        final ShoppingListItem existingShoppingListItem = new ShoppingListItem(1L, shoppingList, item, 1L);
        when(shoppingListItemRepository.findOneByShoppingListAndItem(shoppingList, item))
                .thenReturn(existingShoppingListItem);

        shoppingListItemService.deleteItem(shoppingList, item);

        verify(shoppingListItemRepository).delete(existingShoppingListItem);
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testRemoveItemForWrongShoppingListId() throws Exception {
        shoppingListItemService.deleteItem(999L, item.getId());
    }

    @Test(expected = ItemNotFoundException.class)
    public void testRemoveItemForWrongItemId() throws Exception {
        shoppingListItemService.deleteItem(shoppingList.getId(), 999L);
    }

    @Test
    public void testSetQuantity() throws Exception {

        final ShoppingListItem existingShoppingListItem = new ShoppingListItem(1L, shoppingList, item, 1L);
        when(shoppingListItemRepository.findOneByShoppingListAndItem(shoppingList, item))
                .thenReturn(existingShoppingListItem);

        final SaveAndReturnShoppingListItemAnswer saveAndReturnShoppingListItemAnswer =
                new SaveAndReturnShoppingListItemAnswer();
        when(shoppingListItemRepository.save(any(ShoppingListItem.class))).then(saveAndReturnShoppingListItemAnswer);

        final Long quantity = 2L;

        final ShoppingListItem shoppingListItem = shoppingListItemService.setQuantity(shoppingList, item, quantity);

        // Check that Service returns what was saved
        final ShoppingListItem savedShoppingListItem = saveAndReturnShoppingListItemAnswer.getShoppingListItem();
        assertEquals(savedShoppingListItem, shoppingListItem);

        // Check response content
        assertEquals(shoppingList, shoppingListItem.getShoppingList());
        assertEquals(item, shoppingListItem.getItem());
        assertEquals(quantity, shoppingListItem.getQuantity());
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testSetQuantityForWrongShoppingListId() throws Exception {
        shoppingListItemService.setQuantity(999L, item.getId(), 1L);
    }

    @Test(expected = ItemNotFoundException.class)
    public void testSetQuantityForWrongItemId() throws Exception {
        shoppingListItemService.setQuantity(shoppingList.getId(), 999L, 1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetQuantityForWrongQuantity() throws Exception {

        final ShoppingListItem existingShoppingListItem = new ShoppingListItem(1L, shoppingList, item, 1L);
        when(shoppingListItemRepository.findOneByShoppingListAndItem(shoppingList, item))
                .thenReturn(existingShoppingListItem);

        shoppingListItemService.setQuantity(shoppingList.getId(), item.getId(), 0L);
    }

}
