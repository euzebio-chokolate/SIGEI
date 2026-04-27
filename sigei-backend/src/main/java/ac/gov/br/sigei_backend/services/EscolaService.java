package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.EscolaDTO;
import ac.gov.br.sigei_backend.DTOs.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EscolaService {
    @Autowired
    private EscolaRepository escolaRepository;

    public EscolaResponseDTO salvar(EscolaRequestDTO escolaDTO) throws DataIntegrityViolationException {
        Escola escola = escolaDTO.ConverterParaEntidade();
        escolaRepository.save(escola);
        return new EscolaResponseDTO(
                escola.getId(),
                escola.getNome(),
                escola.getLatitude(),
                escola.getLongitude()

        );
    }

    public List<Escola> buscarTodas() {
        return escolaRepository.findAll();
    }

    public Escola buscarPorId(Long id) {
        Optional<Escola> optionalEscola = escolaRepository.findById(id);

        if(optionalEscola.isEmpty()){
            throw new IllegalArgumentException("Escola não encontrada!");
        }
        return optionalEscola.get();
    }



    public void excluir(Long id) {
        Optional<Escola> optionalEscola = escolaRepository.findById(id);
        if(optionalEscola.isEmpty()){
            throw new IllegalArgumentException("Escola inexistente!");
        } else{
            escolaRepository.deleteById(id);
        }
    }

    public void editar(EscolaDTO escolaDTO) {
        Escola escola = escolaDTO.ConverterParaEntidade();
        escolaRepository.save(escola);
    }
}
