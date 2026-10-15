package ac.gov.br.sigei_backend.exceptions.handler;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManipuladorGlobalExceptionTest {

    private final ManipuladorGlobalException manipulador = new ManipuladorGlobalException();

    @Test
    void violacaoNaoTratadaNaoExpoeAMensagemDoBanco() {
        String mensagemBanco = "could not execute statement [ERROR: update or delete on table \"povo\" violates "
                + "foreign key constraint \"fkf2o029wwl54d2wb8hgbukah7w\"]; SQL [delete from povo where id=?]";
        DataIntegrityViolationException erro = new DataIntegrityViolationException(mensagemBanco,
                new ConstraintViolationException(mensagemBanco, new SQLException(mensagemBanco),
                        "fkf2o029wwl54d2wb8hgbukah7w"));

        ProblemDetail resposta = manipulador.tratarDataIntegrityViolationException(erro);

        assertEquals(409, resposta.getStatus());
        assertEquals("Conflito", resposta.getTitle());
        assertEquals(ManipuladorGlobalException.MENSAGEM_CONFLITO, resposta.getDetail());
    }

    @Test
    void falhaInesperadaNaoExpoeAMensagemDaExcecao() {
        ProblemDetail resposta = manipulador.tratarExcecaoInesperada(new IllegalStateException("detalhe interno"));

        assertEquals(500, resposta.getStatus());
        assertEquals("Ocorreu um erro inesperado no servidor.", resposta.getDetail());
    }
}
