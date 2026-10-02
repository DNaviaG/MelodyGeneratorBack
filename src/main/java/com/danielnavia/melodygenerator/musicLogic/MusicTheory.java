package com.danielnavia.melodygenerator.musicLogic;

import com.danielnavia.melodygenerator.model.*;

import java.util.Arrays;
import java.util.List;

/**
 * Consultas de teoría musical: intervalos de escala, calidades de acorde y las notas
 * que forman ambos.
 *
 * <p>Es una clase de utilidades sin estado: todos sus métodos son estáticos y no
 * dependen de nada de la instancia.
 */
public class MusicTheory {

    private static final int SEMITONES_PER_OCTAVE = 12;

    /**
     * Semitonos que separan la tónica de la nota de un grado de la escala.
     *
     * <p>Los grados se numeran en notación romana en mayúsculas sin distinguir el
     * acorde que forman, porque aquí solo importa su altura dentro de la escala. En
     * modo menor, el segundo, tercer y sexto grado bajan un semitono respecto al mayor.
     *
     * @param scale escala sobre la que se mide el intervalo
     * @param degree grado de la escala, del I al VII
     * @return intervalo en semitones desde la tónica, entre 0 y 11
     */
    public static int getScaleInterval(Scale scale, ScaleDegree degree) {
        if (isMajor(scale)) {
            return switch (degree) {
                case I -> 0;
                case II -> 2;
                case III -> 4;
                case IV -> 5;
                case V -> 7;
                case VI -> 9;
                case VII -> 11;
            };
        }
        return switch (degree) {
            case I -> 0;
            case II -> 2;
            case III -> 3;
            case IV -> 5;
            case V -> 7;
            case VI -> 8;
            case VII -> 10;
        };
    }

    /**
     * Calidad del acorde que se forma sobre un grado de la escala.
     *
     * <p>Se deduce del intervalo de la tónica, de la tercera y de la quinta de cada
     * grado, así que no hace falta mantener una tabla: la escala ya dice cuál es.
     *
     * @param scale escala sobre la que se levanta el acorde
     * @param degree grado de la escala, del I al VII
     * @return calidad del acorde: mayor, menor o disminuido
     */
    public static ChordQuality getChordQuality(Scale scale, ScaleDegree degree) {
        if (isMajor(scale)) {
            return switch (degree) {
                case II, III, VI -> ChordQuality.MINOR;
                case VII -> ChordQuality.DIMINISHED;
                default -> ChordQuality.MAJOR;
            };
        }
        return switch (degree) {
            case I, IV, V -> ChordQuality.MINOR;
            case II -> ChordQuality.DIMINISHED;
            default -> ChordQuality.MAJOR;
        };
    }

    /**
     * Notas de un acorde trifásico a partir de su fundamental y su calidad.
     *
     * @param rootNote fundamental del acorde
     * @param chordQuality calidad que determina la tercera y la quinta
     * @return lista de tres notas en el orden fundamental, tercera, quinta
     */
    public static List<NoteName> getChordNotes(NoteName rootNote, ChordQuality chordQuality) {
        return List.of(
                rootNote,
                getNoteFromInterval(rootNote, chordQuality.getThirdInterval()),
                getNoteFromInterval(rootNote, chordQuality.getFifthInterval())
        );
    }

    /**
     * Notas del acorde que se forma sobre un grado de la escala.
     *
     * <p>Compone las dos consultas anteriores: el grado fija la fundamental y la
     * calidad sale de la propia escala.
     *
     * @param scale escala sobre la que se levanta el acorde
     * @param degree grado de la escala, del I al VII
     * @return lista de tres notas en el orden fundamental, tercera, quinta
     */
    public static List<NoteName> getChordNotes(Scale scale, ScaleDegree degree) {
        NoteName chordRoot = getNoteFromInterval(
                scale.getRootNote(),
                getScaleInterval(scale, degree)
        );
        return getChordNotes(chordRoot, getChordQuality(scale, degree));
    }

    /**
     * Nota que está a cierta distancia en semitonos de otra, subiendo o bajando.
     *
     * <p>El intervalo se envuelve con {@code floorMod} en lugar de sumar y restar a
     * mano: así un intervalo mayor que una octava, o uno negativo, dan el mismo
     * resultado sin casos especiales.
     *
     * @param rootNote nota de partida
     * @param interval distancia en semitonos; admite valores negativos
     * @return nota resultante, dentro de una octava
     */
    public static NoteName getNoteFromInterval(NoteName rootNote, int interval) {
        int semitone = Math.floorMod(rootNote.getSemitone() + interval, SEMITONES_PER_OCTAVE);
        return getNoteFromSemitone(semitone);
    }

    /**
     * Nota que corresponde a un semitono dentro de la octava.
     *
     * @param semitone posición del semitono, de 0 a 11
     * @return nota con ese semitono
     * @throws IllegalArgumentException si el semitono está fuera de la octava
     */
    public static NoteName getNoteFromSemitone(int semitone) {
        for (NoteName noteName : NoteName.values()) {
            if (noteName.getSemitone() == semitone) {
                return noteName;
            }
        }
        throw new IllegalArgumentException("No hay nota para el semitono " + semitone);
    }

    /**
     * Las siete notas de la escala, de la tónica al VII grado.
     *
     * <p>El índice en la lista es el grado: la posición de una nota en el resultado
     * es su número de grado, y el generador de notas se apoya en eso para medir
     * distancias.
     *
     * @param scale escala cuyas notas se quieren obtener
     * @return lista inmutable de siete notas, en orden de grado
     */
    public static List<NoteName> getScaleNotes(Scale scale) {
        return Arrays.stream(ScaleDegree.values())
                .map(degree -> getNoteFromInterval(
                        scale.getRootNote(),
                        getScaleInterval(scale, degree)
                ))
                .toList();
    }

    /**
     * Indica si una escala está en modo mayor.
     *
     * @param scale escala a comprobar
     * @return {@code true} si el modo es mayor
     */
    private static boolean isMajor(Scale scale) {
        return scale.getMode() == Mode.MAJOR;
    }
}