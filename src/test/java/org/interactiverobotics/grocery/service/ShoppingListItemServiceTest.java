/*
 * ShoppingListItemServiceTest.java
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

package org.interactiverobotics.grocery.service;

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.domain.ShoppingListItem;
import org.interactiverobotics.grocery.exception.ItemNotFoundException;
import org.interactiverobotics.grocery.exception.ShoppingListItemNotFoundException;
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.form.ShoppingListItemCreateForm;
import org.interactiverobotics.grocery.form.ShoppingListItemUpdateForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListItemRepository;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ShoppingListItem service test.
 * Tests Service class with mocked Repository.
 */
@RunWith(SpringRunner.class)
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


    /**
     * Initializes test.
     */
    @Before
    public void setUp() throws Exception {

        shoppingListItemService = new ShoppingListItemService(shoppingListRepository, itemRepository,
                shoppingListItemRepository);

        shoppingList = new ShoppingList(1L, "test-shopping-list");

        item = new Item(1L, "test-item");

        when(shoppingListRepository.findById(shoppingList.getId())).thenReturn(Optional.of(shoppingList));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
    }


    @Test
    public void testGetShoppingListItems() {

        final List<ShoppingListItem> existingShoppingListItems = Arrays.asList(
                new ShoppingListItem(shoppingList, item, 1L), new ShoppingListItem(shoppingList, item, 2L));
        when(shoppingListItemRepository.findAllByShoppingList(shoppingList)).thenReturn(existingShoppingListItems);

        final List<ShoppingListItem> shoppingListItems =
                shoppingListItemService.getShoppingListItems(shoppingList.getId());

        assertEquals(existingShoppingListItems, shoppingListItems);
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testGetShoppingListItemsForWrongShoppingListId() {

        shoppingListItemService.getShoppingListItems(new Long(999L));
    }

    @Test
    public void testGetShoppingListItemsPage() {

        final List<ShoppingListItem> existingShoppingListItems = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShoppingListItems.add(new ShoppingListItem(i + 1, shoppingList, item, 1L));
        }

        when(shoppingListItemRepository.findAllByShoppingList(any(Pageable.class), eq(shoppingList)))
                .thenAnswer(invocation -> {
                    assertEquals(2, invocation.getArguments().length);
                    final Pageable pageable = invocation.getArgument(0);
                    return new PageImpl<>(existingShoppingListItems, pageable, existingShoppingListItems.size());
                });

        final Page<ShoppingListItem> shoppingListItems =
                shoppingListItemService.getShoppingListItems(PageRequest.of(0, 10), shoppingList.getId());

        assertEquals(existingShoppingListItems.size(), shoppingListItems.getTotalElements());
        assertEquals(10, shoppingListItems.getTotalPages());
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testGetShoppingListItemsPageForWrongShoppingListId() {

        shoppingListItemService.getShoppingListItems(PageRequest.of(0, 10), new Long(999L));
    }

    @Test
    public void testGetShoppingListItemById() {

        ShoppingListItem existingShoppingListItem = new ShoppingListItem(1L, shoppingList, item, 1L);
        when(shoppingListItemRepository.findById(existingShoppingListItem.getId())).thenReturn(Optional.of(existingShoppingListItem));

        final ShoppingListItem shoppingListItem =
                shoppingListItemService.getShoppingListItemById(existingShoppingListItem.getId());

        assertEquals(existingShoppingListItem, shoppingListItem);
    }

    @Test(expected = ShoppingListItemNotFoundException.class)
    public void testGetNotExistingShoppingListItemById() {

        when(shoppingListItemRepository.findById(any())).thenReturn(Optional.empty());

        shoppingListItemService.getShoppingListItemById(1L);
    }

    @Test
    public void testGetNotAddedItems() {

        final List<Item> existingItems = Arrays.asList(new Item(1L, "test-item-1"), new Item(2L, "test-item-2"));
        when(itemRepository.findAll()).thenReturn(existingItems);

        final ShoppingListItem existingShoppingListItem =
                new ShoppingListItem(1L, shoppingList, existingItems.get(0), 1L);

        when(shoppingListItemRepository.findOneByShoppingListAndItem(shoppingList, existingItems.get(0)))
                .thenReturn(existingShoppingListItem);

        final List<Item> notAddedItems = shoppingListItemService.getNotAddedItems(shoppingList.getId());

        assertEquals(1, notAddedItems.size());
        assertEquals(existingItems.get(1), notAddedItems.get(0));
    }


    public static class SaveAndReturnShoppingListItemAnswer implements Answer<ShoppingListItem> {

        private ShoppingListItem shoppingListItem;

        public ShoppingListItem getShoppingListItem() {
            return shoppingListItem;
        }

        @Override
        public ShoppingListItem answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            shoppingListItem = invocation.getArgument(0);
            if (shoppingListItem.getId() == null) {
                shoppingListItem.setId(1L);
            }
            return shoppingListItem;
        }
    }


    @Test
    public void testCreateShoppingListItem() throws Exception {

        final SaveAndReturnShoppingListItemAnswer saveAndReturnShoppingListItemAnswer =
                new SaveAndReturnShoppingListItemAnswer();
        when(shoppingListItemRepository.save(any(ShoppingListItem.class))).then(saveAndReturnShoppingListItemAnswer);

        final Long quantity = 1L;

        final ShoppingListItem shoppingListItem = shoppingListItemService
                .createShoppingListItem(new ShoppingListItemCreateForm(shoppingList.getId(), item.getId(), quantity));

        // Check that Service returns what was saved
        final ShoppingListItem savedShoppingListItem = saveAndReturnShoppingListItemAnswer.getShoppingListItem();
        assertEquals(savedShoppingListItem, shoppingListItem);

        // Check response content
        assertEquals(shoppingList, shoppingListItem.getShoppingList());
        assertEquals(item, shoppingListItem.getItem());
        assertEquals(quantity, shoppingListItem.getQuantity());
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testCreateShoppingListItemForWrongShoppingListId() throws Exception {
        shoppingListItemService.createShoppingListItem(new ShoppingListItemCreateForm(999L, item.getId(), 1L));
    }

    @Test(expected = ItemNotFoundException.class)
    public void testCreateShoppingListItemForWrongItemId() throws Exception {
        shoppingListItemService.createShoppingListItem(new ShoppingListItemCreateForm(shoppingList.getId(),999L, 1L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShoppingListItemForWrongQuantity() throws Exception {
        shoppingListItemService.createShoppingListItem(
                new ShoppingListItemCreateForm(shoppingList.getId(), item.getId(), 0L));
    }

    @Test
    public void testUpdateShoppingListItem() throws Exception {

        final ShoppingListItem existingShoppingListItem = new ShoppingListItem(1L, shoppingList, item, 1L);
        when(shoppingListItemRepository.findById(existingShoppingListItem.getId())).thenReturn(Optional.of(existingShoppingListItem));

        final SaveAndReturnShoppingListItemAnswer saveAndReturnShoppingListItemAnswer =
                new SaveAndReturnShoppingListItemAnswer();
        when(shoppingListItemRepository.save(any(ShoppingListItem.class))).then(saveAndReturnShoppingListItemAnswer);

        final Long quantity = 2L;

        final ShoppingListItem shoppingListItem = shoppingListItemService
                .updateShoppingListItem(existingShoppingListItem.getId(), new ShoppingListItemUpdateForm(quantity));

        // Check that Service returns what was saved
        final ShoppingListItem savedShoppingListItem = saveAndReturnShoppingListItemAnswer.getShoppingListItem();
        assertEquals(savedShoppingListItem, shoppingListItem);

        // Check response content
        assertEquals(shoppingList, shoppingListItem.getShoppingList());
        assertEquals(item, shoppingListItem.getItem());
        assertEquals(quantity, shoppingListItem.getQuantity());
    }

    @Test(expected = ShoppingListItemNotFoundException.class)
    public void testUpdateShoppingListItemForWrongShoppingListItemId() throws Exception {
        shoppingListItemService.updateShoppingListItem(999L, new ShoppingListItemUpdateForm(1L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShoppingListItemForWrongQuantity() throws Exception {

        final ShoppingListItem existingShoppingListItem = new ShoppingListItem(1L, shoppingList, item, 1L);
        when(shoppingListItemRepository.findById(existingShoppingListItem.getId())).thenReturn(Optional.of(existingShoppingListItem));

        shoppingListItemService.updateShoppingListItem(existingShoppingListItem.getId(),
                new ShoppingListItemUpdateForm(0L));
    }

    @Test
    public void testDeleteShoppingListItem() throws Exception {

        final ShoppingListItem existingShoppingListItem = new ShoppingListItem(1L, shoppingList, item, 1L);
        when(shoppingListItemRepository.findById(existingShoppingListItem.getId())).thenReturn(Optional.of(existingShoppingListItem));

        shoppingListItemService.deleteShoppingListItem(existingShoppingListItem.getId());

        verify(shoppingListItemRepository).delete(existingShoppingListItem);
    }

    @Test(expected = ShoppingListItemNotFoundException.class)
    public void testDeleteShoppingListItemForWrongShoppingListItemId() throws Exception {
        shoppingListItemService.deleteShoppingListItem(999L);
    }
}
