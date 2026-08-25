package com.example.guide.order;

public interface InventoryService {

    boolean reserveStock(String sku, int quantity);

    void releaseStock(String sku, int quantity);
}
