package com.valeartola.turnos.infrastructure.web;

import com.valeartola.turnos.domain.ports.in.GetAvailabilityUseCase;
import com.valeartola.turnos.infrastructure.web.dto.AvailableSlotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Adaptador de entrada REST: turnos disponibles de un profesional en una fecha. */
@RestController
@RequestMapping("/api/professionals")
@RequiredArgsConstructor
public class AvailabilityController {

    private final GetAvailabilityUseCase getAvailability;

    @GetMapping("/{professionalId}/availability")
    public List<AvailableSlotResponse> availability(
            @PathVariable Long professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return getAvailability.getAvailability(professionalId, date).stream()
                .map(AvailableSlotResponse::from)
                .toList();
    }
}
