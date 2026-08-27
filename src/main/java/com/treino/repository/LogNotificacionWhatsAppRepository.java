package com.treino.repository;

import com.treino.entity.LogNotificacionWhatsApp;
import com.treino.entity.PlantillaWhatsApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogNotificacionWhatsAppRepository extends JpaRepository<LogNotificacionWhatsApp, Long> {
    boolean existsByTipoEventoAndReferenciaIdAndEstadoEnvio(
        PlantillaWhatsApp.TipoEvento tipoEvento, 
        Long referenciaId, 
        LogNotificacionWhatsApp.EstadoEnvio estadoEnvio
    );
}
