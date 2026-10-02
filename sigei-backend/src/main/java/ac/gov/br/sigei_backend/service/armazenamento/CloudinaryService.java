package ac.gov.br.sigei_backend.service.armazenamento;

import ac.gov.br.sigei_backend.exceptions.ArmazenamentoImagemNaoConfiguradoException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    public Map uploadImagem(MultipartFile arquivo) throws IOException {

        if (credencialAusente(cloudName)
                || credencialAusente(apiKey)
                || credencialAusente(apiSecret)) {
            throw new ArmazenamentoImagemNaoConfiguradoException();
        }

        return cloudinary.uploader().upload(
                arquivo.getBytes(),
                ObjectUtils.asMap(
                        "folder", "sigei/infraestrutura",
                        "resource_type", "image"
                )
        );
    }

    public void excluirImagem(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    private boolean credencialAusente(String valor) {
        return valor == null
                || valor.isBlank()
                || "disabled".equalsIgnoreCase(valor.trim());
    }
}
