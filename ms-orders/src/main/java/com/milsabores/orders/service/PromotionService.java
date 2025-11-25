package com.milsabores.orders.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio encargado de evaluar qué promoción aplica a un usuario dado.
 *
 * Reglas:
 *  - Adulto mayor 50%: Personas de 50+ años reciben 50% de descuento.
 *  - FELICES50 10%: Descuento de por vida si se registró con ese código.
 *  - Duoc 25% cumpleaños: Estudiantes Duoc (email @duocuc.cl) reciben 25% en su cumpleaños.
 *
 * Importante:
 *  - Las promociones NO se acumulan, se aplica la de mayor porcentaje.
 */
@Service
public class PromotionService {

    public AppliedPromotion evaluatePromotion(String email,
                                              LocalDate birthDate,
                                              String registrationCode,
                                              LocalDate today) {

        List<AppliedPromotion> candidates = new ArrayList<>();

        // 1) Adulto mayor 50% (50+)
        if (birthDate != null) {
            int years = Period.between(birthDate, today).getYears();
            if (years >= 50) {
                candidates.add(new AppliedPromotion(
                        "ADULTO_MAYOR",
                        50,
                        "Descuento 50% por adulto mayor (50+ años)"
                ));
            }
        }

        // 2) FELICES50 10% de por vida
        if (registrationCode != null && !registrationCode.isBlank()
                && "FELICES50".equalsIgnoreCase(registrationCode.trim())) {
            candidates.add(new AppliedPromotion(
                    "FELICES50",
                    10,
                    "Descuento 10% de por vida por código FELICES50"
            ));
        }

        // 3) Estudiante Duoc 25% en su cumpleaños
        if (email != null && birthDate != null) {
            String lowerEmail = email.toLowerCase();
            boolean isDuoc = lowerEmail.endsWith("@duocuc.cl");

            boolean isBirthday =
                    birthDate.getMonth() == today.getMonth()
                            && birthDate.getDayOfMonth() == today.getDayOfMonth();

            if (isDuoc && isBirthday) {
                candidates.add(new AppliedPromotion(
                        "DUOC_CUMPLE",
                        25,
                        "Descuento 25% por cumpleaños de estudiante Duoc"
                ));
            }
        }

        // Si no hay promos, devolvemos null
        if (candidates.isEmpty()) {
            return null;
        }

        // Nos quedamos con la promo de MAYOR porcentaje
        return candidates.stream()
                .max(Comparator.comparingInt(AppliedPromotion::percentage))
                .orElse(null);
    }

    /**
     * Resultado de una promoción aplicada.
     */
    public record AppliedPromotion(
            String code,        // "ADULTO_MAYOR", "FELICES50", "DUOC_CUMPLE"
            int percentage,     // 50, 10, 25
            String description  // Texto amigable
    ) {
    }
}
