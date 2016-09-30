package org.interactiverobotics.grocery.service;

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ShoppingList service.
 */
@Service
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;

    @Autowired
    public ShoppingListService(final ShoppingListRepository shoppingListRepository) {
        this.shoppingListRepository = shoppingListRepository;
    }

    /**
     * Returns ShoppingList(s).
     */
    public List<ShoppingList> getShoppingLists() {
        final List<ShoppingList> shoppingLists = new ArrayList<>();
        this.shoppingListRepository.findAll().forEach(shoppingList -> shoppingLists.add(shoppingList));
        return shoppingLists;
    }

}
