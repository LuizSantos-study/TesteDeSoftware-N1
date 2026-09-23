package farmaciavenda.exception;

/**
 * Exceção lançada quando uma operação de desconto é inválida (ex: desconto negativo
 * ou desconto que resulte em valor total negativo).
 */
public class DescontoInvalidoException extends RuntimeException {

    public DescontoInvalidoException(String mensagem) {
        super(mensagem);
    }

    public DescontoInvalidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
