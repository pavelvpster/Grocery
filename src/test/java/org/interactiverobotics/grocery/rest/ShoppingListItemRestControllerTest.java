/*
 * ShoppingListItemRestControllerTest.java
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

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.domain.ShoppingListItem;
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.form.ShoppingListItemCreateForm;
import org.interactiverobotics.grocery.form.ShoppingListItemUpdateForm;
import org.interactiverobotics.grocery.service.ShoppingListItemService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
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
 * ShoppingListItem REST controller test.
 * Tests Controller with mocked Service.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ShoppingListItemRestController.class)
public class ShoppingListItemRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ShoppingListItemService shoppingListItemService;


    @Test
    public void testGetShoppingListItems() throws Exception {

        final ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        final Item existingItem = new Item(1L, "test-item");
        final List<ShoppingListItem> existingShoppingListItems = Arrays.asList(
                new ShoppingListItem(1L, existingShoppingList, existingItem, 1L),
                new ShoppingListItem(2L, existingShoppingList, existingItem, 2L));
        when(shoppingListItemService.getShoppingListItems(existingShoppingList.getId()))
                .thenReturn(existingShoppingListItems);

        mvc.perform(get("/api/v1/shopping_list_item/" + existingShoppingList.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(existingShoppingListItems.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].shoppingList.id",
                        is(existingShoppingListItems.get(0).getShoppingList().getId().intValue())))
                .andExpect(jsonPath("$[0].item.id", is(existingShoppingListItems.get(0).getItem().getId().intValue())))
                .andExpect(jsonPath("$[0].quantity", is(existingShoppingListItems.get(0).getQuantity().intValue())))
                .andExpect(jsonPath("$[1].id", is(existingShoppingListItems.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].shoppingList.id",
                        is(existingShoppingListItems.get(1).getShoppingList().getId().intValue())))
                .andExpect(jsonPath("$[1].item.id", is(existingShoppingListItems.get(1).getItem().getId().intValue())))
                .andExpect(jsonPath("$[1].quantity", is(existingShoppingListItems.get(1).getQuantity().intValue())));
    }

    @Test(expected = Exception.class)
    public void testGetShoppingListItemsForWrongShoppingListId() throws Exception {

        when(shoppingListItemService.getShoppingListItems(anyLong())).thenThrow(new ShoppingListNotFoundException(-1L));

        mvc.perform(get("/api/v1/shopping_list_item/" + new Long(999L))
                .accept(MediaType.APPLICATION_JSON_UTF8));
    }


    public static class CreateShoppingListItemAnswer implements Answer<ShoppingListItem> {

        private final ShoppingList shoppingList;
        private final Item item;

        public CreateShoppingListItemAnswer(final ShoppingList shoppingList, final Item item) {
            this.shoppingList = shoppingList;
            this.item = item;
        }

        private ShoppingListItem shoppingListItem;

        public ShoppingListItem getShoppingListItem() {
            return shoppingListItem;
        }

        @Override
        public ShoppingListItem answer(InvocationOnMock invocation) throws Throwable {

            assertEquals(2, invocation.getArguments().length);

            final Long shoppingListId = invocation.getArgumentAt(0, Long.class);
            assertEquals(shoppingList.getId(), shoppingListId);

            final ShoppingListItemCreateForm form = invocation.getArgumentAt(1, ShoppingListItemCreateForm.class);
            assertNotNull(form);

            final Long itemId = form.getItem();
            assertEquals(item.getId(), itemId);

            final Long quantity = form.getQuantity();

            shoppingListItem = new ShoppingListItem();
            shoppingListItem.setId(1L);
            shoppingListItem.setShoppingList(shoppingList);
            shoppingListItem.setItem(item);
            shoppingListItem.setQuantity(quantity);

            return shoppingListItem;
        }
    }


    @Test
    public void testCreateShoppingListItem() throws Exception {

        final ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        final Item existingItem = new Item(1L, "test-item");

        final CreateShoppingListItemAnswer createShoppingListItemAnswer =
                new CreateShoppingListItemAnswer(existingShoppingList, existingItem);

        when(shoppingListItemService
                .createShoppingListItem(eq(existingShoppingList.getId()), any(ShoppingListItemCreateForm.class)))
                .thenAnswer(createShoppingListItemAnswer);

        mvc.perform(post("/api/v1/shopping_list_item/shopping_list/" + existingShoppingList.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"item\":" + existingItem.getId() + ",\"quantity\":1}")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(createShoppingListItemAnswer.getShoppingListItem().getId().intValue())))
                .andExpect(jsonPath("$.shoppingList.id", is(createShoppingListItemAnswer.getShoppingListItem()
                        .getShoppingList().getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(createShoppingListItemAnswer.getShoppingListItem()
                        .getItem().getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(createShoppingListItemAnswer.getShoppingListItem()
                        .getQuantity().intValue())));
    }

    @Test(expected = Exception.class)
    public void testCreateShoppingListItemForWrongParams() throws Exception {

        when(shoppingListItemService.createShoppingListItem(anyLong(), anyObject())).thenThrow(new Exception());

        mvc.perform(post("/api/v1/shopping_list_item/shopping_list/" + new Long(999L))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"item\":999,\"quantity\":-1}")
                .accept(MediaType.APPLICATION_JSON_UTF8));
    }


    public static class UpdateShoppingListItemAnswer implements Answer<ShoppingListItem> {

        private final ShoppingListItem shoppingListItem;

        public UpdateShoppingListItemAnswer(final ShoppingListItem shoppingListItem) {
            this.shoppingListItem = shoppingListItem;
        }

        @Override
        public ShoppingListItem answer(InvocationOnMock invocation) throws Throwable {

            assertEquals(2, invocation.getArguments().length);

            final Long shoppingListItemId = invocation.getArgumentAt(0, Long.class);
            assertEquals(shoppingListItem.getShoppingList().getId(), shoppingListItemId);

            final ShoppingListItemUpdateForm form = invocation.getArgumentAt(1, ShoppingListItemUpdateForm.class);
            assertNotNull(form);

            final Long quantity = form.getQuantity();
            shoppingListItem.setQuantity(quantity);

            return shoppingListItem;
        }
    }


    @Test
    public void testUpdateShoppingListItem() throws Exception {

        final ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        final Item existingItem = new Item(1L, "test-item");
        final ShoppingListItem existingShoppingListItem =
                new ShoppingListItem(1L, existingShoppingList, existingItem, 1L);

        final UpdateShoppingListItemAnswer updateShoppingListItemAnswer =
                new UpdateShoppingListItemAnswer(existingShoppingListItem);

        when(shoppingListItemService.updateShoppingListItem(eq(existingShoppingListItem.getId()),
                any(ShoppingListItemUpdateForm.class))).thenAnswer(updateShoppingListItemAnswer);

        final Long quantity = 2L;

        mvc.perform(post("/api/v1/shopping_list_item/" + existingShoppingListItem.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"quantity\":" + quantity + "}")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(existingShoppingListItem.getId().intValue())))
                .andExpect(jsonPath("$.shoppingList.id", is(existingShoppingListItem
                        .getShoppingList().getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(existingShoppingListItem.getItem().getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(quantity.intValue())));
    }

    @Test(expected = Exception.class)
    public void testUpdateShoppingListItemForWrongParams() throws Exception {

        when(shoppingListItemService.updateShoppingListItem(anyLong(), anyObject())).thenThrow(new Exception());

        mvc.perform(post("/api/v1/shopping_list_item/" + new Long(999L))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"quantity\":-1}")
                .accept(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testDeleteShoppingListItem() throws Exception {

        final Long shoppingListItemId = 1L;

        mvc.perform(delete("/api/v1/shopping_list_item/" + shoppingListItemId)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        verify(shoppingListItemService).deleteShoppingListItem(eq(shoppingListItemId));
    }

    @Test(expected = Exception.class)
    public void testDeleteShoppingListItemForWrongParams() throws Exception {

        doThrow(new Exception()).when(shoppingListItemService).deleteShoppingListItem(anyLong());

        mvc.perform(delete("/api/v1/shopping_list_item/" + new Long(999L))
                .accept(MediaType.APPLICATION_JSON_UTF8));
    }

}
