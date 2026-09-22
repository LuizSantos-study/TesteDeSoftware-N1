/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package farmaciavenda.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.TestInstance;

import farmaciavenda.enums.StatusVenda;
import farmaciavenda.enums.TipoProduto;
import farmaciavenda.exception.MedicamentoControladoException;
import farmaciavenda.exception.VendaFinalizadaException;
/**
 *
 * @author gabriella
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VendaTest {
    
    private static Produto prod1;
    private static Produto prod2;
    private static ItemVenda item1;    
    private static ItemVenda item2;

    
    public VendaTest() {
    }
    
    @BeforeAll
    public void setUpClass() {
        this.prod1 = new Produto(
            1L,
            "Rivotril",
            "Clonazepam",
            TipoProduto.REFERENCIA,
            true,
            new BigDecimal("30.00")
        );
        
        this.prod2 = new Produto(
            2L,
            "Dipirona",
            "Dipirona Sódica",
            TipoProduto.GENERICO,
            false,
            new BigDecimal("10.00")
        );
        
        this.item1 = new ItemVenda(prod1, 1);
        this.item2 = new ItemVenda(prod2, 2);
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
     * Testa o cálculo do subtotal considerando todos os itens da venda.
     */
    @Test
    public void testCalcularSubtotalItens() {
        Venda venda = new Venda();
        
        venda.adicionarItem(item1);
        venda.adicionarItem(item2);
        
        assertEquals(new BigDecimal("50.00"), venda.calcularSubtotalItens());
        
        System.out.println("PASSOU :) Subtotal de venda calculado.");
    }
    
    /**
     * Testa o cálculo do valor total da venda após a aplicação de um desconto.
     */
    @Test
    public void testAplicarDesconto() {
        Venda venda = new Venda();
        
        venda.adicionarItem(item1);
        venda.aplicarDesconto(new BigDecimal("5.00"));
        assertEquals(new BigDecimal("25.00"), venda.getValorTotal());
        
        System.out.println("PASSOU :) Valor com disconto calculado.");
    }
    
    /**
     * Testa a identificação de medicamentos controlados na venda.
     */
    @Test
    public void testPossuiMedicamentoControlado() {
        Venda venda = new Venda();
        venda.adicionarItem(item1);
        assertEquals(true, venda.possuiMedicamentoControlado());
        System.out.println("PASSOU :) A venda identificou medicamento controlado.");
    }

    /**
     * Testa o bloqueio da venda de medicamento controlado sem receita.
     */
    @Test
    public void testVendaMedicamentoControladoSemReceita() {
        Venda venda = new Venda();
        venda.adicionarItem(item1);
        
        assertThrows(MedicamentoControladoException.class, () -> {
            venda.finalizarVenda();
        });
        
        System.out.println("PASSOU :) Venda de medicamento controlado sem receita foi bloqueada.");
    }
    
    /**
     * Testa o bloqueio de alterações em uma venda já finalizada.
     */
    @Test
    public void testImpedirAlteracaoVendaFinalizada() {
        Venda venda = new Venda();
        venda.adicionarItem(item2);
        venda.setStatus(StatusVenda.FINALIZADA);
        assertThrows(VendaFinalizadaException.class, () -> {
            venda.adicionarItem(item2);
        });
    
        System.out.println("PASSOU :) Alteração de venda finalizada foi bloqueada.");
    } 
}
