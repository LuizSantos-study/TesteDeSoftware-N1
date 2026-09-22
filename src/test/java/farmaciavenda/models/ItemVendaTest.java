/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package farmaciavenda.models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.TestInstance;

/**
 *
 * @author gabriella
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ItemVendaTest {
    
    private static Produto produto = new Produto();
    private static ItemVenda item;
    
    public ItemVendaTest() {
    }
    
    @BeforeAll
    public void setUpClass() {
        this.produto.setPreco(new BigDecimal("25.00"));
        this.item = new ItemVenda(produto, 3);
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Testa o cálculo do subtotal do item com base no preço e na quantidade.
     */
    @Test
    public void testCalcularSubtotal() {
        assertEquals(new BigDecimal("75.00"), item.calcularSubtotal());
        System.out.println("PASSOU :) Subtotal calculado corretamente."
    );
    }
    
    /**
     * Testa o bloqueio de uma quantidade vendida igual a zero.
     */
    @Test
    public void testQuantidadeVendidaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ItemVenda(produto, 0);
        });
        System.out.println("PASSOU :) Quantidade inválida (zero) foi bloqueada."
        );
    }
}
