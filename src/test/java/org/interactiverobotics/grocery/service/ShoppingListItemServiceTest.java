/*
 * ShoppingListItemServiceTest.java
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
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ShoppingListItem service test.
 * Tests Service class with mocked Repository.
 */
@ExtendWith(MockitoExtension.class)
public class ShoppingListItemServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ShoppingListItemRepository shoppingListItemRepository;

    @InjectMocks
    private ShoppingListItemService shoppingListItemService;

    private ShoppingList shoppingList;

    private Item item;

    /**
     * Initializes test.
     */
    @BeforeEach
    public void setUp() {
        shoppingList = ShoppingList.builder().id(1L).name("test-shopping-list").build();
        lenient().when(shoppingListRepository.findById(shoppingList.getId())).thenReturn(Optional.of(shoppingList));

        item = Item.builder().id(1L).name("test-item").build();
        lenient().when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
    }


    @Test
    public void getShoppingListItems_returnsShoppingListItems() {
        List<ShoppingListItem> existingShoppingListItems = List.of(
                ShoppingListItem.builder().shoppingList(shoppingList).item(item).quantity(1L).build(),
                ShoppingListItem.builder().shoppingList(shoppingList).item(item).quantity(2L).build());
        when(shoppingListItemRepository.findAllByShoppingList(shoppingList)).thenReturn(existingShoppingListItems);

        List<ShoppingListItem> shoppingListItems =
                shoppingListItemService.getShoppingListItems(shoppingList.getId());

        assertEquals(existingShoppingListItems, shoppingListItems);
    }

    @Test
    public void getShoppingListItems_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(ShoppingListNotFoundException.class, () ->
                shoppingListItemService.getShoppingListItems(Long.valueOf(999L)));
    }

    @Test
    public void getShoppingListItems_givenPageRequest_returnsPageOfShoppingListItems() {
        List<ShoppingListItem> existingShoppingListItems = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShoppingListItems.add(ShoppingListItem.builder()
                    .id(i + 1).shoppingList(shoppingList).item(item).quantity(1L).build());
        }

        when(shoppingListItemRepository.findAllByShoppingList(any(Pageable.class), eq(shoppingList)))
                .thenAnswer(invocation -> {
                    assertEquals(2, invocation.getArguments().length);
                    Pageable pageable = invocation.getArgument(0);
                    return new PageImpl<>(existingShoppingListItems, pageable, existingShoppingListItems.size());
                });

        Page<ShoppingListItem> shoppingListItems =
                shoppingListItemService.getShoppingListItems(PageRequest.of(0, 10), shoppingList.getId());

        assertEquals(existingShoppingListItems.size(), shoppingListItems.getTotalElements());
        assertEquals(10, shoppingListItems.getTotalPages());
    }

    @Test
    public void getShoppingListItems_givenPageRequest_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(ShoppingListNotFoundException.class, () ->
                shoppingListItemService.getShoppingListItems(PageRequest.of(0, 10), Long.valueOf(999L)));
    }

    @Test
    public void getShoppingListItemById_returnsShoppingListItem() {
        ShoppingListItem existingShoppingListItem = ShoppingListItem.builder()
                .id(1L).shoppingList(shoppingList).item(item).quantity(1L).build();
        when(shoppingListItemRepository.findById(existingShoppingListItem.getId()))
                .thenReturn(Optional.of(existingShoppingListItem));

        ShoppingListItem shoppingListItem =
                shoppingListItemService.getShoppingListItemById(existingShoppingListItem.getId());

        assertEquals(existingShoppingListItem, shoppingListItem);
    }

    @Test
    public void getShoppingListItemById_whenShoppingListItemDoesNotExist_throwsException() {
        assertThrows(ShoppingListItemNotFoundException.class, () -> {
            when(shoppingListItemRepository.findById(any())).thenReturn(Optional.empty());

            shoppingListItemService.getShoppingListItemById(1L);
        });
    }

    @Test
    public void getNotAddedItems_returnsItems() {
        List<Item> existingItems = List.of(
                Item.builder().id(1L).name("test-item-1").build(),
                Item.builder().id(2L).name("test-item-2").build());
        when(itemRepository.findAll()).thenReturn(existingItems);

        ShoppingListItem existingShoppingListItem = ShoppingListItem.builder()
                .id(1L).shoppingList(shoppingList).item(existingItems.get(0)).quantity(1L).build();

        when(shoppingListItemRepository.findOneByShoppingListAndItem(shoppingList, existingItems.get(0)))
                .thenReturn(existingShoppingListItem);

        List<Item> notAddedItems = shoppingListItemService.getNotAddedItems(shoppingList.getId());

        assertEquals(1, notAddedItems.size());
        assertEquals(existingItems.get(1), notAddedItems.get(0));
    }

    @Test
    public void createShoppingListItem_createsAndReturnsShoppingListItem() {
        when(shoppingListItemRepository.save(any(ShoppingListItem.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Long quantity = 1L;

        ShoppingListItem shoppingListItem = shoppingListItemService
                .createShoppingListItem(new ShoppingListItemCreateForm(shoppingList.getId(), item.getId(), quantity));

        ArgumentCaptor<ShoppingListItem> captor = ArgumentCaptor.forClass(ShoppingListItem.class);
        verify(shoppingListItemRepository).save(captor.capture());
        ShoppingListItem savedShoppingListItem = captor.getValue();

        assertEquals(savedShoppingListItem, shoppingListItem);
        assertEquals(shoppingList, shoppingListItem.getShoppingList());
        assertEquals(item, shoppingListItem.getItem());
        assertEquals(quantity, shoppingListItem.getQuantity());
    }

    @Test
    public void createShoppingListItem_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(ShoppingListNotFoundException.class, () ->
                shoppingListItemService.createShoppingListItem(
                        new ShoppingListItemCreateForm(999L, item.getId(), 1L)));
    }

    @Test
    public void createShoppingListItem_whenItemDoesNotExist_throwsExceptions() {
        assertThrows(ItemNotFoundException.class, () ->
                shoppingListItemService.createShoppingListItem(
                        new ShoppingListItemCreateForm(shoppingList.getId(),999L, 1L)));
    }

    @Test
    public void createShoppingListItem_whenQuantityLessOrEqualToZero_throwsExceptions() {
        assertThrows(IllegalArgumentException.class, () ->
                shoppingListItemService.createShoppingListItem(
                        new ShoppingListItemCreateForm(shoppingList.getId(), item.getId(), 0L)));
    }

    @Test
    public void updateShoppingListItem_updatesAndReturnsShoppingListItem() {
        ShoppingListItem existingShoppingListItem = ShoppingListItem.builder()
                .id(1L).shoppingList(shoppingList).item(item).quantity(1L).build();
        when(shoppingListItemRepository.findById(existingShoppingListItem.getId()))
                .thenReturn(Optional.of(existingShoppingListItem));

        when(shoppingListItemRepository.save(any(ShoppingListItem.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Long quantity = 2L;

        ShoppingListItem shoppingListItem = shoppingListItemService
                .updateShoppingListItem(existingShoppingListItem.getId(), new ShoppingListItemUpdateForm(quantity));

        ArgumentCaptor<ShoppingListItem> captor = ArgumentCaptor.forClass(ShoppingListItem.class);
        verify(shoppingListItemRepository).save(captor.capture());
        ShoppingListItem savedShoppingListItem = captor.getValue();

        assertEquals(savedShoppingListItem, shoppingListItem);
        assertEquals(shoppingList, shoppingListItem.getShoppingList());
        assertEquals(item, shoppingListItem.getItem());
        assertEquals(quantity, shoppingListItem.getQuantity());
    }

    @Test
    public void updateShoppingListItem_whenShoppingListItemDoesNotExist_throwsException() {
        assertThrows(ShoppingListItemNotFoundException.class, () ->
                shoppingListItemService.updateShoppingListItem(999L,
                        new ShoppingListItemUpdateForm(1L)));
    }

    @Test
    public void updateShoppingListItem_whenQuantityLessOrEqualToZero_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ShoppingListItem existingShoppingListItem = new ShoppingListItem(1L, shoppingList, item, 1L);
            when(shoppingListItemRepository.findById(existingShoppingListItem.getId()))
                    .thenReturn(Optional.of(existingShoppingListItem));

            shoppingListItemService.updateShoppingListItem(existingShoppingListItem.getId(),
                    new ShoppingListItemUpdateForm(0L));
        });
    }

    @Test
    public void deleteShoppingListItem_deletesShoppingListItem() {
        ShoppingListItem existingShoppingListItem = ShoppingListItem.builder()
                .id(1L).shoppingList(shoppingList).item(item).quantity(1L).build();
        when(shoppingListItemRepository.findById(existingShoppingListItem.getId()))
                .thenReturn(Optional.of(existingShoppingListItem));

        shoppingListItemService.deleteShoppingListItem(existingShoppingListItem.getId());

        verify(shoppingListItemRepository).delete(existingShoppingListItem);
    }

    @Test
    public void deleteShoppingListItem_whenShoppingListItemDoesNotExist_throwsException() {
        assertThrows(ShoppingListItemNotFoundException.class, () ->
                shoppingListItemService.deleteShoppingListItem(999L));
    }
}
