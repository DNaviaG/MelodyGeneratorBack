package com.danielnavia.melodygenerator.model;

/**
 * El compás: cuánto dura un compás y dónde están los acentos.
 *
 * <p>El proyecto usa 4/4 y la semicorchea como figura más pequeña, así que un compás
 * mide 16 unidades y los acentos caen en la unidad 0 (negra 1) y en la 8 (negra 3).</p>
 *
 * <p>Estos tres números estaban antes escritos a mano en dos clases distintas:
 * el 16 en el generador de compases y el 0 y el 8 en el de notas. Al estar separados,
 * nada decía que el 8 fuese la mitad del 16. Aquí viven juntos.</p>
 */
public class TimeSignature {

    public static final int UNITS_PER_MEASURE = 16;
    public static final int FIRST_STRONG = 0;
    public static final int SECOND_STRONG = UNITS_PER_MEASURE / 2;

    private TimeSignature() {
    }
}