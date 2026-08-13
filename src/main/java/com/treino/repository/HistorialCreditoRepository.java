package com.treino.repository;

import com.treino.entity.HistorialCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialCreditoRepository extends JpaRepository<HistorialCredito, Long> {
    List<HistorialCredito> findByClienteUserIdOrderByFechaMovimientoDesc(Long clienteId);
}
