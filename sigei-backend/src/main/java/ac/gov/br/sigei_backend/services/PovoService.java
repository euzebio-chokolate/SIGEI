package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.PovoRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.PovoResponseDTO;
import ac.gov.br.sigei_backend.mappers.PovoMapper;
import ac.gov.br.sigei_backend.models.Povo;
import ac.gov.br.sigei_backend.repository.PovoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class PovoService {
    @Autowired
    PovoMapper povoMapper;

    @Autowired
    PovoRepository povoRepository;

    public PovoResponseDTO salvar(PovoRequestDTO povoDTO) throws DataIntegrityViolationException{
        Povo povo = povoMapper.converter(povoDTO);
        povoRepository.save(povo);

        return povoMapper.converter(povo);
    }
}
