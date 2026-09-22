package farmaciavenda.exception;

/**
 * Exceção lançada quando as exigências sanitárias para medicamentos controlados (SNGPC)
 * não são satisfeitas (ex: falta de receita médica ou ausência de validação de farmacêutico).
 */
public class MedicamentoControladoException extends RuntimeException {

    public MedicamentoControladoException(String mensagem) {
        super(mensagem);
    }

    public MedicamentoControladoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
