package org.interactiverobotics.grocery.repository;

import org.interactiverobotics.grocery.domain.ShoppingListItem;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * ShoppingListItem repository.
 */
public interface ShoppingListItemRepository extends PagingAndSortingRepository<ShoppingListItem, Long> {
}
