/*
 * ShopRestControllerTest.java
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

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.exception.ShopNotFoundException;
import org.interactiverobotics.grocery.form.ShopForm;
import org.interactiverobotics.grocery.service.ShopService;
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
 * Shop REST controller test.
 * Tests Controller with mocked Service.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ShopRestController.class)
public class ShopRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ShopService shopService;


    @Test
    public void testGetShops() throws Exception {

        final List<Shop> existingShops = Arrays.asList(new Shop(1L, "test-shop-1"), new Shop(2L, "test-shop-2"));
        when(shopService.getShops()).thenReturn(existingShops);

        mvc.perform(get("/api/v1/shop/").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(existingShops.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(existingShops.get(0).getName())))
                .andExpect(jsonPath("$[1].id", is(existingShops.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(existingShops.get(1).getName())));
    }

    @Test
    public void testGetShopsPage() throws Exception {

        final List<Shop> existingShops = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShops.add(new Shop(i, "test-shop-" + i));
        }

        when(shopService.getShops(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            final Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingShops, pageable, existingShops.size());
        });

        mvc.perform(get("/api/v1/shop/list?page=1&size=10").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.totalElements", is(existingShops.size())))
                .andExpect(jsonPath("$.totalPages", is(10)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    public void testGetShopById() throws Exception {

        final Shop existingShop = new Shop(1L, "test-shop");
        when(shopService.getShopById(existingShop.getId())).thenReturn(existingShop);

        mvc.perform(get("/api/v1/shop/" + existingShop.getId()).accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(existingShop.getId().intValue())))
                .andExpect(jsonPath("$.name", is(existingShop.getName())));
    }

    @Test(expected = Exception.class)
    public void testGetNotExistingShopById() throws Exception {

        when(shopService.getShopById(any())).thenThrow(new ShopNotFoundException(-1L));

        mvc.perform(get("/api/v1/shop/" + new Long(999L)).accept(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testGetShopByName() throws Exception {

        final Shop existingShop = new Shop(1L, "test-shop");
        when(shopService.getShopByName(existingShop.getName())).thenReturn(existingShop);

        mvc.perform(get("/api/v1/shop/search?name=" + existingShop.getName()).accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(existingShop.getId().intValue())))
                .andExpect(jsonPath("$.name", is(existingShop.getName())));
    }

    @Test(expected = Exception.class)
    public void testGetNotExistingShopByName() throws Exception {

        when(shopService.getShopByName(any())).thenThrow(new ShopNotFoundException(-1L));

        mvc.perform(get("/api/v1/shop/search?name=test").accept(MediaType.APPLICATION_JSON_UTF8));
    }


    public static class CreateShopAnswer implements Answer<Shop> {

        private Shop shop;

        public Shop getShop() {
            return shop;
        }

        @Override
        public Shop answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            final ShopForm form = invocation.getArgument(0);
            shop = new Shop(1L, form.getName());
            return shop;
        }
    }


    @Test
    public void testCreateShop() throws Exception {

        final CreateShopAnswer createShopAnswer = new CreateShopAnswer();
        when(shopService.createShop(any(ShopForm.class))).then(createShopAnswer);

        mvc.perform(post("/api/v1/shop/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"name\":\"test-shop\"}")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(createShopAnswer.getShop().getId().intValue())))
                .andExpect(jsonPath("$.name", is(createShopAnswer.getShop().getName())));
    }


    public static class UpdateShopAnswer implements Answer<Shop> {

        private final Shop shop;

        public UpdateShopAnswer(final Shop shop) {
            this.shop = shop;
        }

        public Shop getShop() {
            return shop;
        }

        @Override
        public Shop answer(InvocationOnMock invocation) throws Throwable {

            assertEquals(2, invocation.getArguments().length);

            final Long id = invocation.getArgument(0);
            assertEquals(shop.getId(), id);

            final ShopForm form = invocation.getArgument(1);
            shop.setName(form.getName());

            return shop;
        }
    }


    @Test
    public void testUpdateShop() throws Exception {

        final Shop existingShop = new Shop(1L, "test-shop");
        final UpdateShopAnswer updateShopAnswer = new UpdateShopAnswer(existingShop);
        when(shopService.updateShop(eq(existingShop.getId()), any(ShopForm.class))).then(updateShopAnswer);

        mvc.perform(post("/api/v1/shop/" + existingShop.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"name\":\"updated-test-shop\"}")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(updateShopAnswer.getShop().getId().intValue())))
                .andExpect(jsonPath("$.name", is(updateShopAnswer.getShop().getName())));
    }

    @Test(expected = Exception.class)
    public void testUpdateNotExistingShop() throws Exception {

        when(shopService.updateShop(any(), any())).thenThrow(new ShopNotFoundException(-1L));

        mvc.perform(post("/api/v1/shop/" + new Long(999L))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"name\":\"updated-test-shop\"}")
                .accept(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testDeleteShop() throws Exception {

        final Long id = 1L;

        mvc.perform(delete("/api/v1/shop/" + id).accept(MediaType.APPLICATION_JSON_UTF8));

        verify(shopService).deleteShop(eq(id));
    }

    @Test(expected = Exception.class)
    public void testDeleteNotExistingShop() throws Exception {

        doThrow(new ShopNotFoundException(-1L)).when(shopService).deleteShop(any());

        mvc.perform(delete("/api/v1/shop/" + new Long(999L)).accept(MediaType.APPLICATION_JSON_UTF8));
    }
}
