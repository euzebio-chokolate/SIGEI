package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.ImportacaoResultadoDTO;
import ac.gov.br.sigei_backend.DTOs.response.ImportacaoMatriculasResultadoDTO;
import ac.gov.br.sigei_backend.services.ImportacaoExcelService;
import ac.gov.br.sigei_backend.services.ImportacaoMatriculasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/importacoes")
public class ImportacaoExcelController {

    @Autowired
    ImportacaoExcelService importacaoExcelService;

    @Autowired
    ImportacaoMatriculasService importacaoMatriculasService;

    @PostMapping(value = "/infraestrutura", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportacaoResultadoDTO> importarInfraestrutura(@RequestParam("arquivo") MultipartFile arquivo) {
        ImportacaoResultadoDTO resultado = importacaoExcelService.importarInfraestrutura(arquivo);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping(value = "/matriculas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportacaoMatriculasResultadoDTO> importarMatriculas(@RequestParam("arquivo") MultipartFile arquivo) {
        ImportacaoMatriculasResultadoDTO resultado = importacaoMatriculasService.importar(arquivo);
        return ResponseEntity.ok(resultado);
    }
}