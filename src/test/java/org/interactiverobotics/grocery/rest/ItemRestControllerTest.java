/*
 * ItemRestControllerTest.java
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
import org.interactiverobotics.grocery.exception.ItemNotFoundException;
import org.interactiverobotics.grocery.form.ItemForm;
import org.interactiverobotics.grocery.service.ItemService;
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
 * Item REST controller test.
 * Tests Controller with mocked Service.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(ItemRestController.class)
public class ItemRestControllerTest {

    private static final String ITEM_ENDPOINT = "/api/v1/item/";
    private static final String ID_SELECTOR = "$.id";
    private static final String NAME_SELECTOR = "$.name";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemService itemService;


    @Test
    public void getItems_returnsItems() throws Exception {
        List<Item> existingItems = List.of(
                Item.builder().id(1L).name("test-item-1").build(),
                Item.builder().id(2L).name("test-item-2").build());
        when(itemService.getItems()).thenReturn(existingItems);

        mvc.perform(get(ITEM_ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(existingItems.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(existingItems.get(0).getName())))
                .andExpect(jsonPath("$[1].id", is(existingItems.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(existingItems.get(1).getName())));
    }

    @Test
    public void getItemsPage_returnsPageOfItems() throws Exception {
        List<Item> existingItems = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingItems.add(Item.builder().id(i).name("test-item-" + i).build());
        }

        when(itemService.getItems(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingItems, pageable, existingItems.size());
        });

        mvc.perform(get(ITEM_ENDPOINT + "list?page=1&size=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(existingItems.size())))
                .andExpect(jsonPath("$.totalPages", is(10)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    public void getItemById_returnsItem() throws Exception {
        Item existingItem = Item.builder().id(1L).name("test-item").build();
        when(itemService.getItemById(existingItem.getId())).thenReturn(existingItem);

        mvc.perform(get(ITEM_ENDPOINT + existingItem.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(existingItem.getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(existingItem.getName())));
    }

    @Test
    public void getItemById_whenItemDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(itemService.getItemById(any())).thenThrow(new ItemNotFoundException(-1L));

            mvc.perform(get(ITEM_ENDPOINT + Long.valueOf(999L)).accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void getItemByName_returnsItem() throws Exception {
        Item existingItem = Item.builder().id(1L).name("test-item").build();
        when(itemService.getItemByName(existingItem.getName())).thenReturn(existingItem);

        mvc.perform(get(ITEM_ENDPOINT + "search?name=" + existingItem.getName())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(existingItem.getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(existingItem.getName())));
    }

    @Test
    public void getItemByName_whenItemDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(itemService.getItemByName(any())).thenThrow(new ItemNotFoundException(-1L));

            mvc.perform(get(ITEM_ENDPOINT + "search?name=test").accept(MediaType.APPLICATION_JSON));
        });
    }


    public static class CreateItemAnswer implements Answer<Item> {

        private Item item;

        public Item getItem() {
            return item;
        }

        @Override
        public Item answer(InvocationOnMock invocation) {
            assertEquals(1, invocation.getArguments().length);
            ItemForm form = invocation.getArgument(0);
            item = Item.builder().id(1L).name(form.getName()).build();
            return item;
        }
    }


    @Test
    public void createItem_createsAndReturnsItem() throws Exception {
        CreateItemAnswer createItemAnswer = new CreateItemAnswer();
        when(itemService.createItem(any(ItemForm.class))).then(createItemAnswer);

        mvc.perform(post(ITEM_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test-item\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(createItemAnswer.getItem().getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(createItemAnswer.getItem().getName())));

        verify(itemService).createItem(any(ItemForm.class));
    }


    public static class UpdateItemAnswer implements Answer<Item> {

        private final Item item;

        public UpdateItemAnswer(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }

        @Override
        public Item answer(InvocationOnMock invocation) {
            assertEquals(2, invocation.getArguments().length);

            Long id = invocation.getArgument(0);
            assertEquals(item.getId(), id);

            ItemForm form = invocation.getArgument(1);
            item.setName(form.getName());

            return item;
        }
    }


    @Test
    public void updateItem_updatesAndReturnsItem() throws Exception {
        Item existingItem = Item.builder().id(1L).name("test-item").build();
        UpdateItemAnswer updateItemAnswer = new UpdateItemAnswer(existingItem);
        when(itemService.updateItem(eq(existingItem.getId()), any(ItemForm.class))).then(updateItemAnswer);

        mvc.perform(post(ITEM_ENDPOINT + existingItem.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"updated-test-item\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(updateItemAnswer.getItem().getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(updateItemAnswer.getItem().getName())));

        verify(itemService).updateItem(eq(existingItem.getId()), any(ItemForm.class));
    }

    @Test
    public void updateItem_whenItemDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(itemService.updateItem(any(), any())).thenThrow(new ItemNotFoundException(-1L));

            mvc.perform(post(ITEM_ENDPOINT + Long.valueOf(999L))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"updated-test-item\"}")
                    .accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void deleteItem_deletesItem() throws Exception {
        Long id = 1L;

        mvc.perform(delete(ITEM_ENDPOINT + id).accept(MediaType.APPLICATION_JSON));

        verify(itemService).deleteItem(eq(id));
    }

    @Test
    public void deleteItem_whenItemDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            doThrow(new ItemNotFoundException(-1L)).when(itemService).deleteItem(any());

            mvc.perform(delete(ITEM_ENDPOINT + Long.valueOf(999L)).accept(MediaType.APPLICATION_JSON));
        });
    }
}
