package farmaciavenda.exception;

/**
 * Exceção lançada ao tentar modificar itens, valores ou status de uma venda
 * que já foi concluída/finalizada.
 */
public class VendaFinalizadaException extends RuntimeException {

    public VendaFinalizadaException(String mensagem) {
        super(mensagem);
    }

    public VendaFinalizadaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
