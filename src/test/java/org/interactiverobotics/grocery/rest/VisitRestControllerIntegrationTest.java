/*
 * VisitRestControllerIntegrationTest.java
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
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.interactiverobotics.grocery.repository.VisitRepository;
import org.junit.After;
import org.junit.Before;
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

import static org.junit.Assert.*;

/**
 * Visit REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VisitRestControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private VisitRepository visitRepository;

    private Shop shop;


    @Before
    public void setUp() throws Exception {
        shop = shopRepository.save(new Shop("test-shop"));
    }

    @After
    public void tearDown() throws Exception {
        shopRepository.delete(shop);
    }


    @Test
    public void testGetVisits() {

        final List<Visit> existingVisits = new ArrayList<>();
        visitRepository.save(Arrays.asList(new Visit(shop), new Visit(shop)))
                .forEach(visit -> existingVisits.add(visit));

        final ResponseEntity<Visit[]> response = restTemplate.getForEntity("/api/v1/visit/", Visit[].class);

        visitRepository.delete(existingVisits);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingVisits, Arrays.asList(response.getBody()));
    }

    @Test
    public void testGetVisitById() {

        final Visit existingVisit = visitRepository.save(new Visit(shop));

        final ResponseEntity<Visit> response = restTemplate
                .getForEntity("/api/v1/visit/" + existingVisit.getId(), Visit.class);

        visitRepository.delete(existingVisit);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingVisit, response.getBody());
    }

    @Test
    public void testGetNotExistingVisitById() {

        final ResponseEntity<Visit> response = restTemplate
                .getForEntity("/api/v1/visit/" + new Long(999L), Visit.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetVisitsByShopId() {

        final List<Visit> existingVisits = new ArrayList<>();
        visitRepository.save(Arrays.asList(new Visit(shop), new Visit(shop)))
                .forEach(visit -> existingVisits.add(visit));

        final ResponseEntity<Visit[]> response = restTemplate
                .getForEntity("/api/v1/visit/shop/" + shop.getId(), Visit[].class);

        visitRepository.delete(existingVisits);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingVisits, Arrays.asList(response.getBody()));
    }

    @Test
    public void testGetVisitsByNotExistingShopId() {

        final ResponseEntity<Object> response = restTemplate
                .getForEntity("/api/v1/visit/shop/" + new Long(999L), Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testCreateVisit() {

        final ResponseEntity<Visit> response = restTemplate
                .postForEntity("/api/v1/visit/shop/" + shop.getId(), null, Visit.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        visitRepository.delete(response.getBody());
    }

    @Test
    public void testCreateVisitsForNotExistingShop() {

        final ResponseEntity<Object> response = restTemplate
                .postForEntity("/api/v1/visit/shop/" + new Long(999L), null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testStartVisit() {

        final Visit existingVisit = visitRepository.save(new Visit(shop));

        final ResponseEntity<Visit> response = restTemplate
                .postForEntity("/api/v1/visit/" + existingVisit.getId() + "/start", null, Visit.class);

        visitRepository.delete(existingVisit);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingVisit.getShop(), response.getBody().getShop());
        assertNotNull(response.getBody().getStarted());
    }

    @Test
    public void testStartNotExistingVisit() {

        final ResponseEntity<Object> response = restTemplate
                .postForEntity("/api/v1/visit/" + new Long(999L) + "/start", null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testCompleteVisit() {

        final Visit existingVisit = visitRepository.save(new Visit(shop));

        final ResponseEntity<Visit> response = restTemplate
                .postForEntity("/api/v1/visit/" + existingVisit.getId() + "/complete", null, Visit.class);

        visitRepository.delete(existingVisit);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingVisit.getShop(), response.getBody().getShop());
        assertNotNull(response.getBody().getStarted());
        assertNotNull(response.getBody().getCompleted());
    }

    @Test
    public void testCompleteNotExistingVisit() {

        final ResponseEntity<Object> response = restTemplate
                .postForEntity("/api/v1/visit/" + new Long(999L) + "/complete", null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeleteVisit() {

        final Visit existingVisit = visitRepository.save(new Visit(shop));

        restTemplate.delete("/api/v1/visit/" + existingVisit.getId());

        assertNull(visitRepository.findOne(existingVisit.getId()));
    }

    @Test
    public void testDeleteNotExistingVisit() {

        final ResponseEntity<?> response = restTemplate
                .exchange("/api/v1/visit/" + new Long(999L), HttpMethod.DELETE, null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
