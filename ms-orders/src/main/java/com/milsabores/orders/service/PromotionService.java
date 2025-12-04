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
 *  - FELICES50 10%: Descuento de por vida si se registró con ese código
 *    o si escribe el cupón FELICES50 en el checkout.
 *  - Duoc 25% cumpleaños: Estudiantes Duoc (email @duocuc.cl) reciben 25% en su cumpleaños.
 *
 * Importante:
 *  - Las promociones NO se acumulan, se aplica la de mayor porcentaje.
 */
@Service
public class PromotionService {

    /**
     * Versión original (sin cupón manual). La dejamos por compatibilidad.
     */
    public AppliedPromotion evaluatePromotion(String email,
                                              LocalDate birthDate,
                                              String registrationCode,
                                              LocalDate today) {
        return evaluatePromotion(email, birthDate, registrationCode, null, today);
    }

    /**
     * Versión extendida que también considera un cupón manual
     * escrito por el cliente en el formulario de pedido.
     */
    public AppliedPromotion evaluatePromotion(String email,
                                              LocalDate birthDate,
                                              String registrationCode,
                                              String manualCode,
                                              LocalDate today) {

        List<AppliedPromotion> candidates = new ArrayList<>();

        // 0) Cupón manual escrito en el checkout (si existe)
        if (manualCode != null && !manualCode.isBlank()) {
            String code = manualCode.trim().toUpperCase();

            // Por ahora sólo soportamos FELICES50 como cupón manual,
            // pero aquí podrías ir sumando más códigos.
            if ("FELICES50".equals(code)) {
                candidates.add(new AppliedPromotion(
                        "FELICES50",
                        10,
                        "Descuento 10% por cupón FELICES50"
                ));
            }
        }

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

        // 2) FELICES50 10% de por vida (registrado en la cuenta)
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
