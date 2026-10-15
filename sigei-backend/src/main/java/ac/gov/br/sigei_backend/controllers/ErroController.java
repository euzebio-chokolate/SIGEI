package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.exceptions.handler.Problemas;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Substitui o BasicErrorController do Spring Boot, para que os erros encaminhados a
 * /error tenham o mesmo formato ProblemDetail do {@code ManipuladorGlobalException}.
 * Chegam aqui os erros gerados fora dos controllers: 401 sem autenticação, 403 sem
 * permissão ou com token CSRF inválido e exceções nos filtros. A mensagem é sempre
 * a padrão do status; detalhes da exceção não vão para a resposta.
 */
@Hidden
@RestController
public class ErroController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ProblemDetail> erro(HttpServletRequest request) {
        HttpStatus status = statusDoErro(request);

        String detalhe = status == HttpStatus.FORBIDDEN
                && request.getAttribute(WebAttributes.ACCESS_DENIED_403) instanceof CsrfException
                ? "Token de segurança (CSRF) ausente ou inválido. Recarregue a página e tente novamente."
                : Problemas.detalhePadrao(status);

        ProblemDetail problema = Problemas.criar(status, detalhe);
        if (request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) instanceof String rota) {
            problema.setInstance(URI.create(rota));
        }
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problema);
    }

    private static HttpStatus statusDoErro(HttpServletRequest request) {
        if (request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) instanceof Integer codigo) {
            HttpStatus status = HttpStatus.resolve(codigo);
            if (status != null && status.isError()) {
                return status;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
