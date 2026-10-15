package ac.gov.br.sigei_backend.exceptions.handler;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

/**
 * Monta as respostas de erro no formato único da API, o ProblemDetail (RFC 9457,
 * antiga 7807): {@code status}, {@code title} (o tipo do erro), {@code detail} (a
 * mensagem para o usuário), {@code instance} (a rota) e, na validação,
 * {@code errors} com as mensagens por campo.
 */
public final class Problemas {

    /** Propriedade com o mapa campo → mensagens dos erros de validação. */
    public static final String ERROS_POR_CAMPO = "errors";

    private Problemas() {
    }

    public static ProblemDetail criar(HttpStatusCode status, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo(status));
        return problema;
    }

    /** Título curto do tipo de erro, igual para todas as respostas com o mesmo status. */
    public static String titulo(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Requisição inválida";
            case 401 -> "Não autenticado";
            case 403 -> "Acesso negado";
            case 404 -> "Não encontrado";
            case 405 -> "Método não permitido";
            case 409 -> "Conflito";
            case 413 -> "Arquivo muito grande";
            case 415 -> "Formato não aceito";
            case 503 -> "Serviço indisponível";
            default -> status.is5xxServerError() ? "Erro interno" : "Erro na requisição";
        };
    }

    /** Mensagem usada quando não há uma mais específica (erros encaminhados a /error e do próprio Spring). */
    public static String detalhePadrao(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "A requisição é inválida.";
            case 401 -> "Autenticação necessária. Entre novamente.";
            case 403 -> "Você não tem permissão para realizar esta ação.";
            case 404 -> "O recurso solicitado não foi encontrado.";
            case 405 -> "Método não aceito nesta rota.";
            case 413 -> "O arquivo enviado excede o tamanho máximo permitido.";
            case 415 -> "Formato de conteúdo não aceito nesta rota.";
            case 503 -> "O serviço está temporariamente indisponível.";
            default -> status.is5xxServerError()
                    ? "Ocorreu um erro inesperado no servidor."
                    : "Não foi possível processar a requisição.";
        };
    }
}
