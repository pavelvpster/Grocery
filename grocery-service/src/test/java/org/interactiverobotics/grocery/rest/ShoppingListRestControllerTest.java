/*
 * ShoppingListRestControllerTest.java
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
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.form.ShoppingListForm;
import org.interactiverobotics.grocery.service.ShoppingListService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ShoppingList REST controller test.
 * Tests Controller with mocked Service.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(ShoppingListRestController.class)
public class ShoppingListRestControllerTest {

    private static final String SHOPPING_LIST_ENDPOINT = "/api/v1/shopping_list/";
    private static final String ID_SELECTOR = "$.id";
    private static final String NAME_SELECTOR = "$.name";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ShoppingListService shoppingListService;


    @Test
    public void getShoppingLists_returnsShoppingLists() throws Exception {
        List<ShoppingList> existingShoppingList = List.of(
                ShoppingList.builder().id(1L).name("test-shopping-list-1").build(),
                ShoppingList.builder().id(2L).name("test-shopping-list-2").build());
        when(shoppingListService.getShoppingLists()).thenReturn(existingShoppingList);

        mvc.perform(get(SHOPPING_LIST_ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(existingShoppingList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(existingShoppingList.get(0).getName())))
                .andExpect(jsonPath("$[1].id", is(existingShoppingList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(existingShoppingList.get(1).getName())));
    }

    @Test
    public void getShoppingListsPage_returnsPageOfShoppingLists() throws Exception {
        List<ShoppingList> existingShoppingLists = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShoppingLists.add(ShoppingList.builder().id(i).name("test-shopping-list-" + i).build());
        }

        when(shoppingListService.getShoppingLists(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingShoppingLists, pageable, existingShoppingLists.size());
        });

        mvc.perform(get(SHOPPING_LIST_ENDPOINT + "list?page=1&size=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(existingShoppingLists.size())))
                .andExpect(jsonPath("$.totalPages", is(10)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    public void getShoppingListById_returnsShoppingList() throws Exception {
        ShoppingList existingShoppingList = ShoppingList.builder().id(1L).name("test-shopping-list").build();
        when(shoppingListService.getShoppingListById(existingShoppingList.getId())).thenReturn(existingShoppingList);

        mvc.perform(get(SHOPPING_LIST_ENDPOINT + existingShoppingList.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(existingShoppingList.getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(existingShoppingList.getName())));
    }

    @Test
    public void getShoppingListById_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(shoppingListService.getShoppingListById(any())).thenThrow(new ShoppingListNotFoundException(-1L));

            mvc.perform(get(SHOPPING_LIST_ENDPOINT + Long.valueOf(999L)).accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void getShoppingListByName_returnsShoppingList() throws Exception {
        ShoppingList existingShoppingList = ShoppingList.builder().id(1L).name("test-shopping-list").build();
        when(shoppingListService.getShoppingListByName(existingShoppingList.getName()))
                .thenReturn(existingShoppingList);

        mvc.perform(get(SHOPPING_LIST_ENDPOINT + "search?name=" + existingShoppingList.getName())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(existingShoppingList.getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(existingShoppingList.getName())));
    }

    @Test
    public void getShoppingListByName_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(shoppingListService.getShoppingListByName(any())).thenThrow(new ShoppingListNotFoundException(-1L));

            mvc.perform(get(SHOPPING_LIST_ENDPOINT + "search?name=test").accept(MediaType.APPLICATION_JSON));
        });
    }


    public static class CreateShoppingListAnswer implements Answer<ShoppingList> {

        private ShoppingList shoppingList;

        public ShoppingList getShoppingList() {
            return shoppingList;
        }

        @Override
        public ShoppingList answer(InvocationOnMock invocation) {
            assertEquals(1, invocation.getArguments().length);
            ShoppingListForm form = invocation.getArgument(0);
            shoppingList = ShoppingList.builder().id(1L).name(form.getName()).build();
            return shoppingList;
        }
    }


    @Test
    public void createShoppingList_createsAndReturnsShoppingList() throws Exception {
        CreateShoppingListAnswer createShoppingListAnswer = new CreateShoppingListAnswer();
        when(shoppingListService.createShoppingList(any(ShoppingListForm.class))).then(createShoppingListAnswer);

        mvc.perform(post(SHOPPING_LIST_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test-shopping-list\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(createShoppingListAnswer.getShoppingList().getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(createShoppingListAnswer.getShoppingList().getName())));

        verify(shoppingListService).createShoppingList(any(ShoppingListForm.class));
    }


    public static class UpdateShoppingListAnswer implements Answer<ShoppingList> {

        private final ShoppingList shoppingList;

        public UpdateShoppingListAnswer(ShoppingList shoppingList) {
            this.shoppingList = shoppingList;
        }

        public ShoppingList getShoppingList() {
            return shoppingList;
        }

        @Override
        public ShoppingList answer(InvocationOnMock invocation) {
            assertEquals(2, invocation.getArguments().length);

            Long id = invocation.getArgument(0);
            assertEquals(shoppingList.getId(), id);

            ShoppingListForm form = invocation.getArgument(1);
            shoppingList.setName(form.getName());

            return shoppingList;
        }
    }


    @Test
    public void updateShoppingList_updatesAndReturnsShoppingList() throws Exception {
        ShoppingList existingShoppingList = ShoppingList.builder().id(1L).name("test-shopping-list").build();
        UpdateShoppingListAnswer updateShoppingListAnswer = new UpdateShoppingListAnswer(existingShoppingList);
        when(shoppingListService.updateShoppingList(eq(existingShoppingList.getId()), any(ShoppingListForm.class)))
                .then(updateShoppingListAnswer);

        mvc.perform(post(SHOPPING_LIST_ENDPOINT + existingShoppingList.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"updated-test-shopping-list\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(updateShoppingListAnswer.getShoppingList().getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(updateShoppingListAnswer.getShoppingList().getName())));

        verify(shoppingListService).updateShoppingList(eq(existingShoppingList.getId()), any(ShoppingListForm.class));
    }

    @Test
    public void updateShoppingList_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(shoppingListService.updateShoppingList(any(), any())).thenThrow(new ShoppingListNotFoundException(-1L));

            mvc.perform(post(SHOPPING_LIST_ENDPOINT + Long.valueOf(999L))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"updated-test-shopping-list\"}")
                    .accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void deleteShoppingList_deletesShoppingList() throws Exception {
        Long id = 1L;

        mvc.perform(delete(SHOPPING_LIST_ENDPOINT + id).accept(MediaType.APPLICATION_JSON));

        verify(shoppingListService).deleteShoppingList(eq(id));
    }

    @Test
    public void deleteShoppingList_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            doThrow(new ShoppingListNotFoundException(-1L)).when(shoppingListService).deleteShoppingList(any());

            mvc.perform(delete(SHOPPING_LIST_ENDPOINT + Long.valueOf(999L)).accept(MediaType.APPLICATION_JSON));
        });
    }
}
