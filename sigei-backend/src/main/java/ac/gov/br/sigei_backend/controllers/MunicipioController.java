package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.dto.response.MunicipioEstatisticaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.service.MunicipioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/municipio")
public class MunicipioController {

    @Autowired
    MunicipioService municipioService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody MunicipioRequestDTO municipioDTO){
        MunicipioResponseDTO municipioResponseDTO = municipioService.salvar(municipioDTO);
        return new ResponseEntity<MunicipioResponseDTO>(municipioResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<MunicipioResponseDTO>> buscar(){
        List<MunicipioResponseDTO> municipios = municipioService.buscarTodos();
        return new ResponseEntity<List<MunicipioResponseDTO>>(municipios, HttpStatus.OK);
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<List<MunicipioEstatisticaResponseDTO>> buscarEstatisticas() {

        List<MunicipioEstatisticaResponseDTO> municipios =
                municipioService.buscarEstatisticas();

        return new ResponseEntity<>(
                municipios,
                HttpStatus.OK
        );
    }

    @PutMapping("/editar")
    public ResponseEntity<MunicipioResponseDTO> editar(@RequestBody MunicipioRequestDTO municipioDTO){
        MunicipioResponseDTO municipioResponseDTO = municipioService.editar(municipioDTO);
        return new ResponseEntity<MunicipioResponseDTO>(municipioResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id){
        municipioService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
