package com.valeartola.turnos.domain.ports.out;

import com.valeartola.turnos.domain.model.ProfessionalAgenda;

/**
 * Puerto de salida: información vigente del catálogo.
 * El dominio no sabe que detrás hay una llamada REST al servicio de catálogo.
 */
public interface CatalogProvider {

    /**
     * @throws com.valeartola.turnos.domain.exception.ProfessionalNotFoundException si no existe
     * @throws com.valeartola.turnos.domain.exception.CatalogUnavailableException si el catálogo no responde
     */
    ProfessionalAgenda getProfessional(Long professionalId);
}