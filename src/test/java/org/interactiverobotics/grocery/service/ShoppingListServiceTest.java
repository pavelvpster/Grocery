/*
 * ShoppingListServiceTest.java
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

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.form.ShoppingListForm;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
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
 * ShoppingList service test.
 * Tests Service class with mocked Repository.
 */
@ExtendWith(MockitoExtension.class)
public class ShoppingListServiceTest {

    private static final String TEST_SHOPPING_LIST_NAME = "test-shopping-list";

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Test
    public void getShoppingLists_returnsShoppingList() {
        List<ShoppingList> existingShoppingLists = List.of(
                ShoppingList.builder().id(1L).name("test-shopping-list-1").build(),
                ShoppingList.builder().id(2L).name("test-shopping-list-2").build());
        when(shoppingListRepository.findAll()).thenReturn(existingShoppingLists);

        List<ShoppingList> shoppingLists = shoppingListService.getShoppingLists();

        assertEquals(existingShoppingLists, shoppingLists);
    }

    @Test
    public void getShoppingLists_givenPageRequest_returnsPageOfShoppingLists() {
        List<ShoppingList> existingShoppingLists = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShoppingLists.add(ShoppingList.builder().id(i).name("test-shopping-list-" + i).build());
        }

        when(shoppingListRepository.findAll(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingShoppingLists, pageable, existingShoppingLists.size());
        });

        Page<ShoppingList> shoppingLists = shoppingListService.getShoppingLists(PageRequest.of(0, 10));

        assertEquals(existingShoppingLists.size(), shoppingLists.getTotalElements());
        assertEquals(10, shoppingLists.getTotalPages());
    }

    @Test
    public void getShoppingListById_returnsShoppingList() {
        ShoppingList existingShoppingList = ShoppingList.builder().id(1L).name(TEST_SHOPPING_LIST_NAME).build();
        when(shoppingListRepository.findById(existingShoppingList.getId()))
                .thenReturn(Optional.of(existingShoppingList));

        ShoppingList shoppingList = shoppingListService.getShoppingListById(existingShoppingList.getId());

        assertEquals(existingShoppingList, shoppingList);
    }

    @Test
    public void getShoppingListById_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(ShoppingListNotFoundException.class, () -> {
            when(shoppingListRepository.findById(any())).thenReturn(Optional.empty());

            shoppingListService.getShoppingListById(1L);
        });
    }

    @Test
    public void getShoppingListByName_returnsShoppingList() {
        ShoppingList existingShoppingList = ShoppingList.builder().id(1L).name(TEST_SHOPPING_LIST_NAME).build();
        when(shoppingListRepository.findOneByName(existingShoppingList.getName())).thenReturn(existingShoppingList);

        ShoppingList shoppingList = shoppingListService.getShoppingListByName(TEST_SHOPPING_LIST_NAME);

        assertEquals(existingShoppingList, shoppingList);
    }

    @Test
    public void getShoppingListByName_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(ShoppingListNotFoundException.class, () -> {
            when(shoppingListRepository.findOneByName(anyString())).thenReturn(null);

            shoppingListService.getShoppingListByName("test-name");
        });
    }

    @Test
    public void createShoppingList_createsAndReturnsShoppingList() {
        when(shoppingListRepository.save(any(ShoppingList.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        ShoppingListForm form = new ShoppingListForm(TEST_SHOPPING_LIST_NAME);

        ShoppingList shoppingList = shoppingListService.createShoppingList(form);

        ArgumentCaptor<ShoppingList> captor = ArgumentCaptor.forClass(ShoppingList.class);
        verify(shoppingListRepository).save(captor.capture());
        ShoppingList savedShoppingList = captor.getValue();

        assertEquals(savedShoppingList, shoppingList);
        assertEquals(form.getName(), shoppingList.getName());
    }

    @Test
    public void updateShoppingList_updatesAndReturnsShoppingList() {
        ShoppingList existingShoppingList = ShoppingList.builder().id(1L).name(TEST_SHOPPING_LIST_NAME).build();
        when(shoppingListRepository.findById(existingShoppingList.getId()))
                .thenReturn(Optional.of(existingShoppingList));

        when(shoppingListRepository.save(any(ShoppingList.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        ShoppingListForm form = new ShoppingListForm("updated-test-shopping-list");

        ShoppingList shoppingList = shoppingListService.updateShoppingList(existingShoppingList.getId(), form);

        ArgumentCaptor<ShoppingList> captor = ArgumentCaptor.forClass(ShoppingList.class);
        verify(shoppingListRepository).save(captor.capture());
        ShoppingList savedShoppingList = captor.getValue();

        assertEquals(savedShoppingList, shoppingList);
        assertEquals(form.getName(), shoppingList.getName());
    }

    @Test
    public void updateShoppingList_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(ShoppingListNotFoundException.class, () -> {
            when(shoppingListRepository.findById(any())).thenReturn(Optional.empty());

            ShoppingListForm form = new ShoppingListForm("updated-test-shopping-list");

            shoppingListService.updateShoppingList(999L, form);
        });
    }

    @Test
    public void deleteShoppingList_deletesShoppingList() {
        ShoppingList existingShoppingList = ShoppingList.builder().id(1L).name(TEST_SHOPPING_LIST_NAME).build();
        when(shoppingListRepository.findById(existingShoppingList.getId()))
                .thenReturn(Optional.of(existingShoppingList));

        shoppingListService.deleteShoppingList(existingShoppingList.getId());

        verify(shoppingListRepository).delete(eq(existingShoppingList));
    }

    @Test
    public void deleteShoppingList_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(ShoppingListNotFoundException.class, () -> {
            when(shoppingListRepository.findById(any())).thenReturn(Optional.empty());

            shoppingListService.deleteShoppingList(999L);
        });
    }
}
