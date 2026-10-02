package com.danielnavia.melodygenerator.model;

import lombok.Getter;

/**
 * Los nombres de nota que puede enviar la melodía.
 *
 * REST no es una nota: es la ausencia de sonido. Se queda aquí porque necesita
 * su propio valor en el enum, y ese valor es -1 para que la aritmética de
 * alturas lo descarte siempre: la escala y los acordes se calculan con las 12
 * notas de verdad, nunca con el silencio.
 *
 * Cada nota lleva su semitono escrito, así que el orden del enum ya no importa
 * para calcular alturas.
 */
@Getter
public enum NoteName {
    C(0),
    C_SHARP(1),
    D(2),
    D_SHARP(3),
    E(4),
    F(5),
    F_SHARP(6),
    G(7),
    G_SHARP(8),
    A(9),
    A_SHARP(10),
    B(11),
    REST(-1);

    private final int semitone;

    NoteName(int semitone) {
        this.semitone = semitone;
    }
}