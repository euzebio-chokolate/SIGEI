package ac.gov.br.sigei_backend.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<String> tratarDataIntegrityViolationException(DataIntegrityViolationException erro){
        return ResponseEntity.badRequest().body(erro.getMessage());
    }

}
