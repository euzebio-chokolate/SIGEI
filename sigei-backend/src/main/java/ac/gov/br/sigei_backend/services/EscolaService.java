package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.utils.EscolaConversor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EscolaService {
    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    EscolaConversor escolaConversor;

    public EscolaResponseDTO salvar(EscolaRequestDTO escolaRequestDTO) throws DataIntegrityViolationException{
        Municipio municipio = municipioRepository.findById(escolaRequestDTO.municipioId());
    }

    public EscolaResponseDTO buscarPorId(Long id){
        Optional<Escola> optionalEscola = escolaRepository.findById(id);
        if (optionalEscola.isEmpty()){
            throw new IllegalArgumentException("Escola não existe");
        }
        Escola escola = optionalEscola.get();
        EscolaResponseDTO escolaResponseDTO = escolaConversor.converterEscola(escola);
        return escolaResponseDTO;
    }

    public EscolaResponseDTO buscarPorCodigo(String codigo){
        Optional<Escola> optionalEscola = escolaRepository.findEscolaByCodigo(codigo);
        if (optionalEscola.isEmpty()){
            throw new IllegalArgumentException("Escola não existe");
        }
        Escola escola = optionalEscola.get();
        EscolaResponseDTO escolaResponseDTO = escolaConversor.converterEscola(escola);
        return escolaResponseDTO;
    }


}
