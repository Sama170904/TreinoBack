package com.treino.repository;

import com.treino.entity.PaqueteCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaqueteCreditoRepository extends JpaRepository<PaqueteCredito, Long> {
    
    @Query("SELECT p FROM PaqueteCredito p WHERE p.cliente.userId = :clienteId AND p.creditosDisponibles > 0 AND p.fechaExpiracion > :ahora AND p.estado = 'ACTIVO' ORDER BY p.fechaExpiracion ASC")
    List<PaqueteCredito> findAvailableCreditsFIFO(@Param("clienteId") Long clienteId, @Param("ahora") LocalDateTime ahora);

    @Modifying
    @Query("UPDATE PaqueteCredito p SET p.creditosDisponibles = p.creditosDisponibles - 1 WHERE p.creditoId = :id AND p.creditosDisponibles > 0")
    int consumirCreditoAtomico(@Param("id") Long id);
}
