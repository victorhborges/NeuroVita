package com.neurovita.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.neurovita.dto.DocumentoClinicoRequest;
import com.neurovita.dto.DocumentoClinicoResponse;
import com.neurovita.service.DocumentoClinicoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/documentos-clinicos")
public class DocumentoClinicoController {
    private final DocumentoClinicoService service;
    public DocumentoClinicoController(DocumentoClinicoService service) { this.service = service; }

    @GetMapping("/paciente/{pacienteId}")
    public List<DocumentoClinicoResponse> porPaciente(@PathVariable String pacienteId, Authentication auth) {
        return service.porPaciente(pacienteId, auth);
    }

    @PostMapping
    public ResponseEntity<DocumentoClinicoResponse> criar(@Valid @RequestBody DocumentoClinicoRequest request, Authentication auth) {
        return ResponseEntity.status(201).body(service.salvar(null, request, auth));
    }

    @PutMapping("/{id}")
    public DocumentoClinicoResponse atualizar(@PathVariable String id, @Valid @RequestBody DocumentoClinicoRequest request, Authentication auth) {
        return service.salvar(id, request, auth);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id, Authentication auth) {
        service.excluir(id, auth);
        return ResponseEntity.noContent().build();
    }
}
