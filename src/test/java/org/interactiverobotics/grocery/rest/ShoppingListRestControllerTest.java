/*
 * ShoppingListRestControllerTest.java
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

package org.interactiverobotics.grocery.rest;

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.form.ShoppingListForm;
import org.interactiverobotics.grocery.service.ShoppingListService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ShoppingList REST controller test.
 * Tests Controller with mocked Service.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ShoppingListRestController.class)
public class ShoppingListRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ShoppingListService shoppingListService;


    @Test
    public void testGetShoppingLists() throws Exception {

        final List<ShoppingList> existingShoppingList = Arrays.asList(
                new ShoppingList(1L, "test-shopping-list-1"), new ShoppingList(2L, "test-shopping-list-2"));
        when(shoppingListService.getShoppingLists()).thenReturn(existingShoppingList);

        mvc.perform(get("/api/v1/shopping_list/").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(existingShoppingList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(existingShoppingList.get(0).getName())))
                .andExpect(jsonPath("$[1].id", is(existingShoppingList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(existingShoppingList.get(1).getName())));
    }


    public static class ShoppingListPageAnswer implements Answer<Page<ShoppingList>> {

        private final List<ShoppingList> shoppingLists;

        public ShoppingListPageAnswer(final List<ShoppingList> shoppingLists) {
            this.shoppingLists = shoppingLists;
        }

        @Override
        public Page<ShoppingList> answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            final Pageable pageable = invocation.getArgumentAt(0, Pageable.class);
            return new PageImpl<>(shoppingLists, pageable, shoppingLists.size());
        }
    }


    @Test
    public void testGetShoppingListsPage() throws Exception {

        final List<ShoppingList> existingShoppingLists = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShoppingLists.add(new ShoppingList(i, "test-shopping-list-" + i));
        }

        final ShoppingListPageAnswer shoppingListPageAnswer = new ShoppingListPageAnswer(existingShoppingLists);
        when(shoppingListService.getShoppingLists(any(Pageable.class))).thenAnswer(shoppingListPageAnswer);

        mvc.perform(get("/api/v1/shopping_list/list?page=1&size=10").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.totalElements", is(existingShoppingLists.size())))
                .andExpect(jsonPath("$.totalPages", is(10)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    public void testGetShoppingListById() throws Exception {

        final ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        when(shoppingListService.getShoppingListById(existingShoppingList.getId())).thenReturn(existingShoppingList);

        mvc.perform(get("/api/v1/shopping_list/" + existingShoppingList.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(existingShoppingList.getId().intValue())))
                .andExpect(jsonPath("$.name", is(existingShoppingList.getName())));
    }

    @Test(expected = Exception.class)
    public void testGetNotExistingShoppingListById() throws Exception {

        when(shoppingListService.getShoppingListById(any())).thenThrow(new ShoppingListNotFoundException(-1L));

        mvc.perform(get("/api/v1/shopping_list/" + new Long(999L)).accept(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testGetShoppingListByName() throws Exception {

        final ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        when(shoppingListService.getShoppingListByName(existingShoppingList.getName()))
                .thenReturn(existingShoppingList);

        mvc.perform(get("/api/v1/shopping_list/search?name=" + existingShoppingList.getName())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(existingShoppingList.getId().intValue())))
                .andExpect(jsonPath("$.name", is(existingShoppingList.getName())));
    }

    @Test(expected = Exception.class)
    public void testGetNotExistingShoppingListByName() throws Exception {

        when(shoppingListService.getShoppingListByName(any())).thenThrow(new ShoppingListNotFoundException(-1L));

        mvc.perform(get("/api/v1/shopping_list/search?name=test").accept(MediaType.APPLICATION_JSON_UTF8));
    }


    public static class CreateShoppingListAnswer implements Answer<ShoppingList> {

        private ShoppingList shoppingList;

        public ShoppingList getShoppingList() {
            return shoppingList;
        }

        @Override
        public ShoppingList answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            final ShoppingListForm form = invocation.getArgumentAt(0, ShoppingListForm.class);
            shoppingList = new ShoppingList(1L, form.getName());
            return shoppingList;
        }
    }


    @Test
    public void testCreateShoppingList() throws Exception {

        final CreateShoppingListAnswer createShoppingListAnswer = new CreateShoppingListAnswer();
        when(shoppingListService.createShoppingList(any(ShoppingListForm.class))).then(createShoppingListAnswer);

        mvc.perform(post("/api/v1/shopping_list/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"name\":\"test-shopping-list\"}")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(createShoppingListAnswer.getShoppingList().getId().intValue())))
                .andExpect(jsonPath("$.name", is(createShoppingListAnswer.getShoppingList().getName())));
    }


    public static class UpdateShoppingListAnswer implements Answer<ShoppingList> {

        private final ShoppingList shoppingList;

        public UpdateShoppingListAnswer(final ShoppingList shoppingList) {
            this.shoppingList = shoppingList;
        }

        public ShoppingList getShoppingList() {
            return shoppingList;
        }

        @Override
        public ShoppingList answer(InvocationOnMock invocation) throws Throwable {

            assertEquals(2, invocation.getArguments().length);

            final Long id = invocation.getArgumentAt(0, Long.class);
            assertEquals(shoppingList.getId(), id);

            final ShoppingListForm form = invocation.getArgumentAt(1, ShoppingListForm.class);
            shoppingList.setName(form.getName());

            return shoppingList;
        }
    }


    @Test
    public void testUpdateShoppingList() throws Exception {

        final ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        final UpdateShoppingListAnswer updateShoppingListAnswer = new UpdateShoppingListAnswer(existingShoppingList);
        when(shoppingListService.updateShoppingList(eq(existingShoppingList.getId()), any(ShoppingListForm.class)))
                .then(updateShoppingListAnswer);

        mvc.perform(post("/api/v1/shopping_list/" + existingShoppingList.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"name\":\"updated-test-shopping-list\"}")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(updateShoppingListAnswer.getShoppingList().getId().intValue())))
                .andExpect(jsonPath("$.name", is(updateShoppingListAnswer.getShoppingList().getName())));
    }

    @Test(expected = Exception.class)
    public void testUpdateNotExistingShoppingList() throws Exception {

        when(shoppingListService.updateShoppingList(any(), any())).thenThrow(new ShoppingListNotFoundException(-1L));

        mvc.perform(post("/api/v1/shopping_list/" + new Long(999L))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"name\":\"updated-test-shopping-list\"}")
                .accept(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testDeleteShoppingList() throws Exception {

        final Long id = 1L;

        mvc.perform(delete("/api/v1/shopping_list/" + id).accept(MediaType.APPLICATION_JSON_UTF8));

        verify(shoppingListService).deleteShoppingList(eq(id));
    }

    @Test(expected = Exception.class)
    public void testDeleteNotExistingShoppingList() throws Exception {

        doThrow(new ShoppingListNotFoundException(-1L)).when(shoppingListService).deleteShoppingList(any());

        mvc.perform(delete("/api/v1/shopping_list/" + new Long(999L)).accept(MediaType.APPLICATION_JSON_UTF8));
    }

}
