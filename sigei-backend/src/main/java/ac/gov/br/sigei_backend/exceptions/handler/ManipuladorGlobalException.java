package ac.gov.br.sigei_backend.exceptions.handler;

import ac.gov.br.sigei_backend.exceptions.ArmazenamentoImagemNaoConfiguradoException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;


@RestControllerAdvice
public class ManipuladorGlobalException {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, List<String>> tratarValidation(MethodArgumentNotValidException erro){
        Map<String, List<String>> erros = new HashMap<>();
        erro.getBindingResult().getFieldErrors().forEach(
                e -> {
                    erros.computeIfAbsent(
                            e.getField(),
                            campo -> new ArrayList<>()
                    ).add(e.getDefaultMessage());
                }
        );
        return erros;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> tratarDataIntegrityViolationException(DataIntegrityViolationException erro){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarIllegalArgumentException(IllegalArgumentException erro){
        return ResponseEntity.badRequest().body(erro.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> tratarNoSuchElementException(NoSuchElementException erro){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro.getMessage());
    }

    @ExceptionHandler(ArmazenamentoImagemNaoConfiguradoException.class)
    public ResponseEntity<String> tratarArmazenamentoImagemNaoConfiguradoException(
            ArmazenamentoImagemNaoConfiguradoException erro) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(erro.getMessage());
    }
}
