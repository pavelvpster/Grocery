/*
 * PurchaseRestControllerIntegrationTest.java
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

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.Purchase;
import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.interactiverobotics.grocery.repository.PurchaseRepository;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.interactiverobotics.grocery.repository.VisitRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Purchase REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PurchaseRestControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    private Shop shop;

    private Visit visit;

    private Item item;


    @Before
    public void setUp() throws Exception {

        purchaseRepository.deleteAll();

        shop = shopRepository.save(new Shop("test-shop"));

        visit = visitRepository.save(new Visit(shop));

        item = itemRepository.save(new Item("test-item"));
    }

    @After
    public void tearDown() throws Exception {

        itemRepository.delete(item);

        visitRepository.delete(visit);

        shopRepository.delete(shop);
    }


    @Test
    public void testGetPurchasesPage() {

        final List<Purchase> existingPurchases = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingPurchases.add(purchaseRepository.save(new Purchase(visit, item, 1L, null)));
        }

        final ParameterizedTypeReference<PageResponse<Purchase>> responseType =
                new ParameterizedTypeReference<PageResponse<Purchase>>() {};
        final ResponseEntity<PageResponse<Purchase>> response = restTemplate.exchange("/api/v1/purchase/"
                        + visit.getId() + "/list?page=1&size=10", HttpMethod.GET, null, responseType);

        purchaseRepository.delete(existingPurchases);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingPurchases.size(), response.getBody().getTotalElements());
        assertEquals(10, response.getBody().getTotalPages());
        assertEquals(10, response.getBody().getSize());
    }

    @Test
    public void testBuyItem() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/buy/" + item.getId() + "?quantity=1", null, Purchase.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        purchaseRepository.delete(response.getBody());

        assertEquals(visit, response.getBody().getVisit());
        assertEquals(item, response.getBody().getItem());
        assertEquals(new Long(1L), response.getBody().getQuantity());
    }

    @Test
    public void testBuyItemForExistingPurchase() {

        final Purchase existingPurchase = purchaseRepository.save(new Purchase(visit, item, 1L, null));

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/buy/" + item.getId() + "?quantity=1", null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(visit, response.getBody().getVisit());
        assertEquals(item, response.getBody().getItem());
        assertEquals(new Long(2L), response.getBody().getQuantity());
    }

    @Test
    public void testBuyItemForWrongVisitId() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + new Long(999L)
                + "/buy/" + item.getId() + "?quantity=1", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testBuyItemForWrongItemId() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/buy/" + new Long(999L) + "?quantity=1", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testBuyItemForWrongQuantity() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/buy/" + item.getId() + "?quantity=0", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testBuyItemForWrongPrice() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/buy/" + item.getId() + "?quantity=1&price=0", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testBuyItemSetPriceForNewPurchase() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/buy/" + item.getId() + "?quantity=1&price=10", null, Purchase.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        purchaseRepository.delete(response.getBody());

        assertEquals(BigDecimal.valueOf(10L), response.getBody().getPrice());
    }

    @Test
    public void testBuyItemSetPriceForExistingPurchase() {

        final Purchase existingPurchase = purchaseRepository.save(new Purchase(visit, item, 1L, null));

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/buy/" + item.getId() + "?quantity=1&price=10", null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(BigDecimal.valueOf(10L), response.getBody().getPrice());
    }

    @Test
    public void testBuyItemUpdatePrice() {

        final Purchase existingPurchase = purchaseRepository
                .save(new Purchase(visit, item, 1L, BigDecimal.valueOf(10L)));

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/buy/" + item.getId() + "?quantity=1&price=20", null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(BigDecimal.valueOf(1500, 2), response.getBody().getPrice());
    }

    @Test
    public void testReturnItem() {

        final Purchase existingPurchase = purchaseRepository.save(new Purchase(visit, item, 2L, null));

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/return/" + item.getId() + "?quantity=1", null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(visit, response.getBody().getVisit());
        assertEquals(item, response.getBody().getItem());
        assertEquals(new Long(1L), response.getBody().getQuantity());
    }

    @Test
    public void testReturnItemForWrongVisitId() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + new Long(999L)
                + "/return/" + item.getId() + "?quantity=1", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testReturnItemForWrongItemId() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/return/" + new Long(999L) + "?quantity=1", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testReturnItemForNotExistingPurchase() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/return/" + item.getId() + "?quantity=1", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testReturnItemForWrongQuantity1() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/return/" + item.getId() + "?quantity=0", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testReturnItemForWrongQuantity2() {

        final Purchase existingPurchase = purchaseRepository.save(new Purchase(visit, item, 1L, null));

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/return/" + item.getId() + "?quantity=999", null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testReturnItemAndDeletePurchase() {

        final Purchase existingPurchase = purchaseRepository.save(new Purchase(visit, item, 1L, null));

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/return/" + item.getId() + "?quantity=1", null, Purchase.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNull(purchaseRepository.findOne(existingPurchase.getId()));
    }

    @Test
    public void testUpdatePrice() {

        final Purchase existingPurchase = purchaseRepository.save(new Purchase(visit, item, 1L, null));

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/price/" + item.getId() + "?price=10", null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(BigDecimal.valueOf(10), response.getBody().getPrice());
    }

    @Test
    public void testUpdatePriceForWrongVisitId() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + new Long(999L)
                + "/price/" + item.getId() + "?price=10", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testUpdatePriceForWrongItemId() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/price/" + new Long(999L) + "?price=10", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testUpdatePriceForWrongPrice() {

        final ResponseEntity<Purchase> response = restTemplate.postForEntity("/api/v1/purchase/" + visit.getId()
                + "/price/" + item.getId() + "?price=0", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
