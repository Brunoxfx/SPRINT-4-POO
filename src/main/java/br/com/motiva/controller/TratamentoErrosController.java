package br.com.motiva.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

import br.com.motiva.dto.ErroResponse;
import br.com.motiva.exception.ConflitoException;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraNegocioException;

@RestControllerAdvice
public class TratamentoErrosController {
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException e,
            HttpServletRequest requisicao) {
        return this.criarResposta(HttpStatus.NOT_FOUND, e.getMessage(), requisicao, List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException e, HttpServletRequest requisicao) {
        return this.criarResposta(HttpStatus.BAD_REQUEST, e.getMessage(), requisicao, List.of());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> tratarConflito(ConflitoException e, HttpServletRequest requisicao) {
        return this.criarResposta(HttpStatus.CONFLICT, e.getMessage(), requisicao, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException e,
            HttpServletRequest requisicao) {
        List<String> detalhes = new ArrayList<String>();
        for (FieldError erro : e.getBindingResult().getFieldErrors()) {
            detalhes.add(erro.getField() + ": " + erro.getDefaultMessage());
        }
        return this.criarResposta(HttpStatus.BAD_REQUEST, "Dados invalidos na requisicao.", requisicao, detalhes);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException e,
            HttpServletRequest requisicao) {
        return this.criarResposta(HttpStatus.BAD_REQUEST, "O corpo da requisicao esta ausente ou mal formatado.",
                requisicao, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarParametroInvalido(MethodArgumentTypeMismatchException e,
            HttpServletRequest requisicao) {
        return this.criarResposta(HttpStatus.BAD_REQUEST, "Valor invalido para o parametro " + e.getName() + ".",
                requisicao, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> tratarParametroAusente(MissingServletRequestParameterException e,
            HttpServletRequest requisicao) {
        return this.criarResposta(HttpStatus.BAD_REQUEST,
                "O parametro " + e.getParameterName() + " deve ser informado.", requisicao, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarIntegridade(DataIntegrityViolationException e,
            HttpServletRequest requisicao) {
        return this.criarResposta(HttpStatus.CONFLICT,
                "A operacao viola uma restricao do banco de dados (registro duplicado ou em uso).", requisicao,
                List.of());
    }

    private ResponseEntity<ErroResponse> criarResposta(HttpStatus status, String mensagem,
            HttpServletRequest requisicao, List<String> detalhes) {
        ErroResponse corpo = new ErroResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensagem, requisicao.getRequestURI(), detalhes);
        return ResponseEntity.status(status).body(corpo);
    }
}
