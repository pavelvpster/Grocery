/*
 * ItemServiceTest.java
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

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.exception.ItemNotFoundException;
import org.interactiverobotics.grocery.form.ItemForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

/**
 * Item service test.
 * Tests Service class with mocked Repository.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    private ItemService itemService;


    @Before
    public void setUp() throws Exception {
        itemService = new ItemService(itemRepository);
    }


    @Test
    public void testGetItems() {

        final List<Item> existingItems = Arrays.asList(new Item(1L, "test-item-1"), new Item(2L, "test-item-2"));
        when(itemRepository.findAll()).thenReturn(existingItems);

        final List<Item> items = itemService.getItems();

        assertEquals(existingItems, items);
    }

    @Test
    public void testGetItemById() {

        Item existingItem = new Item(1L, "test-item");
        when(itemRepository.findOne(existingItem.getId())).thenReturn(existingItem);

        final Item item = itemService.getItemById(existingItem.getId());

        assertEquals(existingItem, item);
    }

    @Test(expected = ItemNotFoundException.class)
    public void testGetNotExistingItemById() {

        when(itemRepository.findOne(any())).thenReturn(null);

        itemService.getItemById(1L);
    }

    @Test
    public void testGetItemByName() {

        Item existingItem = new Item(1L, "test-item");
        when(itemRepository.findOneByName(existingItem.getName())).thenReturn(existingItem);

        final Item item = itemService.getItemByName("test-item");

        assertEquals(existingItem, item);
    }

    @Test(expected = ItemNotFoundException.class)
    public void testGetNotExistingItemByName() {

        when(itemRepository.findOne(any())).thenReturn(null);

        itemService.getItemByName("test-name");
    }


    public static class SaveAndReturnItemAnswer implements Answer<Item> {

        private Item item;

        public Item getItem() {
            return item;
        }

        @Override
        public Item answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            item = invocation.getArgumentAt(0, Item.class);
            if (item.getId() == null) {
                item.setId(1L);
            }
            return item;
        }
    }


    @Test
    public void testCreateItem() {

        final SaveAndReturnItemAnswer saveAndReturnItemAnswer = new SaveAndReturnItemAnswer();
        when(itemRepository.save(any(Item.class))).then(saveAndReturnItemAnswer);

        final ItemForm form = new ItemForm("test-item");

        final Item item = itemService.createItem(form);

        // Check that Service returns what was saved
        final Item savedItem = saveAndReturnItemAnswer.getItem();
        assertEquals(savedItem, item);

        // Check response content
        assertEquals(form.getName(), item.getName());
    }

    @Test
    public void testUpdateItem() {

        Item existingItem = new Item(1L, "test-item");
        when(itemRepository.findOne(existingItem.getId())).thenReturn(existingItem);

        final SaveAndReturnItemAnswer saveAndReturnItemAnswer = new SaveAndReturnItemAnswer();
        when(itemRepository.save(any(Item.class))).then(saveAndReturnItemAnswer);

        final ItemForm form = new ItemForm("updated-test-item");

        final Item item = itemService.updateItem(existingItem.getId(), form);

        // Check that Service returns what was saved
        final Item savedItem = saveAndReturnItemAnswer.getItem();
        assertEquals(savedItem, item);

        // Check response content
        assertEquals(form.getName(), item.getName());
    }

    @Test(expected = ItemNotFoundException.class)
    public void testUpdateNotExistingItem() {

        when(itemRepository.findOne(any())).thenReturn(null);

        final ItemForm form = new ItemForm("updated-test-item");

        itemService.updateItem(999L, form);
    }

    @Test
    public void testDeleteItem() {

        Item existingItem = new Item(1L, "test-item");
        when(itemRepository.findOne(existingItem.getId())).thenReturn(existingItem);

        itemService.deleteItem(existingItem.getId());

        verify(itemRepository).delete(eq(existingItem));
    }

    @Test(expected = ItemNotFoundException.class)
    public void testDeleteNotExistingItem() {

        when(itemRepository.findOne(any())).thenReturn(null);

        itemService.deleteItem(999L);
    }

}
