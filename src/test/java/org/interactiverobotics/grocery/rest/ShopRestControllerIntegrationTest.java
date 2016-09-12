/*
 * ShopRestControllerIntegrationTest.java
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

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.form.ShopForm;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Shop REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShopRestControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShopRepository shopRepository;


    @Test
    public void testGetShops() {

        final List<Shop> existingShops = new ArrayList<>();
        shopRepository.save(Arrays.asList(new Shop("test-shop-1"), new Shop("test-shop-2")))
                .forEach(shop -> existingShops.add(shop));

        final ResponseEntity<Shop[]> response = restTemplate.getForEntity("/api/v1/shop/", Shop[].class);

        shopRepository.delete(existingShops);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShops, Arrays.asList(response.getBody()));
    }

    @Test
    public void testGetShopById() {

        final Shop existingShop = shopRepository.save(new Shop("test-shop"));

        final ResponseEntity<Shop> response = restTemplate
                .getForEntity("/api/v1/shop/" + existingShop.getId(), Shop.class);

        shopRepository.delete(existingShop);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShop, response.getBody());
    }

    @Test
    public void testGetNotExistingShopById() {

        final ResponseEntity<Shop> response = restTemplate.getForEntity("/api/v1/shop/" + new Long(999L), Shop.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetShopByName() {

        final Shop existingShop = shopRepository.save(new Shop("test-shop"));

        final ResponseEntity<Shop> response = restTemplate
                .getForEntity("/api/v1/shop/search?name=" + existingShop.getName(), Shop.class);

        shopRepository.delete(existingShop);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingShop, response.getBody());
    }

    @Test
    public void testGetNotExistingShopByName() {

        final ResponseEntity<Shop> response = restTemplate.getForEntity("/api/v1/shop/search?name=test", Shop.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testCreateShop() {

        final ShopForm form = new ShopForm();
        form.setName("test-shop");

        final ResponseEntity<Shop> response = restTemplate.postForEntity("/api/v1/shop/", form, Shop.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        shopRepository.delete(response.getBody());

        assertEquals(form.getName(), response.getBody().getName());
    }

    @Test
    public void testUpdateShop() {

        final Shop existingShop = shopRepository.save(new Shop("test-shop"));

        final ShopForm form = new ShopForm();
        form.setName("updated-test-shop");

        final ResponseEntity<Shop> response = restTemplate
                .postForEntity("/api/v1/shop/" + existingShop.getId(), form, Shop.class);

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
                .postForEntity("/api/v1/shop/" + new Long(999L), form, Shop.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeleteShop() {

        final Shop existingShop = shopRepository.save(new Shop("test-shop"));

        restTemplate.delete("/api/v1/shop/" + existingShop.getId());

        assertNull(shopRepository.findOne(existingShop.getId()));
    }

    @Test
    public void testDeleteNotExistingShop() {

        final ResponseEntity<?> response = restTemplate
                .exchange("/api/v1/shop/" + new Long(999L), HttpMethod.DELETE, null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
