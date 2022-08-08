/*
 * ShopRestControllerTest.java
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

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.exception.ShopNotFoundException;
import org.interactiverobotics.grocery.form.ShopForm;
import org.interactiverobotics.grocery.service.ShopService;
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
 * Shop REST controller test.
 * Tests Controller with mocked Service.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(ShopRestController.class)
public class ShopRestControllerTest {

    private static final String SHOP_ENDPOINT = "/api/v1/shop/";
    private static final String ID_SELECTOR = "$.id";
    private static final String NAME_SELECTOR = "$.name";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ShopService shopService;


    @Test
    public void getShops_returnsShops() throws Exception {
        List<Shop> existingShops = List.of(
                Shop.builder().id(1L).name("test-shop-1").build(),
                Shop.builder().id(2L).name("test-shop-2").build());
        when(shopService.getShops()).thenReturn(existingShops);

        mvc.perform(get(SHOP_ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(existingShops.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(existingShops.get(0).getName())))
                .andExpect(jsonPath("$[1].id", is(existingShops.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(existingShops.get(1).getName())));
    }

    @Test
    public void getShopsPage_returnsPageOfShops() throws Exception {
        List<Shop> existingShops = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShops.add(Shop.builder().id(i).name("test-shop-" + i).build());
        }

        when(shopService.getShops(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingShops, pageable, existingShops.size());
        });

        mvc.perform(get(SHOP_ENDPOINT + "list?page=1&size=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(existingShops.size())))
                .andExpect(jsonPath("$.totalPages", is(10)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    public void getShopById_returnsShop() throws Exception {
        Shop existingShop = Shop.builder().id(1L).name("test-shop").build();
        when(shopService.getShopById(existingShop.getId())).thenReturn(existingShop);

        mvc.perform(get(SHOP_ENDPOINT + existingShop.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(existingShop.getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(existingShop.getName())));
    }

    @Test
    public void getShopById_whenShopDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(shopService.getShopById(any())).thenThrow(new ShopNotFoundException(-1L));

            mvc.perform(get(SHOP_ENDPOINT + Long.valueOf(999L)).accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void getShopByName_returnsShop() throws Exception {
        Shop existingShop = Shop.builder().id(1L).name("test-shop").build();
        when(shopService.getShopByName(existingShop.getName())).thenReturn(existingShop);

        mvc.perform(get(SHOP_ENDPOINT + "search?name=" + existingShop.getName())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(existingShop.getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(existingShop.getName())));
    }

    @Test
    public void getShopByName_whenShopDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(shopService.getShopByName(any())).thenThrow(new ShopNotFoundException(-1L));

            mvc.perform(get(SHOP_ENDPOINT + "search?name=test").accept(MediaType.APPLICATION_JSON));
        });
    }


    public static class CreateShopAnswer implements Answer<Shop> {

        private Shop shop;

        public Shop getShop() {
            return shop;
        }

        @Override
        public Shop answer(InvocationOnMock invocation) {
            assertEquals(1, invocation.getArguments().length);
            ShopForm form = invocation.getArgument(0);
            shop = Shop.builder().id(1L).name(form.getName()).build();
            return shop;
        }
    }


    @Test
    public void createShop_createsAndReturnsShop() throws Exception {
        CreateShopAnswer createShopAnswer = new CreateShopAnswer();
        when(shopService.createShop(any(ShopForm.class))).then(createShopAnswer);

        mvc.perform(post(SHOP_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test-shop\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(createShopAnswer.getShop().getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(createShopAnswer.getShop().getName())));

        verify(shopService).createShop(any(ShopForm.class));
    }


    public static class UpdateShopAnswer implements Answer<Shop> {

        private final Shop shop;

        public UpdateShopAnswer(Shop shop) {
            this.shop = shop;
        }

        public Shop getShop() {
            return shop;
        }

        @Override
        public Shop answer(InvocationOnMock invocation) {
            assertEquals(2, invocation.getArguments().length);

            Long id = invocation.getArgument(0);
            assertEquals(shop.getId(), id);

            ShopForm form = invocation.getArgument(1);
            shop.setName(form.getName());

            return shop;
        }
    }


    @Test
    public void updateShop_updatesAndReturnsShop() throws Exception {
        Shop existingShop = Shop.builder().id(1L).name("test-shop").build();
        UpdateShopAnswer updateShopAnswer = new UpdateShopAnswer(existingShop);
        when(shopService.updateShop(eq(existingShop.getId()), any(ShopForm.class))).then(updateShopAnswer);

        mvc.perform(post(SHOP_ENDPOINT + existingShop.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"updated-test-shop\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_SELECTOR, is(updateShopAnswer.getShop().getId().intValue())))
                .andExpect(jsonPath(NAME_SELECTOR, is(updateShopAnswer.getShop().getName())));

        verify(shopService).updateShop(eq(existingShop.getId()), any(ShopForm.class));
    }

    @Test
    public void updateShop_whenShopDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(shopService.updateShop(any(), any())).thenThrow(new ShopNotFoundException(-1L));

            mvc.perform(post(SHOP_ENDPOINT + Long.valueOf(999L))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"updated-test-shop\"}")
                    .accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void deleteShop_deletesShop() throws Exception {
        Long id = 1L;

        mvc.perform(delete(SHOP_ENDPOINT + id).accept(MediaType.APPLICATION_JSON));

        verify(shopService).deleteShop(eq(id));
    }

    @Test
    public void deleteShop_whenShopDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            doThrow(new ShopNotFoundException(-1L)).when(shopService).deleteShop(any());

            mvc.perform(delete(SHOP_ENDPOINT + Long.valueOf(999L)).accept(MediaType.APPLICATION_JSON));
        });
    }
}
