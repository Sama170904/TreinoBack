package com.treino.service;

import com.treino.dto.Create.ClaseCreateDTO;
import com.treino.dto.Update.ClaseUpdateDTO;
import com.treino.dto.Response.ClaseResponseDTO;
import com.treino.entity.Clase;
import com.treino.entity.Sede;
import com.treino.entity.Usuario;
import com.treino.middlewares.BusinessException;
import com.treino.middlewares.ResourceNotFoundException;
import com.treino.repository.ClaseRepository;
import com.treino.repository.SedeRepository;
import com.treino.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaseService {

    private final ClaseRepository claseRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ClaseResponseDTO crear(ClaseCreateDTO dto, Long profesorId) {
        Sede sede = sedeRepository.findById(dto.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
        Usuario profesor = usuarioRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("Profesor no encontrado"));

        Clase clase = Clase.builder()
                .profesor(profesor)
                .sede(sede)
                .disciplina(dto.getDisciplina())
                .descripcion(dto.getDescripcion())
                .fechaHoraInicio(dto.getFechaHoraInicio())
                .fechaHoraFin(dto.getFechaHoraFin())
                .cupoMaximo(dto.getCupoMaximo())
                .build();
        
        clase = claseRepository.save(clase);
        return mapToResponse(clase);
    }

    public List<ClaseResponseDTO> listar(Long sedeId, String disciplina, LocalDate fecha) {
        return claseRepository.findAllByOrderByFechaHoraInicioAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClaseResponseDTO actualizar(ClaseUpdateDTO dto, Long profesorId, String userRole) {
        Clase clase = claseRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada"));
        
        if (!userRole.equals("ROLE_ADMINISTRADOR") && !clase.getProfesor().getUserId().equals(profesorId)) {
            throw new BusinessException("No tienes permisos para editar esta clase");
        }

        Sede sede = sedeRepository.findById(dto.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        clase.setSede(sede);
        clase.setDisciplina(dto.getDisciplina());
        clase.setDescripcion(dto.getDescripcion());
        clase.setFechaHoraInicio(dto.getFechaHoraInicio());
        clase.setFechaHoraFin(dto.getFechaHoraFin());
        clase.setCupoMaximo(dto.getCupoMaximo());
        
        clase = claseRepository.save(clase);
        return mapToResponse(clase);
    }

    @Transactional
    public void eliminar(Long claseId, Long profesorId, String userRole) {
        Clase clase = claseRepository.findById(claseId)
                .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada"));
                
        if (!userRole.equals("ROLE_ADMINISTRADOR") && !clase.getProfesor().getUserId().equals(profesorId)) {
            throw new BusinessException("No tienes permisos para eliminar esta clase");
        }
        
        claseRepository.deleteById(claseId);
    }

    private ClaseResponseDTO mapToResponse(Clase c) {
        String profesorNombre = null;
        if (c.getProfesor() != null) {
            profesorNombre = c.getProfesor().getNombre() + " " + c.getProfesor().getApellido();
        }
        String sedeNombre = null;
        if (c.getSede() != null) {
            sedeNombre = c.getSede().getNombre();
        }

        return ClaseResponseDTO.builder()
                .claseId(c.getClaseId())
                .profesorId(c.getProfesor() != null ? c.getProfesor().getUserId() : null)
                .profesorNombre(profesorNombre)
                .sedeId(c.getSede() != null ? c.getSede().getSedeId() : null)
                .sedeNombre(sedeNombre)
                .disciplina(c.getDisciplina())
                .descripcion(c.getDescripcion())
                .fechaHoraInicio(c.getFechaHoraInicio())
                .fechaHoraFin(c.getFechaHoraFin())
                .cupoMaximo(c.getCupoMaximo())
                .cuposReservados(c.getCuposReservados())
                .estado(c.getEstado() != null ? c.getEstado().name() : "ACTIVO")
                .build();
    }
}
