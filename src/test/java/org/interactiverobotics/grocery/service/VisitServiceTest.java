/*
 * VisitServiceTest.java
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

package org.interactiverobotics.grocery.service;

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.exception.ShopNotFoundException;
import org.interactiverobotics.grocery.exception.VisitNotFoundException;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.interactiverobotics.grocery.repository.VisitRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Visit service test.
 */
@RunWith(SpringRunner.class)
public class VisitServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private ShopRepository shopRepository;

    private VisitService visitService;

    private Shop shop;


    /**
     * Initializes test.
     */
    @Before
    public void setUp() throws Exception {

        visitService = new VisitService(visitRepository, shopRepository);

        shop = new Shop(1L, "test-shop");
    }


    @Test
    public void testGetVisits() {

        final List<Visit> existingVisits = Arrays.asList(new Visit(1L, shop), new Visit(2L, shop));
        when(visitRepository.findAll()).thenReturn(existingVisits);

        final List<Visit> visits = visitService.getVisits();

        assertEquals(existingVisits, visits);
    }

    @Test
    public void testGetVisitsPage() {

        final List<Visit> existingVisits = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingVisits.add(new Visit(i, shop));
        }

        when(visitRepository.findAll(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            final Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingVisits, pageable, existingVisits.size());
        });

        final Page<Visit> visits = visitService.getVisits(PageRequest.of(0, 10));

        assertEquals(existingVisits.size(), visits.getTotalElements());
        assertEquals(10, visits.getTotalPages());
    }

    @Test
    public void testGetVisitById() {

        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findById(existingVisit.getId())).thenReturn(Optional.of(existingVisit));

        final Visit visit = visitService.getVisitById(existingVisit.getId());

        assertEquals(existingVisit, visit);
    }

    @Test(expected = VisitNotFoundException.class)
    public void testGetNotExistingVisitById() {

        when(visitRepository.findById(any())).thenReturn(Optional.empty());

        visitService.getVisitById(999L);
    }

    @Test
    public void testGetVisitsByShopId() {

        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findAllByShop(shop)).thenReturn(Collections.singletonList(existingVisit));

        when(shopRepository.findById(shop.getId())).thenReturn(Optional.of(shop));

        final List<Visit> visits = visitService.getVisitsByShopId(shop.getId());

        assertEquals(1, visits.size());
        assertEquals(existingVisit, visits.get(0));
    }

    @Test(expected = ShopNotFoundException.class)
    public void testGetVisitsByNotExistingShopId() {

        when(shopRepository.findById(any())).thenReturn(Optional.empty());

        visitService.getVisitsByShopId(999L);
    }

    @Test
    public void testGetVisitsByShopName() {

        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findAllByShop(shop)).thenReturn(Collections.singletonList(existingVisit));

        when(shopRepository.findOneByName(shop.getName())).thenReturn(shop);

        final List<Visit> visits = visitService.getVisitsByShopName(shop.getName());

        assertEquals(1, visits.size());
        assertEquals(existingVisit, visits.get(0));
    }

    @Test(expected = ShopNotFoundException.class)
    public void testGetVisitsByNotExistingShopName() {

        when(shopRepository.findOneByName(any())).thenReturn(null);

        visitService.getVisitsByShopName("test-shop");
    }

    @Test
    public void testGetVisitsByShop() {

        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findAllByShop(shop)).thenReturn(Collections.singletonList(existingVisit));

        final List<Visit> visits = visitService.getVisitsByShop(shop);

        assertEquals(1, visits.size());
        assertEquals(existingVisit, visits.get(0));
    }


    public static class SaveAndReturnVisitAnswer implements Answer<Visit> {

        private Visit visit;

        public Visit getVisit() {
            return visit;
        }

        @Override
        public Visit answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            visit = invocation.getArgument(0);
            if (visit.getId() == null) {
                visit.setId(1L);
            }
            return visit;
        }
    }


    @Test
    public void testCreateVisit1() {

        final SaveAndReturnVisitAnswer saveAndReturnVisitAnswer = new SaveAndReturnVisitAnswer();
        when(visitRepository.save(any(Visit.class))).then(saveAndReturnVisitAnswer);

        when(shopRepository.findById(shop.getId())).thenReturn(Optional.of(shop));

        final Visit visit = visitService.createVisit(shop.getId());

        // Check that Service returns what was saved
        final Visit savedVisit = saveAndReturnVisitAnswer.getVisit();
        assertEquals(savedVisit, visit);

        // Check response content
        assertEquals(shop, visit.getShop());
    }

    @Test(expected = ShopNotFoundException.class)
    public void testCreateVisitForNotExistingShopId() {

        when(shopRepository.findById(any())).thenReturn(Optional.empty());

        visitService.createVisit(999L);
    }

    @Test
    public void testCreateVisit2() {

        final SaveAndReturnVisitAnswer saveAndReturnVisitAnswer = new SaveAndReturnVisitAnswer();
        when(visitRepository.save(any(Visit.class))).then(saveAndReturnVisitAnswer);

        when(shopRepository.findOneByName(shop.getName())).thenReturn(shop);

        final Visit visit = visitService.createVisit(shop.getName());

        // Check that Service returns what was saved
        final Visit savedVisit = saveAndReturnVisitAnswer.getVisit();
        assertEquals(savedVisit, visit);

        // Check response content
        assertEquals(shop, visit.getShop());
    }

    @Test(expected = ShopNotFoundException.class)
    public void testCreateVisitForNotExistingShopName() {

        when(shopRepository.findOneByName(any())).thenReturn(null);

        visitService.createVisit("test-shop");
    }

    @Test
    public void testCreateVisit3() {

        final SaveAndReturnVisitAnswer saveAndReturnVisitAnswer = new SaveAndReturnVisitAnswer();
        when(visitRepository.save(any(Visit.class))).then(saveAndReturnVisitAnswer);

        final Visit visit = visitService.createVisit(shop);

        // Check that Service returns what was saved
        final Visit savedVisit = saveAndReturnVisitAnswer.getVisit();
        assertEquals(savedVisit, visit);

        // Check response content
        assertEquals(shop, visit.getShop());
    }

    @Test
    public void testStartVisit() {

        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findById(existingVisit.getId())).thenReturn(Optional.of(existingVisit));

        final SaveAndReturnVisitAnswer saveAndReturnVisitAnswer = new SaveAndReturnVisitAnswer();
        when(visitRepository.save(any(Visit.class))).then(saveAndReturnVisitAnswer);

        final Visit visit = visitService.startVisit(existingVisit.getId());

        // Check that Service returns what was saved
        final Visit savedVisit = saveAndReturnVisitAnswer.getVisit();
        assertEquals(savedVisit, visit);

        // Check response content
        assertEquals(existingVisit.getId(), visit.getId());
        assertNotNull(visit.getStarted());
    }

    @Test(expected = VisitNotFoundException.class)
    public void testStartNotExistingVisit() {

        when(visitRepository.findById(any())).thenReturn(Optional.empty());

        visitService.startVisit(999L);
    }

    @Test
    public void testCompleteVisit() {

        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findById(existingVisit.getId())).thenReturn(Optional.of(existingVisit));

        final SaveAndReturnVisitAnswer saveAndReturnVisitAnswer = new SaveAndReturnVisitAnswer();
        when(visitRepository.save(any(Visit.class))).then(saveAndReturnVisitAnswer);

        final Visit visit = visitService.completeVisit(existingVisit.getId());

        // Check that Service returns what was saved
        final Visit savedVisit = saveAndReturnVisitAnswer.getVisit();
        assertEquals(savedVisit, visit);

        // Check response content
        assertEquals(existingVisit.getId(), visit.getId());
        assertNotNull(visit.getCompleted());
    }

    @Test(expected = VisitNotFoundException.class)
    public void testCompleteNotExistingVisit() {

        when(visitRepository.findById(any())).thenReturn(Optional.empty());

        visitService.completeVisit(999L);
    }

    @Test
    public void testDeleteVisit() {

        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findById(existingVisit.getId())).thenReturn(Optional.of(existingVisit));

        visitService.deleteVisit(existingVisit.getId());

        verify(visitRepository).delete(eq(existingVisit));
    }

    @Test(expected = VisitNotFoundException.class)
    public void testDeleteNotExistingVisit() {

        when(visitRepository.findById(any())).thenReturn(Optional.empty());

        visitService.deleteVisit(999L);
    }
}
