package org.interactiverobotics.grocery.service;

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.exception.ShoppingListNotFoundException;
import org.interactiverobotics.grocery.form.ShoppingListForm;
import org.interactiverobotics.grocery.repository.ShoppingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /**
     * Returns page of ShoppingList(s).
     */
    public Page<ShoppingList> getShoppingLists(Pageable pageable) {
        return this.shoppingListRepository.findAll(pageable);
    }

    /**
     * Returns ShoppingList by Id.
     */
    public ShoppingList getShoppingListById(final Long shoppingListId) {
        return Optional.ofNullable(this.shoppingListRepository.findOne(shoppingListId))
                .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId));
    }

    /**
     * Returns ShoppingList by Name.
     */
    public ShoppingList getShoppingListByName(final String name) {
        return Optional.ofNullable(this.shoppingListRepository.findOneByName(name))
                .orElseThrow(() -> new ShoppingListNotFoundException(-1L));
    }

    /**
     * Creates ShoppingList.
     */
    public ShoppingList createShoppingList(final ShoppingListForm form) {
        return this.shoppingListRepository.save(new ShoppingList(form.getName()));
    }

    /**
     * Updates ShoppingList.
     */
    public ShoppingList updateShoppingList(final Long shoppingListId, final ShoppingListForm form) {
        final ShoppingList shoppingList = Optional.ofNullable(this.shoppingListRepository.findOne(shoppingListId))
                .orElseThrow(() -> new ShoppingListNotFoundException(shoppingListId));
        shoppingList.setName(form.getName());
        return this.shoppingListRepository.save(shoppingList);
    }

    /**
     * Deletes ShoppingList.
     */
    public void deleteShoppingList(final Long itemId) {
        final ShoppingList shoppingList = Optional.ofNullable(this.shoppingListRepository.findOne(itemId))
                .orElseThrow(() -> new ShoppingListNotFoundException(itemId));
        this.shoppingListRepository.delete(shoppingList);
    }

}
