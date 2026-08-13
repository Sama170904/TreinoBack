package com.treino.repository;

import com.treino.entity.Clase;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Clase c WHERE c.claseId = :id AND c.estado = 'ACTIVO'")
    Optional<Clase> findByIdForUpdate(@Param("id") Long id);

    List<Clase> findAllByOrderByFechaHoraInicioAsc();
}
