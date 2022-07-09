/*
 * PurchaseServiceTest.java
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

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.Purchase;
import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.exception.ItemNotFoundException;
import org.interactiverobotics.grocery.exception.PurchaseNotFoundException;
import org.interactiverobotics.grocery.exception.VisitNotFoundException;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.interactiverobotics.grocery.repository.PurchaseRepository;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Purchase service test.
 */
@ExtendWith(MockitoExtension.class)
public class PurchaseServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    private Visit visit;

    private Item item;


    /**
     * Initializes test.
     */
    @BeforeEach
    public void setUp() {
        visit = new Visit(1L, new Shop(1L, "test-shop"));
        lenient().when(visitRepository.findById(visit.getId())).thenReturn(Optional.of(visit));

        item = new Item(1L, "test-item");
        lenient().when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
    }


    @Test
    public void getPurchases_givenVisitId_returnsPurchases() {
        List<Purchase> existingPurchases = List.of(
                new Purchase(1L, visit, item, 1L, null),
                new Purchase(2L, visit, item, 1L, null));
        when(purchaseRepository.findAllByVisit(visit)).thenReturn(existingPurchases);

        List<Purchase> purchases = purchaseService.getPurchases(visit.getId());

        assertEquals(existingPurchases, purchases);
    }

    @Test
    public void getPurchases_givenVisit_returnsPurchases() {
        List<Purchase> existingPurchases = List.of(
                new Purchase(1L, visit, item, 1L, null),
                new Purchase(2L, visit, item, 1L, null));
        when(purchaseRepository.findAllByVisit(visit)).thenReturn(existingPurchases);

        List<Purchase> purchases = purchaseService.getPurchases(visit);

        assertEquals(existingPurchases, purchases);
    }

    @Test
    public void getNotPurchasedItems_givenVisitId_returnsItems() {
        List<Item> existingItems = List.of(new Item(1L, "test-item-1"), new Item(2L, "test-item-2"));
        when(itemRepository.findAll()).thenReturn(existingItems);

        when(purchaseRepository.findOneByVisitAndItem(visit, existingItems.get(0)))
                .thenReturn(new Purchase(visit, existingItems.get(0), 1L, null));

        List<Item> items = purchaseService.getNotPurchasedItems(visit.getId());

        assertEquals(1, items.size());
        assertEquals(existingItems.get(1), items.get(0));
    }

    @Test
    public void getNotPurchasedItems_givenVisit_returnsItems() {
        List<Item> existingItems = List.of(new Item(1L, "test-item-1"), new Item(2L, "test-item-2"));
        when(itemRepository.findAll()).thenReturn(existingItems);

        when(purchaseRepository.findOneByVisitAndItem(visit, existingItems.get(0)))
                .thenReturn(new Purchase(visit, existingItems.get(0), 1L, null));

        List<Item> items = purchaseService.getNotPurchasedItems(visit);

        assertEquals(1, items.size());
        assertEquals(existingItems.get(1), items.get(0));
    }

    @Test
    public void getPurchase_givenPageRequestAndVisitId_returnsPageOfPurchases() {
        List<Purchase> existingPurchases = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingPurchases.add(new Purchase(i, visit, item, 1L, null));
        }

        when(purchaseRepository.findAllByVisit(any(Pageable.class), eq(visit))).then(invocation -> {
            assertEquals(2, invocation.getArguments().length);
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingPurchases, pageable, existingPurchases.size());
        });

        Page<Purchase> purchases = purchaseService.getPurchases(PageRequest.of(0, 10), visit.getId());

        assertEquals(existingPurchases.size(), purchases.getTotalElements());
        assertEquals(10, purchases.getTotalPages());
    }

    @Test
    public void getPurchase_givenPageRequestAndVisit_returnsPageOfPurchases() {
        List<Purchase> existingPurchases = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingPurchases.add(new Purchase(i, visit, item, 1L, null));
        }

        when(purchaseRepository.findAllByVisit(any(Pageable.class), eq(visit))).then(invocation -> {
            assertEquals(2, invocation.getArguments().length);
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingPurchases, pageable, existingPurchases.size());
        });

        Page<Purchase> purchases = purchaseService.getPurchases(PageRequest.of(0, 10), visit);

        assertEquals(existingPurchases.size(), purchases.getTotalElements());
        assertEquals(10, purchases.getTotalPages());
    }

    @Test
    public void buyItem_givenVisitIdAndItemId_returnsPurchase() {
        when(purchaseRepository.save(any(Purchase.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Long quantity = 1L;

        Purchase purchase = purchaseService.buyItem(visit.getId(), item.getId(), quantity, null);

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase savedPurchase = captor.getValue();

        assertEquals(savedPurchase, purchase);
        assertEquals(visit, purchase.getVisit());
        assertEquals(item, purchase.getItem());
        assertEquals(quantity, purchase.getQuantity());
        assertNull(purchase.getPrice());
    }

    @Test
    public void buyItem_givenVisitAndItem_returnsPurchase() {
        when(purchaseRepository.save(any(Purchase.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Long quantity = 1L;

        Purchase purchase = purchaseService.buyItem(visit, item, quantity, null);

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase savedPurchase = captor.getValue();

        assertEquals(savedPurchase, purchase);
        assertEquals(visit, purchase.getVisit());
        assertEquals(item, purchase.getItem());
        assertEquals(quantity, purchase.getQuantity());
        assertNull(purchase.getPrice());
    }

    @Test
    public void buyItem_increasesQuantityAndReturnsPurchase() {
        Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        when(purchaseRepository.save(any(Purchase.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Purchase purchase = purchaseService.buyItem(visit, item, 1L, null);

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase savedPurchase = captor.getValue();

        assertEquals(savedPurchase, purchase);
        assertEquals(visit, purchase.getVisit());
        assertEquals(item, purchase.getItem());
        assertEquals(Long.valueOf(2), purchase.getQuantity());
        assertNull(purchase.getPrice());
    }

    @Test
    public void buyItem_whenVisitDoesNotExist_throwsException() {
        assertThrows(VisitNotFoundException.class, () ->
                purchaseService.buyItem(999L, item.getId(), 1L, null));
    }

    @Test
    public void buyItem_whenItemDoesNotExist_throwsException() {
        assertThrows(ItemNotFoundException.class, () ->
                purchaseService.buyItem(visit.getId(), 999L, 1L, null));
    }

    @Test
    public void buyItem_whenQuantityLessOrEqualToZero_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                purchaseService.buyItem(visit, item, 0L, null));
    }

    @Test
    public void buyItem_whenPriceLessOrEqualToZero_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                purchaseService.buyItem(visit, item, 1L, BigDecimal.ZERO));
    }

    @Test
    public void buyItem_setPriceForNewPurchase() {
        when(purchaseRepository.save(any(Purchase.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Purchase purchase = purchaseService.buyItem(visit, item, 1L, BigDecimal.valueOf(10L));

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase savedPurchase = captor.getValue();

        assertEquals(savedPurchase, purchase);
        assertEquals(BigDecimal.valueOf(10L), purchase.getPrice());
    }

    @Test
    public void buyItem_whenPriceIsNull_setPriceForPurchase() {
        Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        when(purchaseRepository.save(any(Purchase.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Purchase purchase = purchaseService.buyItem(visit, item, 1L, BigDecimal.valueOf(10L));

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase savedPurchase = captor.getValue();

        assertEquals(savedPurchase, purchase);
        assertEquals(BigDecimal.valueOf(10L), purchase.getPrice());
    }

    @Test
    public void buyItem_updatesPriceOfPurchase() {
        Purchase existingPurchase = new Purchase(1L, visit, item, 1L, BigDecimal.valueOf(10L));
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        when(purchaseRepository.save(any(Purchase.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Purchase purchase = purchaseService.buyItem(visit, item, 1L, BigDecimal.valueOf(20L));

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase savedPurchase = captor.getValue();

        assertEquals(savedPurchase, purchase);
        assertEquals(BigDecimal.valueOf(15L), purchase.getPrice());
    }

    @Test
    public void returnItem_givenVisitIdAndItemId_decreasesQuantityAndReturnsPurchase() {
        Purchase existingPurchase = new Purchase(1L, visit, item, 2L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        when(purchaseRepository.save(any(Purchase.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Purchase purchase = purchaseService.returnItem(visit.getId(), item.getId(), 1L);

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase savedPurchase = captor.getValue();

        assertEquals(savedPurchase, purchase);
        assertEquals(visit, purchase.getVisit());
        assertEquals(item, purchase.getItem());
        assertEquals(Long.valueOf(1L), purchase.getQuantity());
        assertNull(purchase.getPrice());
    }

    @Test
    public void returnItem_givenVisitAndItem_decreasesQuantityAndReturnsPurchase() {
        Purchase existingPurchase = new Purchase(1L, visit, item, 2L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        when(purchaseRepository.save(any(Purchase.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Purchase purchase = purchaseService.returnItem(visit, item, 1L);

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase savedPurchase = captor.getValue();

        assertEquals(savedPurchase, purchase);
        assertEquals(visit, purchase.getVisit());
        assertEquals(item, purchase.getItem());
        assertEquals(Long.valueOf(1L), purchase.getQuantity());
        assertNull(purchase.getPrice());
    }

    @Test
    public void returnItem_whenVisitDoesNotExist_throwsException() {
        assertThrows(VisitNotFoundException.class, () ->
                purchaseService.returnItem(999L, item.getId(), 1L));
    }

    @Test
    public void returnItem_whenItemDoesNotExist_throwsException() {
        assertThrows(ItemNotFoundException.class, () ->
                purchaseService.returnItem(visit.getId(), 999L, 1L));
    }

    @Test
    public void returnItem_whenPurchaseDoesNotExist_throwsException() {
        assertThrows(PurchaseNotFoundException.class, () ->
                purchaseService.returnItem(visit, item, 1L));
    }

    @Test
    public void returnItem_whenQuantityLessOrEqualToZero_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
            when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

            purchaseService.returnItem(visit, item, 0L);
        });
    }

    @Test
    public void returnItem_whenQuantityGreaterThanQuantityOfPurchase_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
            when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

            purchaseService.returnItem(visit, item, 999L);
        });
    }

    @Test
    public void returnItem_whenQuantityDecreasesToZero_deletesPurchase() {
        Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        Purchase purchase = purchaseService.returnItem(visit, item, 1L);

        verify(purchaseRepository).delete(eq(existingPurchase));

        assertNull(purchase);
    }

    @Test
    public void updatePrice_givenVisitIdAndItemId_updatesPriceAndReturnsPurchase() {
        Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        when(purchaseRepository.save(any(Purchase.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Purchase purchase = purchaseService.updatePrice(visit.getId(), item.getId(), BigDecimal.valueOf(10L));

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase savedPurchase = captor.getValue();

        assertEquals(savedPurchase, purchase);
        assertEquals(BigDecimal.valueOf(10L), purchase.getPrice());
    }

    @Test
    public void updatePrice_givenVisitAndItem_updatesPriceAndReturnsPurchase() {
        Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        when(purchaseRepository.save(any(Purchase.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        Purchase purchase = purchaseService.updatePrice(visit, item, BigDecimal.valueOf(10L));

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase savedPurchase = captor.getValue();

        assertEquals(savedPurchase, purchase);
        assertEquals(BigDecimal.valueOf(10L), purchase.getPrice());
    }

    @Test
    public void updatePrice_whenVisitIdDoesNotExist_throwsException() {
        assertThrows(VisitNotFoundException.class, () ->
                purchaseService.updatePrice(999L, item.getId(), BigDecimal.valueOf(10L)));
    }

    @Test
    public void updatePrice_whenItemIdDoesNotExist_throwsException() {
        assertThrows(ItemNotFoundException.class, () ->
                purchaseService.updatePrice(visit.getId(), 999L, BigDecimal.valueOf(10L)));
    }

    @Test
    public void updatePrice_whenPriceLessOrEqualToZero_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
            when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

            purchaseService.updatePrice(visit, item, BigDecimal.ZERO);
        });
    }
}
