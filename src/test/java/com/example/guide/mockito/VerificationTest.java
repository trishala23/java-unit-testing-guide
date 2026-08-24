package com.example.guide.mockito;

import com.example.guide.order.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * STEP 5 of the guide: VERIFICATION.
 *
 * Stubbing (STEP 3) controls what a mock returns. Verification checks
 * what a mock was CALLED WITH — useful when the method under test doesn't
 * return a value you can assert on, but has an important side effect
 * (send an email, save a record, charge a card...).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Verification with Mockito")
class VerificationTest {

    @Mock private InventoryService inventoryService;
    @Mock private PaymentGateway paymentGateway;
    @Mock private OrderRepository orderRepository;
    @Mock private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor; // @Captor is equivalent to ArgumentCaptor.forClass(Order.class)

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(inventoryService, paymentGateway, orderRepository, notificationService);
    }

    private Order sampleOrder() {
        return new Order("buyer@example.com", "SKU-1", 2, new BigDecimal("49.99"));
    }

    @Test
    @DisplayName("verify(mock).method(args) checks a call happened with specific arguments")
    void verifiesExactCall() {
        when(inventoryService.reserveStock("SKU-1", 2)).thenReturn(true);
        when(paymentGateway.charge(eq("buyer@example.com"), any(BigDecimal.class)))
                .thenReturn(new PaymentGateway.PaymentResult(true, "txn-1"));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(sampleOrder());

        verify(paymentGateway).charge("buyer@example.com", new BigDecimal("49.99"));
    }

    @Test
    @DisplayName("verify(mock, times(n)) asserts a call count")
    void verifiesCallCount() {
        when(inventoryService.reserveStock(anyString(), anyInt())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(sampleOrder());

        // Stock reservation failed, so payment must NEVER be attempted.
        verify(paymentGateway, never()).charge(anyString(), any(BigDecimal.class));
        verify(inventoryService, times(1)).reserveStock("SKU-1", 2);
        verify(notificationService, times(1)).sendOrderFailure(eq("buyer@example.com"), anyString());
    }

    @Test
    @DisplayName("times(), atLeast(), atMost(), atLeastOnce() cover different frequency needs")
    void verifiesFrequencyVariants() {
        when(inventoryService.reserveStock(anyString(), anyInt())).thenReturn(true);
        when(paymentGateway.charge(anyString(), any(BigDecimal.class)))
                .thenReturn(new PaymentGateway.PaymentResult(true, "txn-2"));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(sampleOrder());
        orderService.placeOrder(sampleOrder());

        verify(orderRepository, times(2)).save(any(Order.class));
        verify(inventoryService, atLeastOnce()).reserveStock(anyString(), anyInt());
        verify(inventoryService, atLeast(1)).reserveStock(anyString(), anyInt());
        verify(inventoryService, atMost(5)).reserveStock(anyString(), anyInt());
    }

    @Test
    @DisplayName("InOrder verifies calls happened in a specific relative sequence")
    void verifiesOrderOfCalls() {
        when(inventoryService.reserveStock(anyString(), anyInt())).thenReturn(true);
        when(paymentGateway.charge(anyString(), any(BigDecimal.class)))
                .thenReturn(new PaymentGateway.PaymentResult(true, "txn-3"));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(sampleOrder());

        InOrder inOrder = inOrder(inventoryService, paymentGateway, orderRepository, notificationService);
        inOrder.verify(inventoryService).reserveStock(anyString(), anyInt());
        inOrder.verify(paymentGateway).charge(anyString(), any(BigDecimal.class));
        inOrder.verify(orderRepository).save(any(Order.class));
        inOrder.verify(notificationService).sendOrderConfirmation(anyString(), anyString());
    }

    @Test
    @DisplayName("ArgumentCaptor captures the actual argument passed for deeper assertions")
    void capturesArgumentForInspection() {
        when(inventoryService.reserveStock(anyString(), anyInt())).thenReturn(true);
        when(paymentGateway.charge(anyString(), any(BigDecimal.class)))
                .thenReturn(new PaymentGateway.PaymentResult(true, "txn-4"));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(sampleOrder());

        verify(orderRepository).save(orderCaptor.capture());
        Order captured = orderCaptor.getValue();
        assertThat(captured.getStatus()).isEqualTo(Order.Status.PAID);
        assertThat(captured.getId()).isNotBlank();
    }

    @Test
    @DisplayName("verifyNoInteractions / verifyNoMoreInteractions guard against unexpected side effects")
    void verifiesNoUnexpectedInteractions() {
        when(inventoryService.reserveStock(anyString(), anyInt())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(sampleOrder());

        // Payment must be completely untouched when stock reservation fails.
        verifyNoInteractions(paymentGateway);

        // And once we've verified everything we expect on inventoryService,
        // assert nothing else happened on it either.
        verify(inventoryService).reserveStock("SKU-1", 2);
        verifyNoMoreInteractions(inventoryService);
    }

    @Test
    @DisplayName("argument matchers (any, eq, argThat) let you verify flexible conditions")
    void verifiesWithCustomArgumentMatcher() {
        when(inventoryService.reserveStock(anyString(), anyInt())).thenReturn(true);
        when(paymentGateway.charge(anyString(), any(BigDecimal.class)))
                .thenReturn(new PaymentGateway.PaymentResult(true, "txn-5"));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(sampleOrder());

        verify(paymentGateway).charge(anyString(),
                argThat(amount -> amount.compareTo(new BigDecimal("49.99")) == 0));
    }
}
