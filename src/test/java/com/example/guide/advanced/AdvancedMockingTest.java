package com.example.guide.advanced;

import com.example.guide.order.*;
import com.example.guide.user.User;
import com.example.guide.user.UserRepository;
import com.example.guide.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

/**
 * STEP 7 of the guide: ADVANCED MOCKING TECHNIQUES.
 *
 * Covers: throwing exceptions from a mock, consecutive/varying return
 * values across calls, custom Answer logic, the BDD-style given/when/then
 * API, and mocking static methods.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Advanced Mockito techniques")
class AdvancedMockingTest {

    @Mock private InventoryService inventoryService;
    @Mock private PaymentGateway paymentGateway;
    @Mock private OrderRepository orderRepository;
    @Mock private NotificationService notificationService;
    @Mock private UserRepository userRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(inventoryService, paymentGateway, orderRepository, notificationService);
    }

    @Test
    @DisplayName("thenThrow / doThrow makes a mock raise an exception")
    void mockThrowsException() {
        when(inventoryService.reserveStock(anyString(), anyInt()))
                .thenThrow(new IllegalStateException("Inventory service unavailable"));

        Order order = new Order("x@example.com", "SKU-9", 1, BigDecimal.TEN);

        assertThatThrownBy(() -> orderService.placeOrder(order))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Inventory service unavailable");
    }

    @Test
    @DisplayName("doThrow(...).when(mock) — required style for void methods")
    void voidMethodThrowsException() {
        UserService userService = new UserService(userRepository);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User(1L, "A", "a@x.com", true)));
        // save() returns User, so when().thenThrow() also works there, but
        // doThrow().when() is required whenever the stubbed method is void.
        doThrow(new RuntimeException("DB write failed")).when(userRepository).save(any(User.class));

        assertThatThrownBy(() -> userService.deactivateUser(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB write failed");
    }

    @Test
    @DisplayName("consecutive calls can return different values in sequence")
    void consecutiveReturnValues() {
        when(inventoryService.reserveStock(anyString(), anyInt()))
                .thenReturn(true)   // 1st call
                .thenReturn(false); // 2nd call and onward

        assertThat(inventoryService.reserveStock("SKU-1", 1)).isTrue();
        assertThat(inventoryService.reserveStock("SKU-1", 1)).isFalse();
        assertThat(inventoryService.reserveStock("SKU-1", 1)).isFalse(); // sticks on the last stub
    }

    @Test
    @DisplayName("a custom Answer computes the return value from the actual arguments")
    void customAnswerBasedOnArguments() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId("generated-" + order.getSku());
            return order;
        });

        Order saved = orderRepository.save(new Order("a@x.com", "SKU-7", 1, BigDecimal.ONE));

        assertThat(saved.getId()).isEqualTo("generated-SKU-7");
    }

    @Test
    @DisplayName("BDDMockito: given/when/then reads closer to Given-When-Then test structure")
    void bddStyleStubbingAndVerification() {
        // given
        given(inventoryService.reserveStock("SKU-2", 1)).willReturn(true);
        given(paymentGateway.charge(anyString(), any(BigDecimal.class)))
                .willReturn(new PaymentGateway.PaymentResult(true, "txn-bdd"));
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        orderService.placeOrder(new Order("bdd@example.com", "SKU-2", 1, BigDecimal.TEN));

        // then
        then(notificationService).should().sendOrderConfirmation(eq("bdd@example.com"), anyString());
    }

    @Test
    @DisplayName("MockedStatic mocks a static method for the duration of a try-with-resources block")
    void mockingStaticMethod() {
        Instant fixed = Instant.parse("2020-01-01T00:00:00Z");

        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {
            mockedInstant.when(Instant::now).thenReturn(fixed);

            assertThat(Instant.now()).isEqualTo(fixed);
        }
        // Outside the try-with-resources block, Instant.now() behaves normally again
        // (real, current time — which is after our fixed 2020 instant).
        assertThat(Instant.now()).isAfter(fixed);
    }
}
