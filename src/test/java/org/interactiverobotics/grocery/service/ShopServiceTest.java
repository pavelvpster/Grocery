/*
 * ShopServiceTest.java
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.exception.ShopNotFoundException;
import org.interactiverobotics.grocery.form.ShopForm;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Shop service test.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    private ShopService shopService;


    @Before
    public void setUp() throws Exception {
        shopService = new ShopService(shopRepository);
    }


    @Test
    public void testGetShops() {

        final List<Shop> existingShops = Arrays.asList(new Shop(1L, "test-shop-1"), new Shop(2L, "test-shop-2"));
        when(shopRepository.findAll()).thenReturn(existingShops);

        final List<Shop> shops = shopService.getShops();

        assertEquals(existingShops, shops);
    }


    public static class ShopPageAnswer implements Answer<Page<Shop>> {

        private final List<Shop> shops;

        public ShopPageAnswer(final List<Shop> shops) {
            this.shops = shops;
        }

        @Override
        public Page<Shop> answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            final Pageable pageable = invocation.getArgumentAt(0, Pageable.class);
            return new PageImpl<>(shops, pageable, shops.size());
        }
    }


    @Test
    public void testGetShopsPage() {

        final List<Shop> existingShops = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShops.add(new Shop(i, "test-shop-" + i));
        }

        final ShopPageAnswer shopPageAnswer = new ShopPageAnswer(existingShops);
        when(shopRepository.findAll(any(Pageable.class))).thenAnswer(shopPageAnswer);

        final Page<Shop> shops = shopService.getShops(new PageRequest(0, 10));

        assertEquals(existingShops.size(), shops.getTotalElements());
        assertEquals(10, shops.getTotalPages());
    }

    @Test
    public void testGetShopById() {

        Shop existingShop = new Shop(1L, "test-shop");
        when(shopRepository.findOne(existingShop.getId())).thenReturn(existingShop);

        final Shop shop = shopService.getShopById(existingShop.getId());

        assertEquals(existingShop, shop);
    }

    @Test(expected = ShopNotFoundException.class)
    public void testGetNotExistingShopById() {

        when(shopRepository.findOne(any())).thenReturn(null);

        shopService.getShopById(1L);
    }

    @Test
    public void testGetShopByName() {

        Shop existingShop = new Shop(1L, "test-shop");
        when(shopRepository.findOneByName(existingShop.getName())).thenReturn(existingShop);

        final Shop shop = shopService.getShopByName("test-shop");

        assertEquals(existingShop, shop);
    }

    @Test(expected = ShopNotFoundException.class)
    public void testGetNotExistingShopByName() {

        when(shopRepository.findOne(any())).thenReturn(null);

        shopService.getShopByName("test-name");
    }


    public static class SaveAndReturnShopAnswer implements Answer<Shop> {

        private Shop shop;

        public Shop getShop() {
            return shop;
        }

        @Override
        public Shop answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            shop = invocation.getArgumentAt(0, Shop.class);
            if (shop.getId() == null) {
                shop.setId(1L);
            }
            return shop;
        }
    }


    @Test
    public void testCreateShop() {

        final SaveAndReturnShopAnswer saveAndReturnShopAnswer = new SaveAndReturnShopAnswer();
        when(shopRepository.save(any(Shop.class))).then(saveAndReturnShopAnswer);

        final ShopForm form = new ShopForm("test-shop");

        final Shop shop = shopService.createShop(form);

        // Check that Service returns what was saved
        final Shop savedShop = saveAndReturnShopAnswer.getShop();
        assertEquals(savedShop, shop);

        // Check response content
        assertEquals(form.getName(), shop.getName());
    }

    @Test
    public void testUpdateShop() {

        Shop existingShop = new Shop(1L, "test-shop");
        when(shopRepository.findOne(existingShop.getId())).thenReturn(existingShop);

        final SaveAndReturnShopAnswer saveAndReturnShopAnswer = new SaveAndReturnShopAnswer();
        when(shopRepository.save(any(Shop.class))).then(saveAndReturnShopAnswer);

        final ShopForm form = new ShopForm("updated-test-shop");

        final Shop shop = shopService.updateShop(existingShop.getId(), form);

        // Check that Service returns what was saved
        final Shop savedShop = saveAndReturnShopAnswer.getShop();
        assertEquals(savedShop, shop);

        // Check response content
        assertEquals(form.getName(), shop.getName());
    }

    @Test(expected = ShopNotFoundException.class)
    public void testUpdateNotExistingShop() {

        when(shopRepository.findOne(any())).thenReturn(null);

        final ShopForm form = new ShopForm("updated-test-shop");

        shopService.updateShop(999L, form);
    }

    @Test
    public void testDeleteShop() {

        Shop existingShop = new Shop(1L, "test-shop");
        when(shopRepository.findOne(existingShop.getId())).thenReturn(existingShop);

        shopService.deleteShop(existingShop.getId());

        verify(shopRepository).delete(eq(existingShop));
    }

    @Test(expected = ShopNotFoundException.class)
    public void testDeleteNotExistingShop() {

        when(shopRepository.findOne(any())).thenReturn(null);

        shopService.deleteShop(999L);
    }

}
