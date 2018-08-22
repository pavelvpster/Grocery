/*
 * ItemRestControllerTest.java
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

package org.interactiverobotics.grocery.rest;

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.exception.ItemNotFoundException;
import org.interactiverobotics.grocery.form.ItemForm;
import org.interactiverobotics.grocery.service.ItemService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
 * Item REST controller test.
 * Tests Controller with mocked Service.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ItemRestController.class)
public class ItemRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemService itemService;


    @Test
    public void testGetItems() throws Exception {

        final List<Item> existingItems = Arrays.asList(new Item(1L, "test-item-1"), new Item(2L, "test-item-2"));
        when(itemService.getItems()).thenReturn(existingItems);

        mvc.perform(get("/api/v1/item/").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(existingItems.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(existingItems.get(0).getName())))
                .andExpect(jsonPath("$[1].id", is(existingItems.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(existingItems.get(1).getName())));
    }

    @Test
    public void testGetItemsPage() throws Exception {

        final List<Item> existingItems = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingItems.add(new Item(i, "test-item-" + i));
        }

        when(itemService.getItems(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            final Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingItems, pageable, existingItems.size());
        });

        mvc.perform(get("/api/v1/item/list?page=1&size=10").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.totalElements", is(existingItems.size())))
                .andExpect(jsonPath("$.totalPages", is(10)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    public void testGetItemById() throws Exception {

        final Item existingItem = new Item(1L, "test-item");
        when(itemService.getItemById(existingItem.getId())).thenReturn(existingItem);

        mvc.perform(get("/api/v1/item/" + existingItem.getId()).accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(existingItem.getId().intValue())))
                .andExpect(jsonPath("$.name", is(existingItem.getName())));
    }

    @Test(expected = Exception.class)
    public void testGetNotExistingItemById() throws Exception {

        when(itemService.getItemById(any())).thenThrow(new ItemNotFoundException(-1L));

        mvc.perform(get("/api/v1/item/" + new Long(999L)).accept(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testGetItemByName() throws Exception {

        final Item existingItem = new Item(1L, "test-item");
        when(itemService.getItemByName(existingItem.getName())).thenReturn(existingItem);

        mvc.perform(get("/api/v1/item/search?name=" + existingItem.getName()).accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(existingItem.getId().intValue())))
                .andExpect(jsonPath("$.name", is(existingItem.getName())));
    }

    @Test(expected = Exception.class)
    public void testGetNotExistingItemByName() throws Exception {

        when(itemService.getItemByName(any())).thenThrow(new ItemNotFoundException(-1L));

        mvc.perform(get("/api/v1/item/search?name=test").accept(MediaType.APPLICATION_JSON_UTF8));
    }


    public static class CreateItemAnswer implements Answer<Item> {

        private Item item;

        public Item getItem() {
            return item;
        }

        @Override
        public Item answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            final ItemForm form = invocation.getArgument(0);
            item = new Item(1L, form.getName());
            return item;
        }
    }


    @Test
    public void testCreateItem() throws Exception {

        final CreateItemAnswer createItemAnswer = new CreateItemAnswer();
        when(itemService.createItem(any(ItemForm.class))).then(createItemAnswer);

        mvc.perform(post("/api/v1/item/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"name\":\"test-item\"}")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(createItemAnswer.getItem().getId().intValue())))
                .andExpect(jsonPath("$.name", is(createItemAnswer.getItem().getName())));
    }


    public static class UpdateItemAnswer implements Answer<Item> {

        private final Item item;

        public UpdateItemAnswer(final Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }

        @Override
        public Item answer(InvocationOnMock invocation) throws Throwable {

            assertEquals(2, invocation.getArguments().length);

            final Long id = invocation.getArgument(0);
            assertEquals(item.getId(), id);

            final ItemForm form = invocation.getArgument(1);
            item.setName(form.getName());

            return item;
        }
    }


    @Test
    public void testUpdateItem() throws Exception {

        final Item existingItem = new Item(1L, "test-item");
        final UpdateItemAnswer updateItemAnswer = new UpdateItemAnswer(existingItem);
        when(itemService.updateItem(eq(existingItem.getId()), any(ItemForm.class))).then(updateItemAnswer);

        mvc.perform(post("/api/v1/item/" + existingItem.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"name\":\"updated-test-item\"}")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(updateItemAnswer.getItem().getId().intValue())))
                .andExpect(jsonPath("$.name", is(updateItemAnswer.getItem().getName())));
    }

    @Test(expected = Exception.class)
    public void testUpdateNotExistingItem() throws Exception {

        when(itemService.updateItem(any(), any())).thenThrow(new ItemNotFoundException(-1L));

        mvc.perform(post("/api/v1/item/" + new Long(999L))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"name\":\"updated-test-item\"}")
                .accept(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testDeleteItem() throws Exception {

        final Long id = 1L;

        mvc.perform(delete("/api/v1/item/" + id).accept(MediaType.APPLICATION_JSON_UTF8));

        verify(itemService).deleteItem(eq(id));
    }

    @Test(expected = Exception.class)
    public void testDeleteNotExistingItem() throws Exception {

        doThrow(new ItemNotFoundException(-1L)).when(itemService).deleteItem(any());

        mvc.perform(delete("/api/v1/item/" + new Long(999L)).accept(MediaType.APPLICATION_JSON_UTF8));
    }
}
