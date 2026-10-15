package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.EquipeEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.EquipeEscolaResponseDTO;
import ac.gov.br.sigei_backend.exceptions.RegraNegocioException;
import ac.gov.br.sigei_backend.mappers.EquipeEscolaMapper;
import ac.gov.br.sigei_backend.models.EquipeEscola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.EquipeEscolaRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class EquipeEscolaService {

    private static final String EQUIPE_DUPLICADA =
            "Este registro anual já tem uma equipe cadastrada; edite a existente.";

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Autowired
    EquipeEscolaRepository equipeEscolaRepository;

    @Autowired
    EquipeEscolaMapper equipeEscolaMapper;

    public EquipeEscolaResponseDTO salvar(EquipeEscolaRequestDTO equipeDTO) {

        RegistroAnual registroAnual = registroAnualRepository
                .findById(equipeDTO.registroAnualId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        if (equipeEscolaRepository.existsByRegistroAnual_Id(registroAnual.getId())) {
            throw new RegraNegocioException(EQUIPE_DUPLICADA);
        }

        EquipeEscola equipeEscola = equipeEscolaMapper.converter(equipeDTO);

        equipeEscola.setRegistroAnual(registroAnual);

        EquipeEscola equipeEscolaSalva = equipeEscolaRepository.save(equipeEscola);

        return equipeEscolaMapper.converter(equipeEscolaSalva);
    }

    public EquipeEscolaResponseDTO editar(EquipeEscolaRequestDTO equipeDTO) {

        EquipeEscola equipeEscola = equipeEscolaRepository.findById(equipeDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Equipe escolar não encontrada"));

        RegistroAnual registroAnual = registroAnualRepository
                .findById(equipeDTO.registroAnualId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        if (equipeEscolaRepository.existsByRegistroAnual_IdAndIdNot(registroAnual.getId(), equipeEscola.getId())) {
            throw new RegraNegocioException(EQUIPE_DUPLICADA);
        }

        equipeEscolaMapper.atualizar(equipeDTO, equipeEscola);

        equipeEscola.setRegistroAnual(registroAnual);

        EquipeEscola equipeEscolaSalva = equipeEscolaRepository.save(equipeEscola);

        return equipeEscolaMapper.converter(equipeEscolaSalva);
    }

    public void excluir(Long id) {

        equipeEscolaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Equipe escolar não encontrada"));

        equipeEscolaRepository.deleteById(id);
    }
}