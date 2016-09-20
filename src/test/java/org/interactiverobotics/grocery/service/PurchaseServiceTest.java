/*
 * PurchaseServiceTest.java
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

package org.interactiverobotics.grocery.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Purchase service test.
 */
@RunWith(MockitoJUnitRunner.class)
public class PurchaseServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    private PurchaseService purchaseService;

    private Shop shop;

    private Visit visit;

    private Item item;


    @Before
    public void setUp() throws Exception {

        purchaseService = new PurchaseService(visitRepository, itemRepository, purchaseRepository);

        shop = new Shop(1L, "test-shop");

        visit = new Visit(1L, shop);

        item = new Item(1L, "test-item");

        when(visitRepository.findOne(visit.getId())).thenReturn(visit);
        when(itemRepository.findOne(item.getId())).thenReturn(item);
    }


    @Test
    public void testGetPurchases1() throws Exception {

        List<Purchase> existingPurchases = Arrays.asList(
                new Purchase(1L, visit, item, 1L, null), new Purchase(2L, visit, item, 1L, null));
        when(purchaseRepository.findAllByVisit(visit)).thenReturn(existingPurchases);

        final List<Purchase> purchases = purchaseService.getPurchases(visit.getId());

        assertEquals(existingPurchases, purchases);
    }

    @Test
    public void testGetPurchases2() throws Exception {

        List<Purchase> existingPurchases = Arrays.asList(
                new Purchase(1L, visit, item, 1L, null), new Purchase(2L, visit, item, 1L, null));
        when(purchaseRepository.findAllByVisit(visit)).thenReturn(existingPurchases);

        final List<Purchase> purchases = purchaseService.getPurchases(visit);

        assertEquals(existingPurchases, purchases);
    }

    @Test
    public void testGetNotPurchasedItems1() throws Exception {

        final List<Item> existingItems = Arrays.asList(new Item(1L, "test-item-1"), new Item(2L, "test-item-2"));
        when(itemRepository.findAll()).thenReturn(existingItems);

        when(purchaseRepository.findOneByVisitAndItem(visit, existingItems.get(0)))
                .thenReturn(new Purchase(visit, existingItems.get(0), 1L, null));

        final List<Item> items = purchaseService.getNotPurchasedItems(visit.getId());

        assertEquals(1, items.size());
        assertEquals(existingItems.get(1), items.get(0));
    }

    @Test
    public void testGetNotPurchasedItems2() throws Exception {

        final List<Item> existingItems = Arrays.asList(new Item(1L, "test-item-1"), new Item(2L, "test-item-2"));
        when(itemRepository.findAll()).thenReturn(existingItems);

        when(purchaseRepository.findOneByVisitAndItem(visit, existingItems.get(0)))
                .thenReturn(new Purchase(visit, existingItems.get(0), 1L, null));

        final List<Item> items = purchaseService.getNotPurchasedItems(visit);

        assertEquals(1, items.size());
        assertEquals(existingItems.get(1), items.get(0));
    }


    public static class PurchasePageAnswer implements Answer<Page<Purchase>> {

        private final List<Purchase> purchases;

        public PurchasePageAnswer(final List<Purchase> purchases) {
            this.purchases = purchases;
        }

        @Override
        public Page<Purchase> answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(2, invocation.getArguments().length);
            final Pageable pageable = invocation.getArgumentAt(0, Pageable.class);
            return new PageImpl<>(purchases, pageable, purchases.size());
        }
    }


    @Test
    public void testGetPurchasePage1() throws Exception {

        final List<Purchase> existingPurchases = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingPurchases.add(new Purchase(i, visit, item, 1L, null));
        }

        final PurchasePageAnswer purchasePageAnswer = new PurchasePageAnswer(existingPurchases);
        when(purchaseRepository.findAllByVisit(any(Pageable.class), eq(visit))).thenAnswer(purchasePageAnswer);

        final Page<Purchase> purchases = purchaseService.getPurchases(new PageRequest(0, 10), visit.getId());

        assertEquals(existingPurchases.size(), purchases.getTotalElements());
        assertEquals(10, purchases.getTotalPages());
    }

    @Test
    public void testGetPurchasePage2() throws Exception {

        final List<Purchase> existingPurchases = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingPurchases.add(new Purchase(i, visit, item, 1L, null));
        }

        final PurchasePageAnswer purchasePageAnswer = new PurchasePageAnswer(existingPurchases);
        when(purchaseRepository.findAllByVisit(any(Pageable.class), eq(visit))).thenAnswer(purchasePageAnswer);

        final Page<Purchase> purchases = purchaseService.getPurchases(new PageRequest(0, 10), visit);

        assertEquals(existingPurchases.size(), purchases.getTotalElements());
        assertEquals(10, purchases.getTotalPages());
    }


    public static class SaveAndReturnPurchaseAnswer implements Answer<Purchase> {

        private Purchase purchase;

        public Purchase getPurchase() {
            return purchase;
        }

        @Override
        public Purchase answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            purchase = invocation.getArgumentAt(0, Purchase.class);
            if (purchase.getId() == null) {
                purchase.setId(1L);
            }
            return purchase;
        }
    }


    @Test
    public void testBuyItem1() throws Exception {

        final SaveAndReturnPurchaseAnswer saveAndReturnPurchaseAnswer = new SaveAndReturnPurchaseAnswer();
        when(purchaseRepository.save(any(Purchase.class))).then(saveAndReturnPurchaseAnswer);

        final Long quantity = 1L;

        final Purchase purchase = purchaseService.buyItem(visit.getId(), item.getId(), quantity, null);

        // Check that Service returns what was saved
        final Purchase savedPurchase = saveAndReturnPurchaseAnswer.getPurchase();
        assertEquals(savedPurchase, purchase);

        // Check response content
        assertEquals(visit, purchase.getVisit());
        assertEquals(item, purchase.getItem());
        assertEquals(quantity, purchase.getQuantity());
        assertNull(purchase.getPrice());
    }

    @Test
    public void testBuyItem2() {

        final SaveAndReturnPurchaseAnswer saveAndReturnPurchaseAnswer = new SaveAndReturnPurchaseAnswer();
        when(purchaseRepository.save(any(Purchase.class))).then(saveAndReturnPurchaseAnswer);

        final Long quantity = 1L;

        final Purchase purchase = purchaseService.buyItem(visit, item, quantity, null);

        // Check that Service returns what was saved
        final Purchase savedPurchase = saveAndReturnPurchaseAnswer.getPurchase();
        assertEquals(savedPurchase, purchase);

        // Check response content
        assertEquals(visit, purchase.getVisit());
        assertEquals(item, purchase.getItem());
        assertEquals(quantity, purchase.getQuantity());
        assertNull(purchase.getPrice());
    }

    @Test
    public void testBuyItemForExistingPurchase() {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        final SaveAndReturnPurchaseAnswer saveAndReturnPurchaseAnswer = new SaveAndReturnPurchaseAnswer();
        when(purchaseRepository.save(any(Purchase.class))).then(saveAndReturnPurchaseAnswer);

        final Purchase purchase = purchaseService.buyItem(visit, item, 1L, null);

        // Check that Service returns what was saved
        final Purchase savedPurchase = saveAndReturnPurchaseAnswer.getPurchase();
        assertEquals(savedPurchase, purchase);

        // Check response content
        assertEquals(visit, purchase.getVisit());
        assertEquals(item, purchase.getItem());
        assertEquals(new Long(2), purchase.getQuantity());
        assertNull(purchase.getPrice());
    }

    @Test(expected = VisitNotFoundException.class)
    public void testBuyItemForWrongVisitId() throws Exception {
        purchaseService.buyItem(999L, item.getId(), 1L, null);
    }

    @Test(expected = ItemNotFoundException.class)
    public void testBuyItemForWrongItemId() throws Exception {
        purchaseService.buyItem(visit.getId(), 999L, 1L, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyItemForWrongQuantity() throws Exception {
        purchaseService.buyItem(visit, item, 0L, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyItemForWrongPrice() throws Exception {
        purchaseService.buyItem(visit, item, 1L, BigDecimal.ZERO);
    }

    @Test
    public void testBuyItemSetPriceForNewPurchase() {

        final SaveAndReturnPurchaseAnswer saveAndReturnPurchaseAnswer = new SaveAndReturnPurchaseAnswer();
        when(purchaseRepository.save(any(Purchase.class))).then(saveAndReturnPurchaseAnswer);

        final Purchase purchase = purchaseService.buyItem(visit, item, 1L, BigDecimal.valueOf(10L));

        // Check that Service returns what was saved
        final Purchase savedPurchase = saveAndReturnPurchaseAnswer.getPurchase();
        assertEquals(savedPurchase, purchase);

        // Check response content
        assertEquals(BigDecimal.valueOf(10L), purchase.getPrice());
    }

    @Test
    public void testBuyItemSetPriceForExistingPurchase() {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        final SaveAndReturnPurchaseAnswer saveAndReturnPurchaseAnswer = new SaveAndReturnPurchaseAnswer();
        when(purchaseRepository.save(any(Purchase.class))).then(saveAndReturnPurchaseAnswer);

        final Purchase purchase = purchaseService.buyItem(visit, item, 1L, BigDecimal.valueOf(10L));

        // Check that Service returns what was saved
        final Purchase savedPurchase = saveAndReturnPurchaseAnswer.getPurchase();
        assertEquals(savedPurchase, purchase);

        // Check response content
        assertEquals(BigDecimal.valueOf(10L), purchase.getPrice());
    }

    @Test
    public void testBuyItemUpdatePrice() {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 1L, BigDecimal.valueOf(10L));
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        final SaveAndReturnPurchaseAnswer saveAndReturnPurchaseAnswer = new SaveAndReturnPurchaseAnswer();
        when(purchaseRepository.save(any(Purchase.class))).then(saveAndReturnPurchaseAnswer);

        final Purchase purchase = purchaseService.buyItem(visit, item, 1L, BigDecimal.valueOf(20L));

        // Check that Service returns what was saved
        final Purchase savedPurchase = saveAndReturnPurchaseAnswer.getPurchase();
        assertEquals(savedPurchase, purchase);

        // Check response content
        assertEquals(BigDecimal.valueOf(15L), purchase.getPrice());
    }

    @Test
    public void testReturnItem1() {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 2L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        final SaveAndReturnPurchaseAnswer saveAndReturnPurchaseAnswer = new SaveAndReturnPurchaseAnswer();
        when(purchaseRepository.save(any(Purchase.class))).then(saveAndReturnPurchaseAnswer);

        final Purchase purchase = purchaseService.returnItem(visit.getId(), item.getId(), 1L);

        // Check that Service returns what was saved
        final Purchase savedPurchase = saveAndReturnPurchaseAnswer.getPurchase();
        assertEquals(savedPurchase, purchase);

        // Check response content
        assertEquals(visit, purchase.getVisit());
        assertEquals(item, purchase.getItem());
        assertEquals(new Long(1L), purchase.getQuantity());
        assertNull(purchase.getPrice());
    }

    @Test
    public void testReturnItem2() {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 2L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        final SaveAndReturnPurchaseAnswer saveAndReturnPurchaseAnswer = new SaveAndReturnPurchaseAnswer();
        when(purchaseRepository.save(any(Purchase.class))).then(saveAndReturnPurchaseAnswer);

        final Purchase purchase = purchaseService.returnItem(visit, item, 1L);

        // Check that Service returns what was saved
        final Purchase savedPurchase = saveAndReturnPurchaseAnswer.getPurchase();
        assertEquals(savedPurchase, purchase);

        // Check response content
        assertEquals(visit, purchase.getVisit());
        assertEquals(item, purchase.getItem());
        assertEquals(new Long(1L), purchase.getQuantity());
        assertNull(purchase.getPrice());
    }

    @Test(expected = VisitNotFoundException.class)
    public void testReturnItemForWrongVisitId() throws Exception {
        purchaseService.returnItem(999L, item.getId(), 1L);
    }

    @Test(expected = ItemNotFoundException.class)
    public void testReturnItemForWrongItemId() throws Exception {
        purchaseService.returnItem(visit.getId(), 999L, 1L);
    }

    @Test(expected = PurchaseNotFoundException.class)
    public void testReturnItemForNotExistingPurchase() throws Exception {
        purchaseService.returnItem(visit, item, 1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReturnItemForWrongQuantity1() throws Exception {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        purchaseService.returnItem(visit, item, 0L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReturnItemForWrongQuantity2() throws Exception {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        purchaseService.returnItem(visit, item, 999L);
    }

    @Test
    public void testReturnItemAndDeletePurchase() {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        final Purchase purchase = purchaseService.returnItem(visit, item, 1L);

        verify(purchaseRepository).delete(eq(existingPurchase));

        assertNull(purchase);
    }

    @Test
    public void testUpdatePrice1() {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        final SaveAndReturnPurchaseAnswer saveAndReturnPurchaseAnswer = new SaveAndReturnPurchaseAnswer();
        when(purchaseRepository.save(any(Purchase.class))).then(saveAndReturnPurchaseAnswer);

        final Purchase purchase = purchaseService.updatePrice(visit.getId(), item.getId(), BigDecimal.valueOf(10L));

        // Check that Service returns what was saved
        final Purchase savedPurchase = saveAndReturnPurchaseAnswer.getPurchase();
        assertEquals(savedPurchase, purchase);

        // Check response content
        assertEquals(BigDecimal.valueOf(10L), purchase.getPrice());
    }

    @Test
    public void testUpdatePrice2() {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        final SaveAndReturnPurchaseAnswer saveAndReturnPurchaseAnswer = new SaveAndReturnPurchaseAnswer();
        when(purchaseRepository.save(any(Purchase.class))).then(saveAndReturnPurchaseAnswer);

        final Purchase purchase = purchaseService.updatePrice(visit, item, BigDecimal.valueOf(10L));

        // Check that Service returns what was saved
        final Purchase savedPurchase = saveAndReturnPurchaseAnswer.getPurchase();
        assertEquals(savedPurchase, purchase);

        // Check response content
        assertEquals(BigDecimal.valueOf(10L), purchase.getPrice());
    }

    @Test(expected = VisitNotFoundException.class)
    public void testUpdatePriceForWrongVisitId() throws Exception {
        purchaseService.updatePrice(999L, item.getId(), BigDecimal.valueOf(10L));
    }

    @Test(expected = ItemNotFoundException.class)
    public void testUpdatePriceForWrongItemId() throws Exception {
        purchaseService.updatePrice(visit.getId(), 999L, BigDecimal.valueOf(10L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePriceForWrongPrice() throws Exception {

        final Purchase existingPurchase = new Purchase(1L, visit, item, 1L, null);
        when(purchaseRepository.findOneByVisitAndItem(visit, item)).thenReturn(existingPurchase);

        purchaseService.updatePrice(visit, item, BigDecimal.ZERO);
    }

}
