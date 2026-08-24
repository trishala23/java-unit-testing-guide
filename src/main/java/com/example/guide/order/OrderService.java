package com.example.guide.order;

import java.util.UUID;

/**
 * Orchestrates four collaborators. This is the class the "verification"
 * and "spy" chapters lean on most: with several dependencies interacting
 * in sequence, it's a natural fit for {@code verify}, {@code InOrder}, and
 * {@code ArgumentCaptor}.
 */
public class OrderService {

    private final InventoryService inventoryService;
    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public OrderService(InventoryService inventoryService,
                         PaymentGateway paymentGateway,
                         OrderRepository orderRepository,
                         NotificationService notificationService) {
        this.inventoryService = inventoryService;
        this.paymentGateway = paymentGateway;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    public Order placeOrder(Order order) {
        boolean reserved = inventoryService.reserveStock(order.getSku(), order.getQuantity());
        if (!reserved) {
            order.setStatus(Order.Status.FAILED);
            notificationService.sendOrderFailure(order.getCustomerEmail(), "Out of stock");
            return orderRepository.save(order);
        }

        PaymentGateway.PaymentResult result = paymentGateway.charge(order.getCustomerEmail(), order.getAmount());

        if (!result.success()) {
            inventoryService.releaseStock(order.getSku(), order.getQuantity());
            order.setStatus(Order.Status.FAILED);
            notificationService.sendOrderFailure(order.getCustomerEmail(), "Payment declined");
            return orderRepository.save(order);
        }

        order.setId(UUID.randomUUID().toString());
        order.setStatus(Order.Status.PAID);
        Order saved = orderRepository.save(order);
        notificationService.sendOrderConfirmation(order.getCustomerEmail(), saved.getId());
        return saved;
    }
}
