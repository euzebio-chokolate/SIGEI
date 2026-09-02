package ac.gov.br.sigei_backend.exceptions;

public class ArmazenamentoImagemNaoConfiguradoException extends RuntimeException {

    public ArmazenamentoImagemNaoConfiguradoException() {
        super("Upload de imagens indisponível: configure CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY e CLOUDINARY_API_SECRET.");
    }
}
