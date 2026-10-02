package com.danielnavia.melodygenerator.musicLogic;

import com.danielnavia.melodygenerator.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Genera la duración de las notas de un compás y se la encarga a
 * {@link MeasureNoteGenerator} decidir los aciertos.
 *
 * <p>El ritmo se elige antes que la altura y sin relación con ella: un compás se
 * mide a 16, con un tiempo fuerte en la unidad 0 y otro en la unidad 8, y las
 * posiciones intermedias se llenan de corcheas o semicorcheas.
 *
 * <p>El estado que lleva entre compases es el historial rítmico, que existe para
 * repetir el ritmo de dos compases antes: es lo que da la sensación de frase en vez
 * de datos sueltos.
 */
public class MeasureGenerator {

    private static final int MAX_NOTES_PER_MEASURE = 6;
    private static final int MIN_SHORT_NOTES_IN_GAP = 2;
    private static final int MAX_SHORT_NOTES_IN_GAP = 4;

    private final Random random;
    private final List<List<Duration>> rhythmHistory = new ArrayList<>();

    /**
     * Crea una instancia del generador.
     *
     * @param random semilla que se comparte con el generador de notas, para
     *               que el ritmo y la altura no se sorts de forma independiente
     */
    public MeasureGenerator(Random random) {
        this.random = random;
    }

    /**
     * Compone un compás completo: su ritmo y sus notas.
     *
     * @param scale escala sobre la que se está generando
     * @param degree grado que sostiene el acorde de este compás
     * @param cadence si el compás resuelve sobre la dominante y lleva silencio
     *               después
     * @param lastMeasure si es el último compás de la melodía, en cuyo caso su
     *                    última nota se elige como resolución final
     * @param previousNote última nota del compás anterior, para dar continuidad,
     *                     o {@code null} al empezar
     * @return compás con las notas ya colocadas
     */
    public Measure generateMeasure(
            Scale scale,
            ScaleDegree degree,
            boolean cadence,
            boolean lastMeasure,
            NoteName previousNote
    ) {
        List<Duration> durations = chooseDurations();
        MeasureNoteGenerator noteGenerator = new MeasureNoteGenerator(scale, degree, random);
        List<Note> notes = noteGenerator.generateNotes(
                durations,
                cadence,
                lastMeasure,
                previousNote
        );
        return new Measure(notes);
    }

    /**
     * Ritmo del compás, reutilizando el de dos compases atrás cuando ya existe.
     *
     * <p>Devuelve la misma lista del historial, no una copia: es el mismo ritmo en los
     * dos sitios, y mutarla en un compás lo cambiaría en el otro.
     *
     * @return lista de duraciones que suman una medida completa
     */
    private List<Duration> chooseDurations() {
        int measureIndex = rhythmHistory.size();
        if (measureIndex >= 2) {
            List<Duration> repeated = rhythmHistory.get(measureIndex - 2);
            rhythmHistory.add(repeated);
            return repeated;
        }
        List<Duration> durations = buildDurations();
        rhythmHistory.add(durations);
        return durations;
    }

    /**
     * Construye un ritmo nuevo partiendo los dos tiempos fuertes.
     *
     * <p>Cada tiempo fuerte es una blanca o una negra, nunca dos blancas seguidas: un
     * compás de cuatro tiempos todo a blancas se queda sin hueco donde meter nada
     * contrario. Lo que queda de cada mitad se reparte en corcheas o semicorcheas, con
     * un tope de seis notas por compás para que no se llene de semicorcheas.
     *
     * @return lista de duraciones que suman una medida completa
     */
    private List<Duration> buildDurations() {
        Duration firstStrong = random.nextBoolean() ? Duration.HALF : Duration.QUARTER;
        Duration secondStrong = random.nextBoolean() ? Duration.HALF : Duration.QUARTER;
        if (firstStrong == Duration.HALF && secondStrong == Duration.HALF) {
            secondStrong = Duration.QUARTER;
        }

        int firstGap = TimeSignature.SECOND_STRONG - firstStrong.getUnits();
        int secondGap = TimeSignature.UNITS_PER_MEASURE
                - TimeSignature.SECOND_STRONG
                - secondStrong.getUnits();

        int budget = MAX_NOTES_PER_MEASURE - 2;
        int minimumForSecondGap = secondGap > 0 ? MIN_SHORT_NOTES_IN_GAP : 0;
        int firstGapCount = shortNoteCount(firstGap, budget - minimumForSecondGap);
        int secondGapCount = shortNoteCount(secondGap, budget - firstGapCount);

        List<Duration> durations = new ArrayList<>();
        durations.add(firstStrong);
        durations.addAll(shortNotes(firstGap, firstGapCount));
        durations.add(secondStrong);
        durations.addAll(shortNotes(secondGap, secondGapCount));
        return durations;
    }

    /**
     * Cuántas notas cortas caben en un hueco de los que quedan entre tiempo fuerte y
     * final de la mitad.
     *
     * <p>Es aleatorio dentro de un rango y luego se recorta al presupuesto que queda:
     * el número de notas del compás está acotado, así que el primer hueco se queda con
     * lo que sobra y el segundo no puede pedir más de lo que le dejan.
     *
     * @param gapUnits duración del hueco, en unidades de semicorchea
     * @param budget número de notas que aún se pueden gastar
     * @return cantidad de notas cortas a colocar en el hueco
     */
    private int shortNoteCount(int gapUnits, int budget) {
        if (gapUnits == 0 || budget < MIN_SHORT_NOTES_IN_GAP) {
            return 0;
        }
        int choice = MIN_SHORT_NOTES_IN_GAP
                + random.nextInt(MAX_SHORT_NOTES_IN_GAP - MIN_SHORT_NOTES_IN_GAP + 1);
        return Math.min(choice, budget);
    }

    /**
     * Elige las duraciones de las notas que llenan un hueco.
     *
     * <p>La última nota se ve obligada: si el hueco no da para lo que faltaba, se
     * alarga a una corchea o se acorta a semicorchea, y las anteriores se van
     * sorteando. Es la única forma de que la suma cuadre sin tener que probar
     * combinaciones.
     *
     * @param gapUnits duración del hueco, en unidades de semicorchea
     * @param count número de notas que se van a colocar en el hueco
     * @return lista de duraciones cuya suma es exactamente {@code gapUnits}
     */
    private List<Duration> shortNotes(int gapUnits, int count) {
        List<Duration> notes = new ArrayList<>();
        int remaining = gapUnits;
        for (int i = 0; i < count; i++) {
            int notesLeft = count - i;
            Duration chosen;
            if (remaining == notesLeft) {
                chosen = Duration.SIXTEENTH;
            } else if (remaining == notesLeft * Duration.EIGHTH.getUnits()) {
                chosen = Duration.EIGHTH;
            } else {
                chosen = random.nextBoolean() ? Duration.EIGHTH : Duration.SIXTEENTH;
            }
            notes.add(chosen);
            remaining -= chosen.getUnits();
        }
        return notes;
    }
}
