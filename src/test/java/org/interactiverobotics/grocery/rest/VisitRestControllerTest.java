/*
 * VisitRestControllerTest.java
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
import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.exception.ShopNotFoundException;
import org.interactiverobotics.grocery.exception.VisitNotFoundException;
import org.interactiverobotics.grocery.service.VisitService;
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

import java.util.ArrayList;
import java.util.Collections;
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
 * Visit REST controller test.
 * Tests Controller with mocked Service.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitRestController.class)
@ImportAutoConfiguration(JsonConfiguration.class)
public class VisitRestControllerTest {

    private static final String VISIT_ENDPOINT = "/api/v1/visit/";
    private static final String VISIT_SHOP_ENDPOINT = "/api/v1/visit/shop/";
    private static final String SHOP_ID_SELECTOR = "$.shop.id";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VisitService visitService;

    private Shop shop;


    @BeforeEach
    public void setUp() {
        shop = new Shop(1L, "test-shop");
    }


    @Test
    public void getVisits_returnsVisits() throws Exception {
        List<Visit> existingVisits = List.of(new Visit(1L, shop), new Visit(2L, shop));
        when(visitService.getVisits()).thenReturn(existingVisits);

        mvc.perform(get(VISIT_ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(existingVisits.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].shop.id", is(existingVisits.get(0).getShop().getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(existingVisits.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].shop.id", is(existingVisits.get(1).getShop().getId().intValue())));
    }

    @Test
    public void getVisitsPage_returnsPageOfVisits() throws Exception {
        List<Visit> existingVisits = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingVisits.add(new Visit(i, shop));
        }

        when(visitService.getVisits(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingVisits, pageable, existingVisits.size());
        });

        mvc.perform(get(VISIT_ENDPOINT + "list?page=1&size=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(existingVisits.size())))
                .andExpect(jsonPath("$.totalPages", is(10)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    public void getVisitById_returnsVisit() throws Exception {
        Visit existingVisit = new Visit(1L, shop);
        when(visitService.getVisitById(existingVisit.getId())).thenReturn(existingVisit);

        mvc.perform(get(VISIT_ENDPOINT + existingVisit.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(existingVisit.getId().intValue())))
                .andExpect(jsonPath(SHOP_ID_SELECTOR, is(existingVisit.getShop().getId().intValue())));
    }

    @Test
    public void getVisitById_whenVisitDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(visitService.getVisitById(any())).thenThrow(new VisitNotFoundException(-1L));

            mvc.perform(get(VISIT_ENDPOINT + Long.valueOf(999L)).accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void getVisitsByShopId_returnsVisit() throws Exception {
        Visit existingVisit = new Visit(1L, shop);
        when(visitService.getVisitsByShopId(shop.getId())).thenReturn(Collections.singletonList(existingVisit));

        mvc.perform(get(VISIT_SHOP_ENDPOINT + shop.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].shop.id", is(existingVisit.getShop().getId().intValue())));
    }

    @Test
    public void getVisitsByShopId_whenShopDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(visitService.getVisitsByShopId(any())).thenThrow(new ShopNotFoundException(-1L));

            mvc.perform(get(VISIT_SHOP_ENDPOINT + Long.valueOf(999L)).accept(MediaType.APPLICATION_JSON));
        });
    }


    public static class CreateVisitAnswer implements Answer<Visit> {

        private final Shop shop;

        public CreateVisitAnswer(final Shop shop) {
            this.shop = shop;
        }

        private Visit visit;

        public Visit getVisit() {
            return visit;
        }

        @Override
        public Visit answer(InvocationOnMock invocation) {
            assertEquals(1, invocation.getArguments().length);

            Long shopId = invocation.getArgument(0);
            assertEquals(shop.getId(), shopId);

            visit = new Visit(1L, shop);
            return visit;
        }
    }


    @Test
    public void createVisit_createsAndReturnsVisit() throws Exception {
        CreateVisitAnswer createVisitAnswer = new CreateVisitAnswer(shop);
        when(visitService.createVisit(any(Long.class))).thenAnswer(createVisitAnswer);

        mvc.perform(post(VISIT_SHOP_ENDPOINT + shop.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath(SHOP_ID_SELECTOR,
                                is(createVisitAnswer.getVisit().getShop().getId().intValue())));

        verify(visitService).createVisit(anyLong());
    }

    @Test
    public void createVisit_whenShopDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(visitService.createVisit(any(Long.class))).thenThrow(new ShopNotFoundException(-1L));

            mvc.perform(post(VISIT_SHOP_ENDPOINT + Long.valueOf(999L)).accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void startVisit_startsAndReturnsVisit() throws Exception {
        Visit existingVisit = new Visit(1L, shop);
        when(visitService.startVisit(existingVisit.getId())).thenReturn(existingVisit);

        mvc.perform(post(VISIT_ENDPOINT + existingVisit.getId() + "/start")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath(SHOP_ID_SELECTOR, is(existingVisit.getShop().getId().intValue())));

        verify(visitService).startVisit(eq(existingVisit.getId()));
    }

    @Test
    public void startVisit_whenVisitDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(visitService.startVisit(any(Long.class))).thenThrow(new VisitNotFoundException(-1L));

            mvc.perform(post(VISIT_ENDPOINT + Long.valueOf(999L) + "/start").accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void completeVisit_completesAndReturnsVisit() throws Exception {
        Visit existingVisit = new Visit(1L, shop);
        when(visitService.completeVisit(existingVisit.getId())).thenReturn(existingVisit);

        mvc.perform(post(VISIT_ENDPOINT + existingVisit.getId() + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(SHOP_ID_SELECTOR, is(existingVisit.getShop().getId().intValue())));

        verify(visitService).completeVisit(eq(existingVisit.getId()));
    }

    @Test
    public void completeVisit_whenVisitDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            when(visitService.completeVisit(any(Long.class))).thenThrow(new VisitNotFoundException(-1L));

            mvc.perform(post(VISIT_ENDPOINT + Long.valueOf(999L) + "/complete").accept(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    public void deleteVisit_deletesVisit() throws Exception {
        Long id = 1L;

        mvc.perform(delete(VISIT_ENDPOINT + id).accept(MediaType.APPLICATION_JSON));

        verify(visitService).deleteVisit(eq(id));
    }

    @Test
    public void deleteVisit_whenVisitDoesNotExist_throwsException() {
        assertThrows(Exception.class, () -> {
            doThrow(new VisitNotFoundException(-1L)).when(visitService).deleteVisit(any());

            mvc.perform(delete(VISIT_ENDPOINT + Long.valueOf(999L)).accept(MediaType.APPLICATION_JSON));
        });
    }
}
