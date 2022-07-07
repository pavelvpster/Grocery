/*
 * VisitServiceTest.java
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

package org.interactiverobotics.grocery.service;

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.exception.ShopNotFoundException;
import org.interactiverobotics.grocery.exception.VisitNotFoundException;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.interactiverobotics.grocery.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Visit service test.
 */
@ExtendWith(MockitoExtension.class)
public class VisitServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private VisitService visitService;

    private Shop shop;


    /**
     * Initializes test.
     */
    @BeforeEach
    public void setUp() {
        shop = new Shop(1L, "test-shop");
    }


    @Test
    public void getVisits_returnsVisits() {
        List<Visit> existingVisits = List.of(new Visit(1L, shop), new Visit(2L, shop));
        when(visitRepository.findAll()).thenReturn(existingVisits);

        List<Visit> visits = visitService.getVisits();

        assertEquals(existingVisits, visits);
    }

    @Test
    public void getVisits_givenPageRequest_returnsPageOfVisits() {
        List<Visit> existingVisits = new ArrayList<>();
        for (long i = 0; i < 100; i++) {
            existingVisits.add(new Visit(i, shop));
        }

        when(visitRepository.findAll(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            final Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingVisits, pageable, existingVisits.size());
        });

        Page<Visit> visits = visitService.getVisits(PageRequest.of(0, 10));

        assertEquals(existingVisits.size(), visits.getTotalElements());
        assertEquals(10, visits.getTotalPages());
    }

    @Test
    public void getVisitById_returnsVisit() {
        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findById(existingVisit.getId())).thenReturn(Optional.of(existingVisit));

        Visit visit = visitService.getVisitById(existingVisit.getId());

        assertEquals(existingVisit, visit);
    }

    @Test
    public void getVisitById_whenVisitDoesNotExist_throwsException() {
        assertThrows(VisitNotFoundException.class, () -> {
            when(visitRepository.findById(any())).thenReturn(Optional.empty());

            visitService.getVisitById(999L);
        });
    }

    @Test
    public void getVisitsByShopId_returnsVisits() {
        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findAllByShop(shop)).thenReturn(Collections.singletonList(existingVisit));

        when(shopRepository.findById(shop.getId())).thenReturn(Optional.of(shop));

        List<Visit> visits = visitService.getVisitsByShopId(shop.getId());

        assertEquals(1, visits.size());
        assertEquals(existingVisit, visits.get(0));
    }

    @Test
    public void getVisitsByShopId_whenShopDoesNotExist_throwsException() {
        assertThrows(ShopNotFoundException.class, () -> {
            when(shopRepository.findById(any())).thenReturn(Optional.empty());

            visitService.getVisitsByShopId(999L);
        });
    }

    @Test
    public void getVisitsByShopName_returnsVisits() {
        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findAllByShop(shop)).thenReturn(Collections.singletonList(existingVisit));

        when(shopRepository.findOneByName(shop.getName())).thenReturn(shop);

        List<Visit> visits = visitService.getVisitsByShopName(shop.getName());

        assertEquals(1, visits.size());
        assertEquals(existingVisit, visits.get(0));
    }

    @Test
    public void getVisitsByShopName_whenShopDoesNotExist_throwsException() {
        assertThrows(ShopNotFoundException.class, () -> {
            when(shopRepository.findOneByName(any())).thenReturn(null);

            visitService.getVisitsByShopName("test-shop");
        });
    }

    @Test
    public void getVisitsByShop_returnsVisits() {
        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findAllByShop(shop)).thenReturn(Collections.singletonList(existingVisit));

        List<Visit> visits = visitService.getVisitsByShop(shop);

        assertEquals(1, visits.size());
        assertEquals(existingVisit, visits.get(0));
    }

    @Test
    public void createVisit_givenShopId_createsAndReturnsVisit() {
        when(visitRepository.save(any(Visit.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        when(shopRepository.findById(shop.getId())).thenReturn(Optional.of(shop));

        Visit visit = visitService.createVisit(shop.getId());

        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
        verify(visitRepository).save(captor.capture());
        Visit savedVisit = captor.getValue();

        assertEquals(savedVisit, visit);
        assertEquals(shop, visit.getShop());
    }

    @Test
    public void createVisit_givenShopId_whenShopDoesNotExist_throwsException() {
        assertThrows(ShopNotFoundException.class, () -> {
            when(shopRepository.findById(any())).thenReturn(Optional.empty());

            visitService.createVisit(999L);
        });
    }

    @Test
    public void createVisit_givenShopName_createsAndReturnsVisit() {
        when(visitRepository.save(any(Visit.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        when(shopRepository.findOneByName(shop.getName())).thenReturn(shop);

        Visit visit = visitService.createVisit(shop.getName());

        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
        verify(visitRepository).save(captor.capture());
        Visit savedVisit = captor.getValue();

        assertEquals(savedVisit, visit);
        assertEquals(shop, visit.getShop());
    }

    @Test
    public void createVisit_givenShopName_whenShopDoesNotExist_throwsException() {
        assertThrows(ShopNotFoundException.class, () -> {
            when(shopRepository.findOneByName(any())).thenReturn(null);

            visitService.createVisit("test-shop");
        });
    }

    @Test
    public void createVisit_createsAndReturnsVisit() {
        when(visitRepository.save(any(Visit.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Visit visit = visitService.createVisit(shop);

        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
        verify(visitRepository).save(captor.capture());
        Visit savedVisit = captor.getValue();

        assertEquals(savedVisit, visit);
        assertEquals(shop, visit.getShop());
    }

    @Test
    public void startVisit_startsAndReturnsVisit() {
        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findById(existingVisit.getId())).thenReturn(Optional.of(existingVisit));

        when(visitRepository.save(any(Visit.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Visit visit = visitService.startVisit(existingVisit.getId());

        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
        verify(visitRepository).save(captor.capture());
        Visit savedVisit = captor.getValue();

        assertEquals(savedVisit, visit);
        assertEquals(existingVisit.getId(), visit.getId());
        assertNotNull(visit.getStarted());
    }

    @Test
    public void startVisit_whenVisitDoesNotExist_throwsException() {
        assertThrows(VisitNotFoundException.class, () -> {
            when(visitRepository.findById(any())).thenReturn(Optional.empty());

            visitService.startVisit(999L);
        });
    }

    @Test
    public void completeVisit_completesVisit() {
        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findById(existingVisit.getId())).thenReturn(Optional.of(existingVisit));

        when(visitRepository.save(any(Visit.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Visit visit = visitService.completeVisit(existingVisit.getId());

        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
        verify(visitRepository).save(captor.capture());
        Visit savedVisit = captor.getValue();

        assertEquals(savedVisit, visit);
        assertEquals(existingVisit.getId(), visit.getId());
        assertNotNull(visit.getCompleted());
    }

    @Test
    public void completeVisit_whenVisitDoesNotExist_throwsException() {
        assertThrows(VisitNotFoundException.class, () -> {
            when(visitRepository.findById(any())).thenReturn(Optional.empty());

            visitService.completeVisit(999L);
        });
    }

    @Test
    public void deleteVisit_deletesVisit() {
        Visit existingVisit = new Visit(1L, shop);
        when(visitRepository.findById(existingVisit.getId())).thenReturn(Optional.of(existingVisit));

        visitService.deleteVisit(existingVisit.getId());

        verify(visitRepository).delete(eq(existingVisit));
    }

    @Test
    public void deleteVisit_whenVisitDoesNotExist_throwsException() {
        assertThrows(VisitNotFoundException.class, () -> {
            when(visitRepository.findById(any())).thenReturn(Optional.empty());

            visitService.deleteVisit(999L);
        });
    }
}
