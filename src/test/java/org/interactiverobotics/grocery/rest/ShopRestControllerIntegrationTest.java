/*
 * ShopRestControllerIntegrationTest.java
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

package org.interactiverobotics.grocery.rest;

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.form.ShopForm;
import org.interactiverobotics.grocery.repository.ShopRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Shop REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShopRestControllerIntegrationTest {

    private static final String SHOP_ENDPOINT = "/api/v1/shop/";
    private static final String TEST_SHOP_NAME = "test-shop";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShopRepository shopRepository;


    @Before
    public void setUp() throws Exception {
        shopRepository.deleteAll();
    }


    @Test
    public void testGetShops() {

        final List<Shop> existingShops = new ArrayList<>();
        shopRepository.saveAll(Arrays.asList(new Shop("test-shop-1"), new Shop("test-shop-2")))
                .forEach(shop -> existingShops.add(shop));

        final ResponseEntity<Shop[]> response = restTemplate.getForEntity(SHOP_ENDPOINT, Shop[].class);

        shopRepository.deleteAll(existingShops);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShops, Arrays.asList(response.getBody()));
    }

    @Test
    public void testGetShopsPage() {

        final List<Shop> existingShops = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShops.add(shopRepository.save(new Shop("test-shop-" + i)));
        }

        final ParameterizedTypeReference<PageResponse<Shop>> responseType =
                new ParameterizedTypeReference<PageResponse<Shop>>() {};
        final ResponseEntity<PageResponse<Shop>> response = restTemplate.exchange(SHOP_ENDPOINT + "list?page=1&size=10",
                HttpMethod.GET, null, responseType);

        shopRepository.deleteAll(existingShops);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShops.size(), response.getBody().getTotalElements());
        assertEquals(10, response.getBody().getTotalPages());
        assertEquals(10, response.getBody().getSize());
    }

    @Test
    public void testGetShopById() {

        final Shop existingShop = shopRepository.save(new Shop(TEST_SHOP_NAME));

        final ResponseEntity<Shop> response = restTemplate
                .getForEntity(SHOP_ENDPOINT + existingShop.getId(), Shop.class);

        shopRepository.delete(existingShop);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShop, response.getBody());
    }

    @Test
    public void testGetNotExistingShopById() {

        final ResponseEntity<Shop> response = restTemplate.getForEntity(SHOP_ENDPOINT + new Long(999L), Shop.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetShopByName() {

        final Shop existingShop = shopRepository.save(new Shop(TEST_SHOP_NAME));

        final ResponseEntity<Shop> response = restTemplate
                .getForEntity(SHOP_ENDPOINT + "search?name=" + existingShop.getName(), Shop.class);

        shopRepository.delete(existingShop);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShop, response.getBody());
    }

    @Test
    public void testGetNotExistingShopByName() {

        final ResponseEntity<Shop> response = restTemplate.getForEntity(SHOP_ENDPOINT + "search?name=test", Shop.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testCreateShop() {

        final ShopForm form = new ShopForm();
        form.setName(TEST_SHOP_NAME);

        final ResponseEntity<Shop> response = restTemplate.postForEntity(SHOP_ENDPOINT, form, Shop.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        shopRepository.delete(response.getBody());

        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void testUpdateShop() {

        final Shop existingShop = shopRepository.save(new Shop(TEST_SHOP_NAME));

        final ShopForm form = new ShopForm();
        form.setName("updated-test-shop");

        final ResponseEntity<Shop> response = restTemplate
                .postForEntity(SHOP_ENDPOINT + existingShop.getId(), form, Shop.class);

        shopRepository.delete(existingShop);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShop.getId(), response.getBody().getId());
        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void testUpdateNotExistingShop() {

        final ShopForm form = new ShopForm();
        form.setName("updated-test-shop");

        final ResponseEntity<Shop> response = restTemplate
                .postForEntity(SHOP_ENDPOINT + new Long(999L), form, Shop.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeleteShop() {

        final Shop existingShop = shopRepository.save(new Shop(TEST_SHOP_NAME));

        restTemplate.delete(SHOP_ENDPOINT + existingShop.getId());

        assertFalse(shopRepository.findById(existingShop.getId()).isPresent());
    }

    @Test
    public void testDeleteNotExistingShop() {

        final ResponseEntity<?> response = restTemplate
                .exchange(SHOP_ENDPOINT + new Long(999L), HttpMethod.DELETE, null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
