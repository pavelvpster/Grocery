package org.interactiverobotics.grocery.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.form.ShoppingListForm;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
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
 * ShoppingList service test.
 * Tests Service class with mocked Repository.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    private ShoppingListService shoppingListService;


    @Before
    public void setUp() throws Exception {
        shoppingListService = new ShoppingListService(shoppingListRepository);
    }


    @Test
    public void testGetShoppingLists() {

        final List<ShoppingList> existingShoppingLists = Arrays.asList(
                new ShoppingList(1L, "test-shopping-list-1"), new ShoppingList(2L, "test-shopping-list-2"));
        when(shoppingListRepository.findAll()).thenReturn(existingShoppingLists);

        final List<ShoppingList> shoppingLists = shoppingListService.getShoppingLists();

        assertEquals(existingShoppingLists, shoppingLists);
    }


    public static class ShoppingListPageAnswer implements Answer<Page<ShoppingList>> {

        private final List<ShoppingList> shoppingLists;

        public ShoppingListPageAnswer(final List<ShoppingList> shoppingLists) {
            this.shoppingLists = shoppingLists;
        }

        @Override
        public Page<ShoppingList> answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            final Pageable pageable = invocation.getArgumentAt(0, Pageable.class);
            return new PageImpl<>(shoppingLists, pageable, shoppingLists.size());
        }
    }


    @Test
    public void testGetShoppingListsPage() {

        final List<ShoppingList> existingShoppingLists = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShoppingLists.add(new ShoppingList(i, "test-shopping-list-" + i));
        }

        final ShoppingListPageAnswer shoppingListPageAnswer = new ShoppingListPageAnswer(existingShoppingLists);
        when(shoppingListRepository.findAll(any(Pageable.class))).thenAnswer(shoppingListPageAnswer);

        final Page<ShoppingList> shoppingLists = shoppingListService.getShoppingLists(new PageRequest(0, 10));

        assertEquals(existingShoppingLists.size(), shoppingLists.getTotalElements());
        assertEquals(10, shoppingLists.getTotalPages());
    }

    @Test
    public void testGetShoppingListById() {

        ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        when(shoppingListRepository.findOne(existingShoppingList.getId())).thenReturn(existingShoppingList);

        final ShoppingList shoppingList = shoppingListService.getShoppingListById(existingShoppingList.getId());

        assertEquals(existingShoppingList, shoppingList);
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testGetNotExistingShoppingListById() {

        when(shoppingListRepository.findOne(any())).thenReturn(null);

        shoppingListService.getShoppingListById(1L);
    }

    @Test
    public void testGetShoppingListByName() {

        ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        when(shoppingListRepository.findOneByName(existingShoppingList.getName())).thenReturn(existingShoppingList);

        final ShoppingList shoppingList = shoppingListService.getShoppingListByName("test-shopping-list");

        assertEquals(existingShoppingList, shoppingList);
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testGetNotExistingShoppingListByName() {

        when(shoppingListRepository.findOne(any())).thenReturn(null);

        shoppingListService.getShoppingListByName("test-name");
    }


    public static class SaveAndReturnShoppingListAnswer implements Answer<ShoppingList> {

        private ShoppingList shoppingList;

        public ShoppingList getShoppingList() {
            return shoppingList;
        }

        @Override
        public ShoppingList answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            shoppingList = invocation.getArgumentAt(0, ShoppingList.class);
            if (shoppingList.getId() == null) {
                shoppingList.setId(1L);
            }
            return shoppingList;
        }
    }


    @Test
    public void testCreateShoppingList() {

        final SaveAndReturnShoppingListAnswer saveAndReturnShoppingListAnswer = new SaveAndReturnShoppingListAnswer();
        when(shoppingListRepository.save(any(ShoppingList.class))).then(saveAndReturnShoppingListAnswer);

        final ShoppingListForm form = new ShoppingListForm("test-shopping-list");

        final ShoppingList shoppingList = shoppingListService.createShoppingList(form);

        // Check that Service returns what was saved
        final ShoppingList savedShoppingList = saveAndReturnShoppingListAnswer.getShoppingList();
        assertEquals(savedShoppingList, shoppingList);

        // Check response content
        assertEquals(form.getName(), shoppingList.getName());
    }

    @Test
    public void testUpdateShoppingList() {

        ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        when(shoppingListRepository.findOne(existingShoppingList.getId())).thenReturn(existingShoppingList);

        final SaveAndReturnShoppingListAnswer saveAndReturnShoppingListAnswer = new SaveAndReturnShoppingListAnswer();
        when(shoppingListRepository.save(any(ShoppingList.class))).then(saveAndReturnShoppingListAnswer);

        final ShoppingListForm form = new ShoppingListForm("updated-test-shopping-list");

        final ShoppingList shoppingList = shoppingListService.updateShoppingList(existingShoppingList.getId(), form);

        // Check that Service returns what was saved
        final ShoppingList savedShoppingList = saveAndReturnShoppingListAnswer.getShoppingList();
        assertEquals(savedShoppingList, shoppingList);

        // Check response content
        assertEquals(form.getName(), shoppingList.getName());
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testUpdateNotExistingShoppingList() {

        when(shoppingListRepository.findOne(any())).thenReturn(null);

        final ShoppingListForm form = new ShoppingListForm("updated-test-shopping-list");

        shoppingListService.updateShoppingList(999L, form);
    }

    @Test
    public void testDeleteShoppingList() {

        ShoppingList existingShoppingList = new ShoppingList(1L, "test-shopping-list");
        when(shoppingListRepository.findOne(existingShoppingList.getId())).thenReturn(existingShoppingList);

        shoppingListService.deleteShoppingList(existingShoppingList.getId());

        verify(shoppingListRepository).delete(eq(existingShoppingList));
    }

    @Test(expected = ShoppingListNotFoundException.class)
    public void testDeleteNotExistingShoppingList() {

        when(shoppingListRepository.findOne(any())).thenReturn(null);

        shoppingListService.deleteShoppingList(999L);
    }

}
