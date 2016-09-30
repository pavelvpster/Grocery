package org.interactiverobotics.grocery.rest;

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.service.ShoppingListService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ShoppingList REST controller test.
 * Tests Controller with mocked Service.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ShoppingListRestController.class)
public class ShoppingListControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ShoppingListService shoppingListService;


    @Test
    public void testGetShoppingLists() throws Exception {

        final List<ShoppingList> existingShoppingList = Arrays.asList(
                new ShoppingList(1L, "shopping-list-1"), new ShoppingList(2L, "shopping-list-2"));
        when(shoppingListService.getShoppingLists()).thenReturn(existingShoppingList);

        mvc.perform(get("/api/v1/shopping_list/").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(existingShoppingList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(existingShoppingList.get(0).getName())))
                .andExpect(jsonPath("$[1].id", is(existingShoppingList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(existingShoppingList.get(1).getName())));
    }

}
