package com.example.guide.mockito;

import com.example.guide.util.Calculator;
import com.example.guide.util.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * STEP 6 of the guide: SPIES.
 *
 * A mock starts EMPTY: every method returns a default (null/0/empty) unless
 * you stub it. A spy wraps a REAL object: every method calls the real
 * implementation UNLESS you explicitly override it. Use a spy when you want
 * mostly-real behavior with one or two methods faked — e.g. to avoid a slow
 * or side-effecting call while keeping the rest of the object's real logic.
 *
 * Prefer plain mocks over spies whenever possible: spies make it easy to
 * accidentally invoke real logic (including I/O) and their partial-mocking
 * behavior is easy to get wrong (see the two "gotcha" tests below).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Spies (partial mocking)")
class SpyTest {

    @Spy
    private Calculator calculatorSpy; // Mockito instantiates Calculator() and wraps it

    @Spy
    private List<String> listSpy = new ArrayList<>(); // spying an existing instance

    @Test
    @DisplayName("unstubbed spy methods call the REAL implementation")
    void unstubbedMethodsCallRealCode() {
        // No stubbing at all — this really executes Calculator.add().
        int result = calculatorSpy.add(2, 3);

        assertThat(result).isEqualTo(5);
        verify(calculatorSpy).add(2, 3);
    }

    @Test
    @DisplayName("a specific method can be overridden while the rest stay real")
    void oneMethodOverriddenRestReal() {
        doReturn(999).when(calculatorSpy).multiply(anyInt(), anyInt());

        assertThat(calculatorSpy.multiply(2, 3)).isEqualTo(999); // faked
        assertThat(calculatorSpy.add(2, 3)).isEqualTo(5);        // still real
    }

    @Test
    @DisplayName("GOTCHA: use doReturn().when(), not when().thenReturn(), to stub a spy")
    void useDoReturnNotWhenThenReturnOnSpies() {
        // when(spy.divide(10, 0)).thenReturn(...) would actually EXECUTE
        // calculatorSpy.divide(10, 0) first to set up the stub — and here
        // that throws ArithmeticException before stubbing ever completes.
        // doReturn(...).when(spy)... never calls the real method at all.
        doReturn(-1.0).when(calculatorSpy).divide(10, 0);

        assertThat(calculatorSpy.divide(10, 0)).isEqualTo(-1.0);
    }

    @Test
    @DisplayName("spying a real, stateful object: real state is preserved")
    void spyingARealCollection() {
        listSpy.add("a");
        listSpy.add("b");

        assertThat(listSpy).containsExactly("a", "b"); // real ArrayList behavior
        verify(listSpy, times(2)).add(anyString());
    }

    @Test
    @DisplayName("Mockito.spy(realObject) — spying without the @Spy annotation")
    void manualSpyCreation() {
        StringUtils.class.getName(); // StringUtils is static-only; spy a stateful object instead
        List<String> real = new ArrayList<>(List.of("x", "y"));
        List<String> spy = Mockito.spy(real);

        doReturn(42).when(spy).size(); // override just size()

        assertThat(spy.size()).isEqualTo(42);      // faked
        assertThat(spy.get(0)).isEqualTo("x");     // still delegates to the real list
    }
}
