/*
 * PurchaseRestControllerIntegrationTest.java
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

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.Purchase;
import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.interactiverobotics.grocery.repository.PurchaseRepository;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.interactiverobotics.grocery.repository.VisitRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Purchase REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PurchaseRestControllerIntegrationTest {

    private static final String PURCHASE_ENDPOINT = "/api/v1/purchase/";
    private static final String BUY_ACTION = "/buy/";
    private static final String RETURN_ACTION = "/return/";
    private static final String PRICE_ACTION = "/price/";
    private static final String QUANTITY_1 = "?quantity=1";

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


    /**
     * Initializes test.
     */
    @BeforeEach
    public void setUp() {
        purchaseRepository.deleteAll();
        shop = shopRepository.save(Shop.builder().name("test-shop").build());
        visit = visitRepository.save(Visit.builder().shop(shop).build());
        item = itemRepository.save(Item.builder().name("test-item").build());
    }

    /**
     * Finalises test.
     */
    @AfterEach
    public void tearDown() {
        itemRepository.delete(item);
        visitRepository.delete(visit);
        shopRepository.delete(shop);
    }


    @Test
    public void getNotPurchasedItems_returnsItems() {
        List<Item> existingItems = new ArrayList<>();
        itemRepository.saveAll(List.of(Item.builder().name("test-item-1").build(), Item.builder().name("test-item-2").build()))
                .forEach(item -> existingItems.add(item));

        Purchase purchase = purchaseRepository.save(Purchase.builder().visit(visit).item(existingItems.get(0)).quantity(1L).build());

        ResponseEntity<Item[]> response = restTemplate.getForEntity(PURCHASE_ENDPOINT + visit.getId()
                + "/not_purchased_items", Item[].class);

        purchaseRepository.delete(purchase);

        itemRepository.deleteAll(existingItems);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertTrue(List.of(response.getBody()).contains(existingItems.get(1)));
    }

    @Test
    public void getPurchasesPage_returnsPageOfPurchases() {
        List<Purchase> existingPurchases = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingPurchases.add(purchaseRepository.save(Purchase.builder().visit(visit).item(item).quantity(1L).build()));
        }

        ParameterizedTypeReference<PageResponse<Purchase>> responseType =
                new ParameterizedTypeReference<>() {};
        ResponseEntity<PageResponse<Purchase>> response = restTemplate.exchange(PURCHASE_ENDPOINT
                        + visit.getId() + "/list?page=1&size=10", HttpMethod.GET, null, responseType);

        purchaseRepository.deleteAll(existingPurchases);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingPurchases.size(), response.getBody().getTotalElements());
        assertEquals(10, response.getBody().getTotalPages());
        assertEquals(10, response.getBody().getSize());
    }

    @Test
    public void buyItem_createsAndReturnsPurchase() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + BUY_ACTION + item.getId() + QUANTITY_1, null, Purchase.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        purchaseRepository.delete(response.getBody());

        assertEquals(visit, response.getBody().getVisit());
        assertEquals(item, response.getBody().getItem());
        assertEquals(Long.valueOf(1L), response.getBody().getQuantity());
    }

    @Test
    public void buyItem_whenPurchaseExists_updatesAndReturnsPurchase() {
        Purchase existingPurchase = purchaseRepository.save(Purchase.builder().visit(visit).item(item).quantity(1L).build());

        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + BUY_ACTION + item.getId() + QUANTITY_1, null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(visit, response.getBody().getVisit());
        assertEquals(item, response.getBody().getItem());
        assertEquals(Long.valueOf(2L), response.getBody().getQuantity());
    }

    @Test
    public void buyItem_whenVisitDoesNotExist_returnsError() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + Long.valueOf(999L)
                + BUY_ACTION + item.getId() + QUANTITY_1, null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void buyItem_whenItemDoesNotExist_returnsError() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + BUY_ACTION + Long.valueOf(999L) + QUANTITY_1, null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void buyItem_whenQuantityLessOrEqualToZero_returnsError() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + BUY_ACTION + item.getId() + "?quantity=0", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void buyItem_whenPriceLessOrEqualToZero_returnsError() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + BUY_ACTION + item.getId() + "?quantity=1&price=0", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void buyItem_whenPurchaseDoesNotExist_setPriceAndReturnsPurchase() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + BUY_ACTION + item.getId() + "?quantity=1&price=10", null, Purchase.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        purchaseRepository.delete(response.getBody());

        assertEquals(BigDecimal.valueOf(10L), response.getBody().getPrice());
    }

    @Test
    public void buyItem_whenPurchaseExists_updatesPriceAndReturnsPurchase() {
        Purchase existingPurchase = purchaseRepository.save(Purchase.builder().visit(visit).item(item).quantity(1L).build());

        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + BUY_ACTION + item.getId() + "?quantity=1&price=10", null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(BigDecimal.valueOf(10L), response.getBody().getPrice());
    }

    @Test
    public void buyItem_whenPurchaseExistsAndPriceSet_updatesPrice() {
        Purchase existingPurchase = purchaseRepository
                .save(Purchase.builder().visit(visit).item(item).quantity(1L).price(BigDecimal.valueOf(10L)).build());

        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + BUY_ACTION + item.getId() + "?quantity=1&price=20", null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(BigDecimal.valueOf(1500, 2), response.getBody().getPrice());
    }

    @Test
    public void returnItem_updatesAndReturnsPurchase() {
        Purchase existingPurchase = purchaseRepository.save(Purchase.builder().visit(visit).item(item).quantity(2L).build());

        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + RETURN_ACTION + item.getId() + QUANTITY_1, null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(visit, response.getBody().getVisit());
        assertEquals(item, response.getBody().getItem());
        assertEquals(Long.valueOf(1L), response.getBody().getQuantity());
    }

    @Test
    public void returnItem_whenVisitDoesNotExist_returnsError() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + Long.valueOf(999L)
                + RETURN_ACTION + item.getId() + QUANTITY_1, null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void returnItem_whenItemDoesNotExist_returnsError() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + RETURN_ACTION + Long.valueOf(999L) + QUANTITY_1, null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void returnItem_whenPurchaseDoesNotExist_returnsError() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + RETURN_ACTION + item.getId() + QUANTITY_1, null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void returnItem_whenQuantityLessOrEqualToZero_returnsError() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + RETURN_ACTION + item.getId() + "?quantity=0", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void returnItem_whenQuantityIsGreaterThanQuantityOfPurchase_returnsError() {
        Purchase existingPurchase = purchaseRepository.save(Purchase.builder().visit(visit).item(item).quantity(1L).build());

        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + RETURN_ACTION + item.getId() + "?quantity=999", null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void returnItem_whenNewQuantityIsZero_deletesPurchase() {
        Purchase existingPurchase = purchaseRepository.save(Purchase.builder().visit(visit).item(item).quantity(1L).build());

        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + RETURN_ACTION + item.getId() + QUANTITY_1, null, Purchase.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertFalse(purchaseRepository.findById(existingPurchase.getId()).isPresent());
    }

    @Test
    public void updatePrice_updatesAndReturnsPurchase() {
        Purchase existingPurchase = purchaseRepository.save(Purchase.builder().visit(visit).item(item).quantity(1L).build());

        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + PRICE_ACTION + item.getId() + "?price=10", null, Purchase.class);

        purchaseRepository.delete(existingPurchase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(BigDecimal.valueOf(10), response.getBody().getPrice());
    }

    @Test
    public void updatePrice_whenVisitDoesNotExist_returnsError() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + Long.valueOf(999L)
                + PRICE_ACTION + item.getId() + "?price=10", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void updatePrice_whenItemDoesNotExist_returnsError() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + PRICE_ACTION + Long.valueOf(999L) + "?price=10", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void updatePrice_whenPriceLessOrEqualToZero_returnsError() {
        ResponseEntity<Purchase> response = restTemplate.postForEntity(PURCHASE_ENDPOINT + visit.getId()
                + PRICE_ACTION + item.getId() + "?price=0", null, Purchase.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
