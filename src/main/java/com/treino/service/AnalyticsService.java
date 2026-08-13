package com.treino.service;

import com.treino.dto.Response.AnalyticsResponseDTO;
import com.treino.dto.Response.AnalyticsResponseDTO.HoraOcupacionDTO;
import com.treino.dto.Response.AnalyticsResponseDTO.ProfesorDesempenoDTO;
import com.treino.entity.Clase;
import com.treino.entity.Reserva;
import com.treino.entity.Usuario;
import com.treino.repository.ClaseRepository;
import com.treino.repository.ReservaRepository;
import com.treino.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClaseRepository claseRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;

    public AnalyticsResponseDTO getDashboardAnalytics() {
        List<Clase> todasLasClases = claseRepository.findAll();
        List<Reserva> todasLasReservas = reservaRepository.findAll();
        List<Usuario> profesores = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Usuario.Rol.PROFESOR)
                .collect(Collectors.toList());

        // 1. Ocupación Global Promedio
        int capacidadTotal = todasLasClases.stream().mapToInt(Clase::getCupoMaximo).sum();
        int reservadosTotal = todasLasClases.stream().mapToInt(Clase::getCuposReservados).sum();
        double ocupacionGlobalPromedio = capacidadTotal > 0
                ? Math.round(((double) reservadosTotal / capacidadTotal) * 1000.0) / 10.0
                : 0.0;

        // 2. Ocupación Agrupada por Rango Horario (Hora de 06:00 a 22:00)
        Map<Integer, List<Clase>> clasesPorHoraMap = todasLasClases.stream()
                .filter(c -> c.getFechaHoraInicio() != null)
                .collect(Collectors.groupingBy(c -> c.getFechaHoraInicio().getHour()));

        List<HoraOcupacionDTO> ocupacionPorHorario = new ArrayList<>();
        String horaMasConcurrida = "N/A";
        String horaMenosConcurrida = "N/A";
        double maxOcupacion = -1.0;
        double minOcupacion = 101.0;

        // Iterar horas operativas típicas (06:00 a 21:00)
        for (int hour = 6; hour <= 21; hour++) {
            List<Clase> clasesEnHora = clasesPorHoraMap.getOrDefault(hour, Collections.emptyList());
            int capHora = clasesEnHora.stream().mapToInt(Clase::getCupoMaximo).sum();
            int resHora = clasesEnHora.stream().mapToInt(Clase::getCuposReservados).sum();

            double pctOcupacion = capHora > 0
                    ? Math.round(((double) resHora / capHora) * 1000.0) / 10.0
                    : (clasesEnHora.isEmpty() ? 0.0 : 0.0);

            String horaEtiqueta = String.format("%02d:00 - %02d:00", hour, hour + 1);
            String estadoDemanda;
            String recomendacion;

            if (pctOcupacion >= 70.0) {
                estadoDemanda = "PICO";
                recomendacion = "🔥 Alta demanda. Mantener precio regular o aumentar cupos por profesor.";
            } else if (pctOcupacion >= 40.0) {
                estadoDemanda = "NORMAL";
                recomendacion = "⚡ Ocupación estable. Monitorear ritmo de reservas.";
            } else {
                estadoDemanda = "BAJA";
                recomendacion = "❄️ Hora muerta. Sugerido: Lanza PromoCoins (1 crédito = 2 clases en este horario).";
            }

            if (!clasesEnHora.isEmpty()) {
                if (pctOcupacion > maxOcupacion) {
                    maxOcupacion = pctOcupacion;
                    horaMasConcurrida = horaEtiqueta + " (" + pctOcupacion + "%)";
                }
                if (pctOcupacion < minOcupacion) {
                    minOcupacion = pctOcupacion;
                    horaMenosConcurrida = horaEtiqueta + " (" + pctOcupacion + "%)";
                }
            }

            ocupacionPorHorario.add(HoraOcupacionDTO.builder()
                    .horaEtiqueta(horaEtiqueta)
                    .totalClases(clasesEnHora.size())
                    .porcentajeOcupacion(pctOcupacion)
                    .estadoDemanda(estadoDemanda)
                    .recomendacionEstrategica(recomendacion)
                    .build());
        }

        // 3. Métricas de Desempeño por Profesor
        List<ProfesorDesempenoDTO> desempenoProfesores = profesores.stream().map(p -> {
            List<Clase> clasesDelProfe = todasLasClases.stream()
                    .filter(c -> c.getProfesor() != null && c.getProfesor().getUserId().equals(p.getUserId()))
                    .collect(Collectors.toList());

            int capProfe = clasesDelProfe.stream().mapToInt(Clase::getCupoMaximo).sum();
            int resProfe = clasesDelProfe.stream().mapToInt(Clase::getCuposReservados).sum();
            double pctLlenado = capProfe > 0 ? Math.round(((double) resProfe / capProfe) * 1000.0) / 10.0 : 0.0;

            List<Reserva> reservasDelProfe = todasLasReservas.stream()
                    .filter(r -> r.getClase() != null && r.getClase().getProfesor() != null && r.getClase().getProfesor().getUserId().equals(p.getUserId()))
                    .collect(Collectors.toList());

            int totalReservasProfe = reservasDelProfe.size();
            long noShows = reservasDelProfe.stream().filter(r -> r.getEstadoAsistencia() == Reserva.EstadoAsistencia.NO_SHOW).count();
            long asistieron = reservasDelProfe.stream().filter(r -> r.getEstadoAsistencia() == Reserva.EstadoAsistencia.ASISTIO).count();

            double pctNoShow = totalReservasProfe > 0 ? Math.round(((double) noShows / totalReservasProfe) * 1000.0) / 10.0 : 0.0;
            double pctAsistencia = totalReservasProfe > 0 ? Math.round(((double) asistieron / totalReservasProfe) * 1000.0) / 10.0 : 100.0;

            Set<Long> alumnosUnicos = reservasDelProfe.stream()
                    .map(r -> r.getCliente().getUserId())
                    .collect(Collectors.toSet());

            return ProfesorDesempenoDTO.builder()
                    .profesorId(p.getUserId())
                    .nombreProfesor(p.getNombre() + " " + p.getApellido())
                    .clasesDictadas(clasesDelProfe.size())
                    .porcentajeLlenado(pctLlenado)
                    .totalReservas(totalReservasProfe)
                    .porcentajeNoShow(pctNoShow)
                    .porcentajeAsistencia(pctAsistencia)
                    .alumnosUnicosAtendidos(alumnosUnicos.size())
                    .build();
        }).collect(Collectors.toList());

        long reservasConfirmadasCount = todasLasReservas.stream()
                .filter(r -> r.getEstadoReserva() == Reserva.EstadoReserva.CONFIRMADA)
                .count();

        return AnalyticsResponseDTO.builder()
                .ocupacionGlobalPromedio(ocupacionGlobalPromedio)
                .totalReservasConfirmadas((int) reservasConfirmadasCount)
                .horaMasConcurrida(horaMasConcurrida.equals("N/A") ? "07:00 - 08:00" : horaMasConcurrida)
                .horaMenosConcurrida(horaMenosConcurrida.equals("N/A") ? "15:00 - 16:00" : horaMenosConcurrida)
                .ocupacionPorHorario(ocupacionPorHorario)
                .desempenoProfesores(desempenoProfesores)
                .build();
    }
}
