package org.interactiverobotics.grocery.repository;

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * ShoppingList repository.
 */
public interface ShoppingListRepository extends PagingAndSortingRepository<ShoppingList, Long> {
}
