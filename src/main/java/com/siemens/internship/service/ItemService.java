package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * It represents the Business Logic of our application, by providing multiple methods for processing information
 * related to our application.
 * */
@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    /*
    - Here we have some issues, because we aim to use an Asyncronous method, we cannot use types that are not thread-safe.
    - Either int and List<> are not thread safe, and this leads to concurrency problem when these shared variables are
    modified by different threads.
    - To prevent thread-safe we have to use Atomic variables and Concurrent Colletions to be to assure thread-safety.
    For example, instead of int we could use AtomicInteger, and instead of List<Item> we could use ConcurrentLinkedQueue or
    CopyOnWriteArrayList.
    */
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;

    /**
     * Returns a list consisting of all items.
     * */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /**
     * Returns an item found by its ID.
     * */
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Saves an item in the database.
     * */
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    /**
     * Deletes an item based on its ID.
     * */
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /*
    This method contains several problems, which I have highlighted in this code:

    1. The returning type is wrong, because we have to return a CompletableFuture<List<Item>>, because it is an
    Asyncronous method.
    * @Async
    public List<Item> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        for (Long id : itemIds) {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(100);

                    Item item = itemRepository.findById(id).orElse(null);
                    if (item == null) {
                        return;
                    }

                    2. This increment is not correct because it is not thread-safe.
                       We have to use AtomicInteger to be able to do this implementation safe.
                    processedCount++;

                    item.setStatus("PROCESSED");
                    itemRepository.save(item);

                    3. Non thread-safe collection is used for processedItems.
                    processedItems.add(item);

                } catch (InterruptedException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }, executor);
        }

        4. This does not wait for finishing all Asynchronous tasks, so the returned list might be incomplete.
        It returns immediately after one async task finished.
        return processedItems;
    }
    * */

    /**
     * This method processes the items asynchronously.
     * @return A CompletableFuture List of Items which contains all processed tasks.
     * */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        /*
        * We create a List containing all items by their IDs.
        * By collecting all items in the first step, we reduce substantially the execution time.
        * Thus it is memory costing, for small data sets, it is the best practise for a small application.
        * */
        List<Long> itemIds = itemRepository.findAllIds();

        /*
        * I have used a concurrent data structure for saving the processed items, because we have to ensure
        * thread safety when we run this on multiple threads.
        * I found ConcurrentListQueue to be the most suitable for this case, because it has a reduced time comparing
        * to other concurrent structures.
        * */
        ConcurrentLinkedQueue<Item> processedItems = new ConcurrentLinkedQueue<>();

        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    try {
                        /*
                            I have used an Optional<Item> to prevent an exception while processing each Item
                            if the ID is invalid or the Item is not present.
                         */
                        Optional<Item> optionalItem = itemRepository.findById(id);
                        if (optionalItem.isPresent()) {
                            Item item = optionalItem.get();
                            item.setStatus("PROCESSED");
                            Item saved = itemRepository.save(item);
                            processedItems.add(saved);
                        }
                    } catch (Exception e) {
                        // If something went wrong, we send this exception to the runtime.
                        throw new RuntimeException("Error processing item with ID " + id, e);
                    }
                }, executor))
                .toList();

        /*
        * Wait until all tasks have finished their work and then we collect them into a single CompletableFuture.
        * We want to return a list of completed items.
        * If something happens during the procession, we handle that exception, by sending it to the Runtime.
        * */
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> List.copyOf(processedItems))
                .exceptionally(e -> {
                    throw new CompletionException(e);
                });
    }

}

