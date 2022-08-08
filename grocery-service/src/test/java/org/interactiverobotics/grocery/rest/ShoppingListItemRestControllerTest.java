/*
 * ShoppingListItemRestControllerTest.java
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

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.domain.ShoppingListItem;
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.form.ShoppingListItemCreateForm;
import org.interactiverobotics.grocery.form.ShoppingListItemUpdateForm;
import org.interactiverobotics.grocery.service.ShoppingListItemService;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ShoppingListItem REST controller test.
 * Tests Controller with mocked Service.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(ShoppingListItemRestController.class)
public class ShoppingListItemRestControllerTest {

    private static final String SHOPPING_LIST_ITEM_ENDPOINT = "/api/v1/shopping_list_item/";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ShoppingListItemService shoppingListItemService;


    @Test
    public void getShoppingListItems_returnsItems() throws Exception {
        ShoppingList existingShoppingList = ShoppingList.builder().id(1L).name("test-shopping-list").build();
        Item existingItem = Item.builder().id(1L).name("test-item").build();
        List<ShoppingListItem> existingShoppingListItems = List.of(
                ShoppingListItem.builder()
                        .id(1L).shoppingList(existingShoppingList).item(existingItem).quantity(1L).build(),
                ShoppingListItem.builder()
                        .id(2L).shoppingList(existingShoppingList).item(existingItem).quantity(2L).build());
        when(shoppingListItemService.getShoppingListItems(existingShoppingList.getId()))
                .thenReturn(existingShoppingListItems);

        mvc.perform(get(SHOPPING_LIST_ITEM_ENDPOINT + existingShoppingList.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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

    @Test
    public void getShoppingListItems_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(shoppingListItemService.getShoppingListItems(anyLong())).thenThrow(new ShoppingListNotFoundException(-1L));

            mvc.perform(get(SHOPPING_LIST_ITEM_ENDPOINT + Long.valueOf(999L))
                    .accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void getShoppingListItemsPage_returnsPageOfItems() throws Exception {
        ShoppingList existingShoppingList = ShoppingList.builder().id(1L).name("test-shopping-list").build();
        Item existingItem = Item.builder().id(1L).name("test-item").build();
        List<ShoppingListItem> existingShoppingListItems = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShoppingListItems.add(ShoppingListItem.builder()
                    .id(i).shoppingList(existingShoppingList).item(existingItem).quantity(1L).build());
        }

        when(shoppingListItemService.getShoppingListItems(any(Pageable.class), anyLong())).thenAnswer(invocation -> {
            assertEquals(2, invocation.getArguments().length);
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingShoppingListItems, pageable, existingShoppingListItems.size());
        });

        mvc.perform(get(SHOPPING_LIST_ITEM_ENDPOINT + existingShoppingList.getId() + "/list?page=1&size=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(existingShoppingListItems.size())))
                .andExpect(jsonPath("$.totalPages", is(10)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    public void getShoppingListItemsPage_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(shoppingListItemService.getShoppingListItems(any(Pageable.class), anyLong()))
                    .thenThrow(new ShoppingListNotFoundException(-1L));

            mvc.perform(get(SHOPPING_LIST_ITEM_ENDPOINT + Long.valueOf(999L) + "/list?page=1&size=10").accept(MediaType.APPLICATION_JSON));
        });
    }


    public static class CreateShoppingListItemAnswer implements Answer<ShoppingListItem> {

        private final ShoppingList shoppingList;
        private final Item item;

        public CreateShoppingListItemAnswer(ShoppingList shoppingList, Item item) {
            this.shoppingList = shoppingList;
            this.item = item;
        }

        private ShoppingListItem shoppingListItem;

        public ShoppingListItem getShoppingListItem() {
            return shoppingListItem;
        }

        @Override
        public ShoppingListItem answer(InvocationOnMock invocation) {
            assertEquals(1, invocation.getArguments().length);

            ShoppingListItemCreateForm form = invocation.getArgument(0);
            assertNotNull(form);

            Long shoppingListId = form.getShoppingList();
            assertEquals(shoppingList.getId(), shoppingListId);

            Long itemId = form.getItem();
            assertEquals(item.getId(), itemId);

            Long quantity = form.getQuantity();

            shoppingListItem = ShoppingListItem.builder()
                    .id(1L)
                    .shoppingList(shoppingList)
                    .item(item)
                    .quantity(quantity)
                    .build();

            return shoppingListItem;
        }
    }


    @Test
    public void createShoppingListItem_createsAndReturnsShoppingListItem() throws Exception {
        ShoppingList existingShoppingList = ShoppingList.builder().id(1L).name("test-shopping-list").build();
        Item existingItem = Item.builder().id(1L).name("test-item").build();

        CreateShoppingListItemAnswer createShoppingListItemAnswer =
                new CreateShoppingListItemAnswer(existingShoppingList, existingItem);

        when(shoppingListItemService.createShoppingListItem(any(ShoppingListItemCreateForm.class)))
                .thenAnswer(createShoppingListItemAnswer);

        mvc.perform(post(SHOPPING_LIST_ITEM_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"shoppingList\":" + existingShoppingList.getId() + ",\"item\":" + existingItem.getId()
                        + ",\"quantity\":1}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(createShoppingListItemAnswer.getShoppingListItem().getId().intValue())))
                .andExpect(jsonPath("$.shoppingList.id", is(createShoppingListItemAnswer.getShoppingListItem()
                        .getShoppingList().getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(createShoppingListItemAnswer.getShoppingListItem()
                        .getItem().getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(createShoppingListItemAnswer.getShoppingListItem()
                        .getQuantity().intValue())));

        verify(shoppingListItemService).createShoppingListItem(any(ShoppingListItemCreateForm.class));
    }

    @Test
    public void createShoppingListItem_whenShoppingListAndItemDoNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(shoppingListItemService.createShoppingListItem(any(ShoppingListItemCreateForm.class)))
                    .thenThrow(new Exception());

            mvc.perform(post(SHOPPING_LIST_ITEM_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"shoppingList\":999,\"item\":999,\"quantity\":-1}")
                    .accept(MediaType.APPLICATION_JSON));
        });
    }


    public static class UpdateShoppingListItemAnswer implements Answer<ShoppingListItem> {

        private final ShoppingListItem shoppingListItem;

        public UpdateShoppingListItemAnswer(ShoppingListItem shoppingListItem) {
            this.shoppingListItem = shoppingListItem;
        }

        @Override
        public ShoppingListItem answer(InvocationOnMock invocation) {

            assertEquals(2, invocation.getArguments().length);

            final Long shoppingListItemId = invocation.getArgument(0);
            assertEquals(shoppingListItem.getShoppingList().getId(), shoppingListItemId);

            final ShoppingListItemUpdateForm form = invocation.getArgument(1);
            assertNotNull(form);

            final Long quantity = form.getQuantity();
            shoppingListItem.setQuantity(quantity);

            return shoppingListItem;
        }
    }


    @Test
    public void updateShoppingListItem_updatesAndReturnsShoppingListItem() throws Exception {
        ShoppingList existingShoppingList = ShoppingList.builder().id(1L).name("test-shopping-list").build();
        Item existingItem = Item.builder().id(1L).name("test-item").build();
        ShoppingListItem existingShoppingListItem = ShoppingListItem.builder()
                .id(1L).shoppingList(existingShoppingList).item(existingItem).quantity(1L).build();

        UpdateShoppingListItemAnswer updateShoppingListItemAnswer =
                new UpdateShoppingListItemAnswer(existingShoppingListItem);

        when(shoppingListItemService.updateShoppingListItem(eq(existingShoppingListItem.getId()),
                any(ShoppingListItemUpdateForm.class))).thenAnswer(updateShoppingListItemAnswer);

        Long quantity = 2L;

        mvc.perform(post(SHOPPING_LIST_ITEM_ENDPOINT + existingShoppingListItem.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":" + quantity + "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(existingShoppingListItem.getId().intValue())))
                .andExpect(jsonPath("$.shoppingList.id", is(existingShoppingListItem
                        .getShoppingList().getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(existingShoppingListItem.getItem().getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(quantity.intValue())));

        verify(shoppingListItemService).updateShoppingListItem(eq(existingShoppingListItem.getId()), any(ShoppingListItemUpdateForm.class));
    }

    @Test
    public void updateShoppingListItem_whenShoppingListDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(shoppingListItemService.updateShoppingListItem(anyLong(), any(ShoppingListItemUpdateForm.class)))
                    .thenThrow(new ShoppingListNotFoundException(-1L));

            mvc.perform(post(SHOPPING_LIST_ITEM_ENDPOINT + Long.valueOf(999L))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"quantity\":-1}")
                    .accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void deleteShoppingListItem_deletesShoppingListItem() throws Exception {
        Long shoppingListItemId = 1L;

        mvc.perform(delete(SHOPPING_LIST_ITEM_ENDPOINT + shoppingListItemId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(shoppingListItemService).deleteShoppingListItem(eq(shoppingListItemId));
    }

    @Test
    public void deleteShoppingListItem_whenShoppingListItemDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            doThrow(new Exception()).when(shoppingListItemService).deleteShoppingListItem(anyLong());

            mvc.perform(delete(SHOPPING_LIST_ITEM_ENDPOINT + Long.valueOf(999L))
                    .accept(MediaType.APPLICATION_JSON));
        });
    }
}
