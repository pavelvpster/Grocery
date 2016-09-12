/*
 * PurchaseRestControllerTest.java
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

import org.interactiverobotics.grocery.configuration.JsonConfiguration;
import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.Purchase;
import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.service.PurchaseService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

/**
 * Purchase REST controller test.
 * Tests Controller with mocked Service.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(PurchaseRestController.class)
@ImportAutoConfiguration(JsonConfiguration.class)
public class PurchaseRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PurchaseService purchaseService;

    private Shop shop;

    private Visit visit;

    private Item item;


    @Before
    public void setUp() throws Exception {

        shop = new Shop(1L, "test-shop");

        visit = new Visit(1L, shop);

        item = new Item(1L, "test-item");
    }


    public static class BuyItemAnswer implements Answer<Purchase> {

        private final Visit visit;

        private final Item item;

        public BuyItemAnswer(final Visit visit, final Item item) {
            this.visit = visit;
            this.item = item;
        }

        private Purchase purchase;

        public Purchase getPurchase() {
            return purchase;
        }

        @Override
        public Purchase answer(InvocationOnMock invocation) throws Throwable {

            assertEquals(4, invocation.getArguments().length);

            final Long visitId = invocation.getArgumentAt(0, Long.class);
            assertEquals(visit.getId(), visitId);

            final Long itemId = invocation.getArgumentAt(1, Long.class);
            assertEquals(item.getId(), itemId);

            final Long quantity = invocation.getArgumentAt(2, Long.class);

            final BigDecimal price = invocation.getArgumentAt(3, BigDecimal.class);

            purchase = new Purchase();
            purchase.setId(1L);
            purchase.setVisit(visit);
            purchase.setItem(item);
            purchase.setQuantity(quantity);
            purchase.setPrice(price);

            return purchase;
        }
    }


    @Test
    public void testBuyItem() throws Exception {

        final BuyItemAnswer buyItemAnswer = new BuyItemAnswer(visit, item);
        when(purchaseService.buyItem(eq(visit.getId()), eq(item.getId()), anyLong(), anyObject()))
                .thenAnswer(buyItemAnswer);

        mvc.perform(post("/api/v1/purchase/" + visit.getId() + "/buy/" + item.getId())
                .param("quantity", "1")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.visit.id", is(visit.getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(item.getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(1)));
    }

    @Test(expected = Exception.class)
    public void testBuyItemForWrongParams() throws Exception {

        when(purchaseService.buyItem(anyLong(), anyLong(), anyLong(), anyObject())).thenThrow(new Exception());

        mvc.perform(post("/api/v1/purchase/" + new Long(999L) + "/buy/" + new Long(999L))
                .param("quantity", "-1")
                .accept(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testBuyItemSetPrice() throws Exception {

        final BuyItemAnswer buyItemAnswer = new BuyItemAnswer(visit, item);
        when(purchaseService.buyItem(eq(visit.getId()), eq(item.getId()), anyLong(), anyObject()))
                .thenAnswer(buyItemAnswer);

        mvc.perform(post("/api/v1/purchase/" + visit.getId() + "/buy/" + item.getId())
                .param("quantity", "1")
                .param("price", "10")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.price", is(10)));
    }


    public static class ReturnItemAnswer implements Answer<Purchase> {

        private final Visit visit;

        private final Item item;

        public ReturnItemAnswer(final Visit visit, final Item item) {
            this.visit = visit;
            this.item = item;
        }

        private Purchase purchase;

        public Purchase getPurchase() {
            return purchase;
        }

        @Override
        public Purchase answer(InvocationOnMock invocation) throws Throwable {

            assertEquals(3, invocation.getArguments().length);

            final Long visitId = invocation.getArgumentAt(0, Long.class);
            assertEquals(visit.getId(), visitId);

            final Long itemId = invocation.getArgumentAt(1, Long.class);
            assertEquals(item.getId(), itemId);

            final Long quantity = invocation.getArgumentAt(2, Long.class);

            purchase = new Purchase();
            purchase.setId(1L);
            purchase.setVisit(visit);
            purchase.setItem(item);
            purchase.setQuantity(quantity);

            return purchase;
        }
    }


    @Test
    public void testReturnItem() throws Exception {

        final ReturnItemAnswer returnItemAnswer = new ReturnItemAnswer(visit, item);
        when(purchaseService.returnItem(eq(visit.getId()), eq(item.getId()), anyLong()))
                .thenAnswer(returnItemAnswer);

        mvc.perform(post("/api/v1/purchase/" + visit.getId() + "/return/" + item.getId())
                .param("quantity", "1")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.visit.id", is(visit.getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(item.getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(1)));
    }

    @Test(expected = Exception.class)
    public void testReturnItemForWrongParams() throws Exception {

        when(purchaseService.returnItem(anyLong(), anyLong(), anyLong())).thenThrow(new Exception());

        mvc.perform(post("/api/v1/purchase/" + new Long(999L) + "/return/" + new Long(999L))
                .param("quantity", "-1")
                .accept(MediaType.APPLICATION_JSON_UTF8));
    }


    public static class UpdatePriceAnswer implements Answer<Purchase> {

        private final Purchase purchase;

        public UpdatePriceAnswer(final Purchase purchase) {
            this.purchase = purchase;
        }

        public Purchase getPurchase() {
            return purchase;
        }

        @Override
        public Purchase answer(InvocationOnMock invocation) throws Throwable {

            assertEquals(3, invocation.getArguments().length);

            final Long visitId = invocation.getArgumentAt(0, Long.class);
            assertEquals(purchase.getVisit().getId(), visitId);

            final Long itemId = invocation.getArgumentAt(1, Long.class);
            assertEquals(purchase.getItem().getId(), itemId);

            final BigDecimal price = invocation.getArgumentAt(2, BigDecimal.class);
            purchase.setPrice(price);

            return purchase;
        }
    }


    @Test
    public void testUpdatePrice() throws Exception {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 1L, BigDecimal.valueOf(10L));
        final UpdatePriceAnswer updatePriceAnswer = new UpdatePriceAnswer(existingPurchase);
        when(purchaseService.updatePrice(eq(visit.getId()), eq(item.getId()), anyObject()))
                .thenAnswer(updatePriceAnswer);

        mvc.perform(post("/api/v1/purchase/" + visit.getId() + "/price/" + item.getId())
                .param("price", "10")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.visit.id", is(existingPurchase.getVisit().getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(existingPurchase.getItem().getId().intValue())))
                .andExpect(jsonPath("$.price", is(existingPurchase.getPrice().intValue())));
    }

    @Test(expected = Exception.class)
    public void testUpdatePriceForWrongParams() throws Exception {

        when(purchaseService.updatePrice(anyLong(), anyLong(), anyObject())).thenThrow(new Exception());

        mvc.perform(post("/api/v1/purchase/" + new Long(999L) + "/price/" + new Long(999L))
                .param("price", "-1")
                .accept(MediaType.APPLICATION_JSON_UTF8));
    }

}
