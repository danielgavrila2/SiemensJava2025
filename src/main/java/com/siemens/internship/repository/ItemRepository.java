package com.siemens.internship.repository;

import com.siemens.internship.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Provides multiple methods for handling information through JPARepository.
 * */
public interface ItemRepository extends JpaRepository<Item, Long> {
    /**
     * Returns a list consisting of all items from the database.
     * */
    @Query("SELECT id FROM Item")
    List<Long> findAllIds();
}
