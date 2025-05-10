package com.siemens.internship.controller;

import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Handles the endpoints of our application, by providing methods for each endpoint or entrypoint.
 * */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id,@Valid @RequestBody Item item, BindingResult result) {
        // If the element is not valid, we will return a BAD_REQUEST
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        if (itemService.findById(id).isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /*
    * Here before deleting an item, we have to make sure that this exists in our database.
    * This could be done either here, or in the deleteById() method.
    * - If success -> ResponseEntity = 204 (NO_CONTENT) or 200 (OK).
    * - If it failed -> ResponseENtity = 404 (NOT_FOUND) or 400 (BAD_REQUEST).
    * */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            itemService.deleteById(existingItem.get().getId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /*
    * We have to treat each case separate.
    * 1. If everything works fine, we will return OK.
    * 2. If there are some exceptions, we will catch them and will return 500 (internal server error).
    * Eventually, we can also interrupt the thread.
    * */
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        try {
            List<Item> processedItems = itemService.processItemsAsync().get();
            return ResponseEntity.ok(processedItems);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
