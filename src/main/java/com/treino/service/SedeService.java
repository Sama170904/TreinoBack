package com.treino.service;

import com.treino.dto.Create.SedeCreateDTO;
import com.treino.dto.Update.SedeUpdateDTO;
import com.treino.dto.Response.SedeResponseDTO;
import com.treino.entity.Sede;
import com.treino.middlewares.ResourceNotFoundException;
import com.treino.repository.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SedeService {

    private final SedeRepository sedeRepository;

    @Transactional
    public SedeResponseDTO crear(SedeCreateDTO dto) {
        Sede sede = Sede.builder()
                .nombre(dto.getNombre())
                .direccion(dto.getDireccion())
                .capacidadMaxima(dto.getCapacidadMaxima())
                .build();
        sede = sedeRepository.save(sede);
        return mapToResponse(sede);
    }

    public List<SedeResponseDTO> listarTodas() {
        return sedeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SedeResponseDTO obtenerPorId(Long id) {
        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
        return mapToResponse(sede);
    }

    @Transactional
    public SedeResponseDTO actualizar(SedeUpdateDTO dto) {
        Sede sede = sedeRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
        
        sede.setNombre(dto.getNombre());
        sede.setDireccion(dto.getDireccion());
        sede.setCapacidadMaxima(dto.getCapacidadMaxima());
        sede = sedeRepository.save(sede);
        return mapToResponse(sede);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!sedeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sede no encontrada");
        }
        sedeRepository.deleteById(id); // Esto lanza el @SQLDelete
    }

    private SedeResponseDTO mapToResponse(Sede s) {
        return SedeResponseDTO.builder()
                .sedeId(s.getSedeId())
                .nombre(s.getNombre())
                .direccion(s.getDireccion())
                .capacidadMaxima(s.getCapacidadMaxima())
                .build();
    }
}
