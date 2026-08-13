package com.treino.repository;

import com.treino.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    Optional<Reserva> findByClienteUserIdAndClaseClaseIdAndEstadoReserva(Long clienteId, Long claseId, Reserva.EstadoReserva estadoReserva);
    List<Reserva> findByClienteUserIdOrderByFechaReservaDesc(Long clienteId);
    List<Reserva> findByClaseClaseId(Long claseId);
}
