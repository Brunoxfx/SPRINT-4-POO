package br.com.motiva.exception;

public class ConflitoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
