/*
 * PurchaseRestControllerTest.java
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

import org.interactiverobotics.grocery.configuration.JsonConfiguration;
import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.Purchase;
import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.service.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Purchase REST controller test.
 * Tests Controller with mocked Service.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(PurchaseRestController.class)
@ImportAutoConfiguration(JsonConfiguration.class)
public class PurchaseRestControllerTest {

    private static final String PURCHASE_ENDPOINT = "/api/v1/purchase/";
    private static final String QUANTITY_PARAM = "quantity";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PurchaseService purchaseService;

    private Visit visit;

    private Item item;


    /**
     * Initializes test.
     */
    @BeforeEach
    public void setUp() {
        visit = new Visit(1L, new Shop(1L, "test-shop"));
        item = new Item(1L, "test-item");
    }


    @Test
    public void getNotPurchasedItems_returnsItems() throws Exception {
        List<Item> notPurchasedItems = List.of(new Item(1L, "test-item-1"), new Item(2L, "test-item-2"));
        when(purchaseService.getNotPurchasedItems(visit.getId())).thenReturn(notPurchasedItems);

        mvc.perform(get(PURCHASE_ENDPOINT + visit.getId() + "/not_purchased_items")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(notPurchasedItems.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(notPurchasedItems.get(0).getName())))
                .andExpect(jsonPath("$[1].id", is(notPurchasedItems.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(notPurchasedItems.get(1).getName())));
    }

    @Test
    public void getNotPurchasedItems_whenVisitDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(purchaseService.getNotPurchasedItems(anyLong())).thenThrow(new Exception());

            mvc.perform(get(PURCHASE_ENDPOINT + Long.valueOf(999L) + "/not_purchased_items")
                            .accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void getPurchasesPage_returnsPageOfPurchases() throws Exception {
        List<Purchase> existingPurchases = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingPurchases.add(new Purchase(i, visit, item, 1L, null));
        }

        when(purchaseService.getPurchases(any(Pageable.class), eq(visit.getId()))).thenAnswer(invocation -> {
            assertEquals(2, invocation.getArguments().length);
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingPurchases, pageable, existingPurchases.size());
        });

        mvc.perform(get(PURCHASE_ENDPOINT + visit.getId() + "/list?page=1&size=10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(existingPurchases.size())))
                .andExpect(jsonPath("$.totalPages", is(10)))
                .andExpect(jsonPath("$.size", is(10)));
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
        public Purchase answer(InvocationOnMock invocation) {
            assertEquals(4, invocation.getArguments().length);

            Long visitId = invocation.getArgument(0);
            assertEquals(visit.getId(), visitId);

            Long itemId = invocation.getArgument(1);
            assertEquals(item.getId(), itemId);

            Long quantity = invocation.getArgument(2);
            BigDecimal price = invocation.getArgument(3);

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
    public void buyItem_createsAndReturnsPurchase() throws Exception {
        BuyItemAnswer buyItemAnswer = new BuyItemAnswer(visit, item);
        when(purchaseService.buyItem(eq(visit.getId()), eq(item.getId()), any(Long.class), any()))
                .thenAnswer(buyItemAnswer);

        mvc.perform(post(PURCHASE_ENDPOINT + visit.getId() + "/buy/" + item.getId())
                .param(QUANTITY_PARAM, "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.visit.id", is(visit.getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(item.getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(1)));

        verify(purchaseService).buyItem(eq(visit.getId()), eq(item.getId()), eq(1L), isNull());
    }

    @Test
    public void buyItem_whenVisitAndItemDoNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(purchaseService.buyItem(any(Long.class), any(Long.class), any(Long.class), any(BigDecimal.class)))
                    .thenThrow(new Exception());

            mvc.perform(post(PURCHASE_ENDPOINT + Long.valueOf(999L) + "/buy/" + Long.valueOf(999L))
                    .param(QUANTITY_PARAM, "-1")
                    .accept(MediaType.APPLICATION_JSON));
        });
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
        public Purchase answer(InvocationOnMock invocation) {
            assertEquals(3, invocation.getArguments().length);

            Long visitId = invocation.getArgument(0);
            assertEquals(visit.getId(), visitId);

            Long itemId = invocation.getArgument(1);
            assertEquals(item.getId(), itemId);

            Long quantity = invocation.getArgument(2);

            purchase = new Purchase();
            purchase.setId(1L);
            purchase.setVisit(visit);
            purchase.setItem(item);
            purchase.setQuantity(quantity);
            return purchase;
        }
    }


    @Test
    public void returnItem_updatesAndReturnsPurchase() throws Exception {
        ReturnItemAnswer returnItemAnswer = new ReturnItemAnswer(visit, item);
        when(purchaseService.returnItem(eq(visit.getId()), eq(item.getId()), any(Long.class)))
                .thenAnswer(returnItemAnswer);

        mvc.perform(post(PURCHASE_ENDPOINT + visit.getId() + "/return/" + item.getId())
                .param(QUANTITY_PARAM, "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.visit.id", is(visit.getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(item.getId().intValue())))
                .andExpect(jsonPath("$.quantity", is(1)));

        verify(purchaseService).returnItem(eq(visit.getId()), eq(item.getId()), eq(1L));
    }

    @Test
    public void returnItem_whenVisitAndItemDoNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(purchaseService.returnItem(any(Long.class), any(Long.class), any(Long.class))).thenThrow(new Exception());

            mvc.perform(post(PURCHASE_ENDPOINT + Long.valueOf(999L) + "/return/" + Long.valueOf(999L))
                    .param(QUANTITY_PARAM, "-1")
                    .accept(MediaType.APPLICATION_JSON));
        });
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
        public Purchase answer(InvocationOnMock invocation) {
            assertEquals(3, invocation.getArguments().length);

            Long visitId = invocation.getArgument(0);
            assertEquals(purchase.getVisit().getId(), visitId);

            Long itemId = invocation.getArgument(1);
            assertEquals(purchase.getItem().getId(), itemId);

            BigDecimal price = invocation.getArgument(2);
            purchase.setPrice(price);

            return purchase;
        }
    }


    @Test
    public void updatePrice_updatesAndReturnsPurchase() throws Exception {
        Purchase existingPurchase = new Purchase(1L, visit, item, 1L, BigDecimal.valueOf(10L));
        UpdatePriceAnswer updatePriceAnswer = new UpdatePriceAnswer(existingPurchase);
        when(purchaseService.updatePrice(eq(visit.getId()), eq(item.getId()), any(BigDecimal.class)))
                .thenAnswer(updatePriceAnswer);

        mvc.perform(post(PURCHASE_ENDPOINT + visit.getId() + "/price/" + item.getId())
                .param("price", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.visit.id", is(existingPurchase.getVisit().getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(existingPurchase.getItem().getId().intValue())))
                .andExpect(jsonPath("$.price", is(existingPurchase.getPrice().intValue())));

        verify(purchaseService).updatePrice(eq(visit.getId()), eq(item.getId()), eq(BigDecimal.valueOf(10L)));
    }

    @Test
    public void updatePrice_whenVisitAndItemDoNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(purchaseService.updatePrice(any(Long.class), any(Long.class), any(BigDecimal.class)))
                    .thenThrow(new Exception());

            mvc.perform(post(PURCHASE_ENDPOINT + Long.valueOf(999L) + "/price/" + Long.valueOf(999L))
                    .param("price", "-1")
                    .accept(MediaType.APPLICATION_JSON));
        });
    }
}
