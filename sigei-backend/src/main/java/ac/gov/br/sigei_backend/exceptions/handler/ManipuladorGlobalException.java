package ac.gov.br.sigei_backend.exceptions.handler;

import ac.gov.br.sigei_backend.exceptions.ArmazenamentoImagemNaoConfiguradoException;
import ac.gov.br.sigei_backend.exceptions.CredenciaisInvalidasException;
import ac.gov.br.sigei_backend.exceptions.RegraNegocioException;
import ac.gov.br.sigei_backend.exceptions.TokenInvalidoException;
import ac.gov.br.sigei_backend.exceptions.UsuarioDesativadoException;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Todas as respostas de erro saem como ProblemDetail (ver {@link Problemas}). A
 * superclasse trata as exceções do próprio Spring MVC (JSON mal formado, parâmetro
 * ausente ou de tipo errado, método não aceito, rota inexistente, upload grande);
 * {@link #handleExceptionInternal} troca os textos em inglês por mensagens em português.
 * O que chega a /error (segurança, falhas fora dos controllers) é tratado por
 * {@code ErroController}, no mesmo formato.
 */
@RestControllerAdvice
public class ManipuladorGlobalException extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ManipuladorGlobalException.class);

    static final String MENSAGEM_CONFLITO =
            "Não foi possível concluir a operação porque ela conflita com outros registros. "
                    + "Atualize a página e tente novamente.";

    @Value("${spring.servlet.multipart.max-file-size:20MB}")
    private DataSize tamanhoMaximoUpload;

    /** 400 com {@code errors}: campo → mensagens; o {@code detail} resume para quem não olha os campos. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException erro, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, List<String>> erros = new LinkedHashMap<>();
        erro.getBindingResult().getFieldErrors().forEach(e ->
                erros.computeIfAbsent(e.getField(), campo -> new ArrayList<>()).add(e.getDefaultMessage()));

        String detalhe = erros.size() == 1
                ? erros.values().iterator().next().getFirst()
                : "Alguns campos precisam de correção.";
        ProblemDetail problema = Problemas.criar(HttpStatus.BAD_REQUEST, detalhe);
        problema.setProperty(Problemas.ERROS_POR_CAMPO, erros);
        return handleExceptionInternal(erro, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception erro, Object corpo, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Exceções que já trazem o próprio ProblemDetail (rota inexistente, método e parâmetro) chegam sem corpo
        if (corpo == null && erro instanceof ErrorResponse resposta) {
            corpo = resposta.getBody();
        }
        if (corpo instanceof ProblemDetail problema) {
            problema.setTitle(Problemas.titulo(status));
            CampoInvalido campo = erro instanceof HttpMessageNotReadableException leitura ? campoInvalido(leitura) : null;
            if (campo != null) {
                problema.setDetail(campo.mensagem());
                problema.setProperty(Problemas.ERROS_POR_CAMPO, Map.of(campo.nome(), List.of(campo.mensagem())));
            } else if (!(erro instanceof MethodArgumentNotValidException)) {
                problema.setDetail(detalheEmPortugues(erro, status));
            }
        }
        return super.handleExceptionInternal(erro, corpo, headers, status, request);
    }

    private record CampoInvalido(String nome, String mensagem) {
    }

    /**
     * Campo do JSON com valor que não pôde ser convertido (texto num número, opção
     * fora do enum, nulo num booleano), para responder como a validação: com o campo
     * em {@code errors}. Sem um campo identificável (JSON mal formado), devolve null.
     */
    private static CampoInvalido campoInvalido(HttpMessageNotReadableException erro) {
        for (Throwable causa = erro.getCause(); causa != null; causa = causa.getCause()) {
            if (causa instanceof MismatchedInputException conversao) {
                String nome = caminho(conversao);
                if (nome.isEmpty()) {
                    return null;
                }
                String mensagem = "Valor inválido para o campo " + nome + ".";
                Class<?> tipo = conversao.getTargetType();
                if (tipo != null && tipo.isEnum()) {
                    mensagem += " Use um destes: " + Arrays.stream(tipo.getEnumConstants())
                            .map(opcao -> ((Enum<?>) opcao).name())
                            .collect(Collectors.joining(", ")) + ".";
                }
                return new CampoInvalido(nome, mensagem);
            }
        }
        return null;
    }

    /** Caminho do campo no JSON, como nos erros de validação: {@code infraestrutura.numSalas}, {@code itens[0].nome}. */
    private static String caminho(MismatchedInputException erro) {
        StringBuilder caminho = new StringBuilder();
        for (JacksonException.Reference referencia : erro.getPath()) {
            if (referencia.getPropertyName() != null) {
                caminho.append(caminho.isEmpty() ? "" : ".").append(referencia.getPropertyName());
            } else if (referencia.getIndex() >= 0) {
                caminho.append('[').append(referencia.getIndex()).append(']');
            }
        }
        return caminho.toString();
    }

    private String detalheEmPortugues(Exception erro, HttpStatusCode status) {
        return switch (erro) {
            case HttpMessageNotReadableException e -> "O corpo da requisição é inválido ou está mal formatado.";
            case MaxUploadSizeExceededException e -> "O arquivo enviado excede o tamanho máximo permitido ("
                    + tamanhoMaximoUpload.toMegabytes() + " MB).";
            case MissingServletRequestParameterException e -> "O parâmetro " + e.getParameterName() + " é obrigatório.";
            case MissingServletRequestPartException e -> "A parte " + e.getRequestPartName() + " é obrigatória.";
            case TypeMismatchException e -> "Valor inválido para " + e.getPropertyName() + ".";
            case HttpRequestMethodNotSupportedException e -> "O método " + e.getMethod() + " não é aceito nesta rota.";
            default -> Problemas.detalhePadrao(status);
        };
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail tratarRegraNegocioException(RegraNegocioException erro) {
        return Problemas.criar(HttpStatus.CONFLICT, erro.getMessage());
    }

    /**
     * Violação de restrição que os services não anteciparam (por exemplo, duas
     * gravações simultâneas). A mensagem do banco traz SQL e nomes de constraints,
     * então não vai para a resposta; o log registra só o nome da constraint, porque
     * os valores da violação podem incluir dados sensíveis.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratarDataIntegrityViolationException(DataIntegrityViolationException erro) {
        String constraint = erro.getCause() instanceof ConstraintViolationException violacao
                ? violacao.getConstraintName()
                : null;
        log.warn("Violação de integridade não tratada pelo service (constraint: {})", constraint);
        return Problemas.criar(HttpStatus.CONFLICT, MENSAGEM_CONFLITO);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail tratarIllegalArgumentException(IllegalArgumentException erro) {
        return Problemas.criar(HttpStatus.BAD_REQUEST, erro.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail tratarNoSuchElementException(NoSuchElementException erro) {
        return Problemas.criar(HttpStatus.NOT_FOUND, erro.getMessage());
    }

    @ExceptionHandler(ArmazenamentoImagemNaoConfiguradoException.class)
    public ProblemDetail tratarArmazenamentoImagemNaoConfiguradoException(
            ArmazenamentoImagemNaoConfiguradoException erro) {
        return Problemas.criar(HttpStatus.SERVICE_UNAVAILABLE, erro.getMessage());
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ProblemDetail tratarTokenInvalidoException(TokenInvalidoException erro) {
        return Problemas.criar(HttpStatus.UNAUTHORIZED, erro.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ProblemDetail tratarCredenciaisInvalidasException(CredenciaisInvalidasException erro) {
        return Problemas.criar(HttpStatus.UNAUTHORIZED, erro.getMessage());
    }

    @ExceptionHandler(UsuarioDesativadoException.class)
    public ProblemDetail tratarUsuarioDesativadoException(UsuarioDesativadoException erro) {
        return Problemas.criar(HttpStatus.FORBIDDEN, erro.getMessage());
    }

    /** Falha inesperada: a mensagem da exceção fica só no log, nunca na resposta. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarExcecaoInesperada(Exception erro) {
        log.error("Erro não tratado na requisição", erro);
        return Problemas.criar(HttpStatus.INTERNAL_SERVER_ERROR, Problemas.detalhePadrao(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
