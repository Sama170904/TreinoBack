package com.treino.repository;

import com.treino.entity.PlantillaWhatsApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlantillaWhatsAppRepository extends JpaRepository<PlantillaWhatsApp, Long> {
    Optional<PlantillaWhatsApp> findByTipoEvento(PlantillaWhatsApp.TipoEvento tipoEvento);
}
