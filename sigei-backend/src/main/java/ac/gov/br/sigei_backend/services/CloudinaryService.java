package ac.gov.br.sigei_backend.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public Map uploadImagem(MultipartFile arquivo) throws IOException {

        return cloudinary.uploader().upload(
                arquivo.getBytes(),
                ObjectUtils.asMap(
                        "folder", "sigei/infraestrutura",
                        "resource_type", "image"
                )
        );
    }
}