/*
 * ShoppingListServiceTest.java
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

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.form.ShoppingListForm;
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
 * ShoppingList service test.
 * Tests Service class with mocked Repository.
 */
@RunWith(SpringRunner.class)
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
                new ShoppingList(1L, "test-shopping-list-1"), new ShoppingList(2L, "test-shopping-list-2"));
        when(shoppingListRepository.findAll()).thenReturn(existingShoppingLists);

        final List<ShoppingList> shoppingLists = shoppingListService.getShoppingLists();

        assertEquals(existingShoppingLists, shoppingLists);
    }

    @Test
    public void testGetShoppingListsPage() {

        final List<ShoppingList> existingShoppingLists = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShoppingLists.add(new ShoppingList(i, "test-shopping-list-" + i));
        }

        when(shoppingListRepository.findAll(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            final Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingShoppingLists, pageable, existingShoppingLists.size());
        });

        final Page<ShoppingList> shoppingLists = shoppingListService.getShoppingLists(PageRequest.of(0, 10));

        assertEquals(existingShoppingLists.size(), shoppingLists.getTotalElements());
        assertEquals(10, shoppingLists.getTotalPages());
    }

    @Test
    public void testGetShoppingListById() {

        ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        when(shoppingListRepository.findById(existingShoppingList.getId())).thenReturn(Optional.of(existingShoppingList));

        final ShoppingList shoppingList = shoppingListService.getShoppingListById(existingShoppingList.getId());

        assertEquals(existingShoppingList, shoppingList);
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testGetNotExistingShoppingListById() {

        when(shoppingListRepository.findById(any())).thenReturn(Optional.empty());

        shoppingListService.getShoppingListById(1L);
    }

    @Test
    public void testGetShoppingListByName() {

        ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        when(shoppingListRepository.findOneByName(existingShoppingList.getName())).thenReturn(existingShoppingList);

        final ShoppingList shoppingList = shoppingListService.getShoppingListByName("test-shopping-list");

        assertEquals(existingShoppingList, shoppingList);
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testGetNotExistingShoppingListByName() {

        when(shoppingListRepository.findById(any())).thenReturn(Optional.empty());

        shoppingListService.getShoppingListByName("test-name");
    }


    public static class SaveAndReturnShoppingListAnswer implements Answer<ShoppingList> {

        private ShoppingList shoppingList;

        public ShoppingList getShoppingList() {
            return shoppingList;
        }

        @Override
        public ShoppingList answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            shoppingList = invocation.getArgument(0);
            if (shoppingList.getId() == null) {
                shoppingList.setId(1L);
            }
            return shoppingList;
        }
    }


    @Test
    public void testCreateShoppingList() {

        final SaveAndReturnShoppingListAnswer saveAndReturnShoppingListAnswer = new SaveAndReturnShoppingListAnswer();
        when(shoppingListRepository.save(any(ShoppingList.class))).then(saveAndReturnShoppingListAnswer);

        final ShoppingListForm form = new ShoppingListForm("test-shopping-list");

        final ShoppingList shoppingList = shoppingListService.createShoppingList(form);

        // Check that Service returns what was saved
        final ShoppingList savedShoppingList = saveAndReturnShoppingListAnswer.getShoppingList();
        assertEquals(savedShoppingList, shoppingList);

        // Check response content
        assertEquals(form.getName(), shoppingList.getName());
    }

    @Test
    public void testUpdateShoppingList() {

        ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        when(shoppingListRepository.findById(existingShoppingList.getId())).thenReturn(Optional.of(existingShoppingList));

        final SaveAndReturnShoppingListAnswer saveAndReturnShoppingListAnswer = new SaveAndReturnShoppingListAnswer();
        when(shoppingListRepository.save(any(ShoppingList.class))).then(saveAndReturnShoppingListAnswer);

        final ShoppingListForm form = new ShoppingListForm("updated-test-shopping-list");

        final ShoppingList shoppingList = shoppingListService.updateShoppingList(existingShoppingList.getId(), form);

        // Check that Service returns what was saved
        final ShoppingList savedShoppingList = saveAndReturnShoppingListAnswer.getShoppingList();
        assertEquals(savedShoppingList, shoppingList);

        // Check response content
        assertEquals(form.getName(), shoppingList.getName());
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testUpdateNotExistingShoppingList() {

        when(shoppingListRepository.findById(any())).thenReturn(Optional.empty());

        final ShoppingListForm form = new ShoppingListForm("updated-test-shopping-list");

        shoppingListService.updateShoppingList(999L, form);
    }

    @Test
    public void testDeleteShoppingList() {

        ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        when(shoppingListRepository.findById(existingShoppingList.getId())).thenReturn(Optional.of(existingShoppingList));

        shoppingListService.deleteShoppingList(existingShoppingList.getId());

        verify(shoppingListRepository).delete(eq(existingShoppingList));
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testDeleteNotExistingShoppingList() {

        when(shoppingListRepository.findById(any())).thenReturn(Optional.empty());

        shoppingListService.deleteShoppingList(999L);
    }
}
