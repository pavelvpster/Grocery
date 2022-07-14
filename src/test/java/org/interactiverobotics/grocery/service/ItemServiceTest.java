/*
 * ItemServiceTest.java
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

package org.interactiverobotics.grocery.service;

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.exception.ItemNotFoundException;
import org.interactiverobotics.grocery.form.ItemForm;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Item service test.
 * Tests Service class with mocked Repository.
 */
@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    private static final String TEST_ITEM_NAME = "test-item";

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    public void getItems_returnsItems() {
        List<Item> existingItems = List.of(
                Item.builder().id(1L).name("test-item-1").build(),
                Item.builder().id(2L).name("test-item-2").build());
        when(itemRepository.findAll()).thenReturn(existingItems);

        List<Item> items = itemService.getItems();

        assertEquals(existingItems, items);
    }

    @Test
    public void getItems_givenPageRequest_returnsPageOfItems() {
        List<Item> existingItems = new ArrayList<>();
        for (long i = 0; i < 100; i++) {
            existingItems.add(Item.builder().id(i).name("test-item-" + i).build());
        }

        when(itemRepository.findAll(any(Pageable.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingItems, pageable, existingItems.size());
        });

        Page<Item> items = itemService.getItems(PageRequest.of(0, 10));

        assertEquals(existingItems.size(), items.getTotalElements());
        assertEquals(10, items.getTotalPages());
    }

    @Test
    public void getItemById_returnsItem() {
        Item existingItem = Item.builder().id(1L).name(TEST_ITEM_NAME).build();
        when(itemRepository.findById(existingItem.getId())).thenReturn(Optional.of(existingItem));

        Item item = itemService.getItemById(existingItem.getId());

        assertEquals(existingItem, item);
    }

    @Test
    public void getItemById_whenItemDoesNotExist_throwsException() {
        assertThrows(ItemNotFoundException.class, () -> {
            when(itemRepository.findById(any())).thenReturn(Optional.empty());

            itemService.getItemById(1L);
        });
    }

    @Test
    public void getItemByName_returnsItem() {
        Item existingItem = Item.builder().id(1L).name(TEST_ITEM_NAME).build();
        when(itemRepository.findOneByName(existingItem.getName())).thenReturn(existingItem);

        Item item = itemService.getItemByName(TEST_ITEM_NAME);

        assertEquals(existingItem, item);
    }

    @Test
    public void getItemByName_whenItemDoesNotExist_throwsException() {
        assertThrows(ItemNotFoundException.class, () -> {
            when(itemRepository.findOneByName(anyString())).thenReturn(null);

            itemService.getItemByName(TEST_ITEM_NAME);
        });
    }

    @Test
    public void createItem_createsAndReturnsItem() {
        when(itemRepository.save(any(Item.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        ItemForm form = ItemForm.builder().name(TEST_ITEM_NAME).build();

        Item item = itemService.createItem(form);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        Item savedItem = captor.getValue();

        assertEquals(savedItem, item);
        assertEquals(form.getName(), item.getName());
    }

    @Test
    public void updateItem_updatesAndReturnsItem() {
        Item existingItem = Item.builder().id(1L).name(TEST_ITEM_NAME).build();
        when(itemRepository.findById(existingItem.getId())).thenReturn(Optional.of(existingItem));

        when(itemRepository.save(any(Item.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        ItemForm form = ItemForm.builder().name("updated-test-item").build();

        Item item = itemService.updateItem(existingItem.getId(), form);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        Item savedItem = captor.getValue();

        assertEquals(savedItem, item);
        assertEquals(form.getName(), item.getName());
    }

    @Test
    public void updateItem_whenItemDoesNotExist_throwsException() {
        assertThrows(ItemNotFoundException.class, () -> {
            when(itemRepository.findById(any())).thenReturn(Optional.empty());

            ItemForm form = ItemForm.builder().name("updated-test-item").build();

            itemService.updateItem(999L, form);
        });
    }

    @Test
    public void deleteItem_deletesItem() {
        Item existingItem = Item.builder().id(1L).name(TEST_ITEM_NAME).build();
        when(itemRepository.findById(existingItem.getId())).thenReturn(Optional.of(existingItem));

        itemService.deleteItem(existingItem.getId());

        verify(itemRepository).delete(eq(existingItem));
    }

    @Test
    public void deleteItem_whenItemDoesNotExist_throwsException() {
        assertThrows(ItemNotFoundException.class, () -> {
            when(itemRepository.findById(any())).thenReturn(Optional.empty());

            itemService.deleteItem(999L);
        });
    }
}
