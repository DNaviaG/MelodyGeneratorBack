package com.danielnavia.melodygenerator.musicLogic;

import com.danielnavia.melodygenerator.model.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Decide qué nota suena en cada posición de un compás ya ritmado.
 *
 * <p>El compás pertenece a un acorde, así que por defecto se toca con notas del
 * acorde. Las excepciones son las notas ajenas al acorde, que aportan el movimiento
 * melódico, y están limitadas por tres reglas: como mucho una seguida, siempre con
 * una resolución de vuelta al acorde, y nunca dos saltos seguidos en la misma
 * dirección.
 *
 * <p>Se distinguen tres tipos por su función, no por ser la misma nota:
 * <ul>
 *   <li>de paso, entre dos notas del acorde, una por encima o por debajo</li>
 *   <li>de vecilla, al lado de la anterior, a la que se vuelve</li>
 *   <li>apoyada, sostenida sobre la fundamental, solo sobre la dominante</li>
 * </ul>
 *
 * <p>La resolución se guarda en {@link #pendingResolution} en vez de resolverse en
 * el acto, porque la nota siguiente puede caer en otro tiempo o en el compás
 * siguiente, y la promesa tiene que sobrevivir a ese salto.
 *
 * <p>El estado se reinicia en cada llamada a {@link #generateNotes}, así que la
 * generación de dos compases consecutivos no está conectada: por eso
 * {@code generateNotes} paga aquí las resoluciones que se le quedan colgando.
 */
@Data
public class MeasureNoteGenerator {
    /** Máximo de notas ajenas al acorde que pueden sonar seguidas. */
    private static final int MAX_CONSECUTIVE_NON_CHORD_NOTES = 1;

    /** Octava en la que se escriben todas las notas del compás. */
    private final int OCTAVE = 4;

    private final Random random;
    private final List<NoteName> scaleNotes;
    private final List<NoteName> chordNotes;
    private final List<NoteName> chordRootAndFifth;
    private final boolean dominant;
    private NoteName pendingResolution;
    private int consecutiveNonChordNotes;
    private int requiredStepDirection;

    /**
     * Prepara el generador para un compás sobre un grado concreto.
     *
     * @param scale escala del compás
     * @param degree grado que sostiene el acorde
     * @param random fuente de azar compartida con el generador de ritmo
     */
    public MeasureNoteGenerator(
            Scale scale,
            ScaleDegree degree,
            Random random
    ) {
        this.random = random;
        this.scaleNotes = MusicTheory.getScaleNotes(scale);
        this.chordNotes = MusicTheory.getChordNotes(scale, degree);
        this.chordRootAndFifth = List.of(chordNotes.get(0), chordNotes.get(2));
        this.dominant = degree == ScaleDegree.V;
    }

    /**
     * Indica si una nota pertenece al acorde del compás.
     *
     * @param note nota a comprobar
     * @return {@code true} si la nota es del acorde
     */
    private boolean isChordNote(NoteName note) {
        return note != null && chordNotes.contains(note);
    }

    /**
     * Grado que ocupa una nota dentro de la escala.
     *
     * @param note nota a localizar
     * @return índice del grado, o -1 si la nota no está en la escala
     */
    private int degreeOf(NoteName note) {
        return note == null ? -1 : scaleNotes.indexOf(note);
    }

    /**
     * Nota de un grado que puede salirse de la escala por arriba o por abajo.
     *
     * @param degree grado pedido, sin límites
     * @return nota del grado equivalente dentro de la escala
     */
    private NoteName noteAtWrappedDegree(int degree) {
        return scaleNotes.get(Math.floorMod(degree, scaleNotes.size()));
    }

    /**
     * Distancia con signo entre dos grados, contada por el camino más corto.
     *
     * <p>Importa que sea el más corto: un intervalo de quinta hacia arriba no es lo
     * mismo que una cuarta hacia abajo, pero en la escala de siete notas se miden
     * igual, y para decidir si un salto es de grado o de cuarta la dirección importa.
     *
     * @param fromDegree grado de partida
     * @param toDegree grado de llegada
     * @return semigrados con signo, entre -3 y 3
     */
    private int scaleDelta(int fromDegree, int toDegree) {
        int delta = Math.floorMod(toDegree - fromDegree, scaleNotes.size());
        if (delta > scaleNotes.size() / 2) {
            delta -= scaleNotes.size();
        }
        return delta;
    }

    /**
     * Indica si dos grados son vecinos en la escala.
     *
     * @param firstDegree primer grado
     * @param secondDegree segundo grado
     * @return {@code true} si hay exactamente un grado entre medias
     */
    private boolean isStepApart(int firstDegree, int secondDegree) {
        return firstDegree != -1 && secondDegree != -1
                && Math.abs(scaleDelta(firstDegree, secondDegree)) == 1;
    }

    /**
     * Cuenta una nota como tocada y actualiza el contador de notas ajenas al
     * acorde.
     *
     * @param note nota que acaba de sonar
     * @return la misma nota, para encadenar la llamada
     */
    private NoteName countAsPlayed(NoteName note) {
        consecutiveNonChordNotes = isChordNote(note) ? 0 : consecutiveNonChordNotes + 1;
        return note;
    }

    /**
     * Elige una nota del acorde, evitando repetir la anterior.
     *
     * <p>Se sortea entre fundamental y quinta, las dos notas estables del acorde. Si
     * sale la que ya sonaba, se va a la tercera: repetir la misma nota dos veces
     * seguidas suena a hueco.
     *
     * @param currentNote nota que acaba de sonar
     * @return nota del acorde, distinta de {@code currentNote} si es posible
     */
    private NoteName chooseChordNote(NoteName currentNote) {
        NoteName chosen = chordRootAndFifth.get(random.nextInt(chordRootAndFifth.size()));
        if (chosen.equals(currentNote)) {
            return chordNotes.get(1);
        }
        return chosen;
    }

    /**
     * Última nota de la melodía: una fundamental o una quinta del acorde.
     *
     * <p>Si había una resolución pendiente y sirve como nota final, se paga aquí: no
     * hace falta arrastrarla al principio del compás siguiente.
     *
     * @return nota con la que termina la melodía
     */
    private NoteName chooseFinalNote() {
        if (pendingResolution != null && chordRootAndFifth.contains(pendingResolution)) {
            NoteName resolution = pendingResolution;
            pendingResolution = null;
            return resolution;
        }
        return chordRootAndFifth.get(random.nextInt(chordRootAndFifth.size()));
    }

    /**
     * Nota de paso entre la anterior y la que vendría detrás.
     *
     * <p>Se comprueba en las dos direcciones cuál sirve: para que sea nota de paso,
     * la siguiente tiene que ser del acorde, y ella sola no. La resolución se guarda
     * en {@link #pendingResolution} para el siguiente turno.
     *
     * @param from nota desde la que se sale
     * @return nota de paso, o {@code null} si ninguna dirección sirve
     */
    private NoteName choosePassingNote(NoteName from) {
        int fromDegree = degreeOf(from);
        if (fromDegree == -1) {
            return null;
        }
        List<Integer> validDirections = new ArrayList<>();
        for (int direction : new int[]{1, -1}) {
            NoteName passing = noteAtWrappedDegree(fromDegree + direction);
            NoteName resolution = noteAtWrappedDegree(fromDegree + 2 * direction);
            if (isChordNote(resolution) && !isChordNote(passing)) {
                validDirections.add(direction);
            }
        }
        if (validDirections.isEmpty()) {
            return null;
        }
        int direction = validDirections.get(random.nextInt(validDirections.size()));
        pendingResolution = noteAtWrappedDegree(fromDegree + 2 * direction);
        return noteAtWrappedDegree(fromDegree + direction);
    }

    /**
     * Nota de vecilla: se va a un lado de la anterior y se vuelve a ella.
     *
     * @param from nota a la que se va a regresar
     * @return nota de vecilla, o {@code null} si arriba y abajo son notas del acorde
     */
    private NoteName chooseNeighbourNote(NoteName from) {
        int fromDegree = degreeOf(from);
        if (fromDegree == -1) {
            return null;
        }
        List<Integer> validDirections = new ArrayList<>();
        for (int direction : new int[]{1, -1}) {
            if (!isChordNote(noteAtWrappedDegree(fromDegree + direction))) {
                validDirections.add(direction);
            }
        }
        if (validDirections.isEmpty()) {
            return null;
        }
        int direction = validDirections.get(random.nextInt(validDirections.size()));
        pendingResolution = from;
        return noteAtWrappedDegree(fromDegree + direction);
    }

    /**
     * Nota apoyada: se agarra a la fundamental del acorde y se suelta.
     *
     * <p>Solo tiene sentido sobre la dominante, que es donde el acorde pide tensión y
     * por eso se usa este recurso. Además tiene que haber un salto de grado o más
     * desde la nota anterior; si se llega por escalones, no es una apoyada sino una
     * nota de paso y la habría duplicado.
     *
     * @param from nota desde la que se llega
     * @return nota apoyada, o {@code null} si no se puede colocar aquí
     */
    private NoteName chooseAppoggiaturaNote(NoteName from) {
        int rootDegree = degreeOf(chordNotes.getFirst());
        if (rootDegree == -1) {
            return null;
        }
        NoteName leaning = noteAtWrappedDegree(rootDegree + 1);
        if (isChordNote(leaning)) {
            return null;
        }
        int fromDegree = degreeOf(from);
        if (fromDegree == -1 || !isLeapApart(fromDegree, degreeOf(leaning))) {
            return null;
        }
        pendingResolution = chordNotes.getFirst();
        return leaning;
    }

    /**
     * Indica si dos grados están separados por más de un escalón.
     *
     * @param firstDegree primer grado
     * @param secondDegree segundo grado
     * @return {@code true} si entre medias hay al menos un grado
     */
    private boolean isLeapApart(int firstDegree, int secondDegree) {
        return firstDegree != -1 && secondDegree != -1
                && Math.abs(scaleDelta(firstDegree, secondDegree)) > 1;
    }

    /**
     * Corrige una nota elegida para que no repita la dirección del salto anterior.
     *
     * <p>Dos saltos seguidos hacia el mismo lado suenan a barrido, no a melodía. Si el
     * salto anterior fue hacia arriba, esta nota baja un escalón como mínimo, y al
     * revés. Se busca la nota del acorde más cercana en esa dirección: primero a un
     * escalón, y si cae fuera del acorde, se sigue hasta dos o tres escalones. La
     * marcha por escalones solo se acepta si se llega al acorde.
     *
     * @param currentNote nota que acaba de sonar
     * @param chosen nota que se iba a tocar
     * @return nota corregida, que puede ser la misma si no hay nada que corregir
     */
    private NoteName applyLeapCompensation(NoteName currentNote, NoteName chosen) {
        if (currentNote == null || chosen == null || requiredStepDirection == 0) {
            return chosen;
        }
        int fromDegree = degreeOf(currentNote);
        if (fromDegree == -1 || !isLeapApart(fromDegree, degreeOf(chosen))) {
            return chosen;
        }
        if (Math.signum(scaleDelta(fromDegree, degreeOf(chosen))) != -requiredStepDirection) {
            return chosen;
        }
        NoteName stepped = noteAtWrappedDegree(fromDegree + requiredStepDirection);
        if (isChordNote(stepped)) {
            pendingResolution = null;
            return stepped;
        }
        NoteName walked = stepped;
        for (int step = 2; step <= 3; step++) {
            NoteName candidate = noteAtWrappedDegree(fromDegree + requiredStepDirection * step);
            if (isChordNote(candidate)) {
                pendingResolution = null;
                return candidate;
            }
            walked = candidate;
        }
        pendingResolution = isChordNote(currentNote) ? currentNote : null;
        return walked;
    }

    /**
     * Anota el intervalo que se acaba de tocar para poder compensar el siguiente salto.
     *
     * <p>Solo deja dirección pendiente si ha habido salto: tras un escalón o tras la
     * primera nota no hay nada que compensar, y la marca a cero.
     *
     * @param previous nota anterior
     * @param current nota que acaba de sonar
     */
    private void registerInterval(NoteName previous, NoteName current) {
        int fromDegree = degreeOf(previous);
        int toDegree = degreeOf(current);
        if (fromDegree == -1 || toDegree == -1) {
            requiredStepDirection = 0;
            return;
        }
        int delta = scaleDelta(fromDegree, toDegree);
        requiredStepDirection = Math.abs(delta) > 1 ? -Integer.signum(delta) : 0;
    }

    /**
     * Elige la siguiente nota sin restricciones de tiempo fuerte.
     *
     * <p>Si ya se ha gastado la nota ajena al acorde permitida, se toca del acorde.
     * Si queda, se prueban los recursos melódicos en orden y se coge el primero que
     * devuelva algo, y si ninguno sirve, se cae al acorde: siempre suena una nota,
     * solo se pierde el color.
     *
     * @param currentNote nota que acaba de sonar
     * @return nota para el siguiente tiempo
     */
    private NoteName chooseFreeNote(NoteName currentNote) {
        if (currentNote == null
                || consecutiveNonChordNotes >= MAX_CONSECUTIVE_NON_CHORD_NOTES) {
            return chooseChordNote(currentNote);
        }
        for (Supplier<NoteName> candidate : nonChordNoteCandidates(currentNote)) {
            NoteName nonChordNote = candidate.get();
            if (nonChordNote != null) {
                return nonChordNote;
            }
        }
        return chooseChordNote(currentNote);
    }

    /**
     * Los recursos de nota ajena al acorde, en el orden en que se prueban.
     *
     * <p>Se devuelven como proveedores y no como notas porque ninguno está garantizado:
     * cada uno depende de lo que haya sonado antes y casi todos devuelven {@code null}
     * cuando no encajan. Se prueban en orden, no se sortea entre ellos, para que la
     * apoyada, la más difícil de colocar, no se lleve la nota buena.
     *
     * @param currentNote nota que acaba de sonar
     * @return lista de proveedores de notas, con al menos uno que siempre funciona
     */
    private List<Supplier<NoteName>> nonChordNoteCandidates(NoteName currentNote) {
        List<Supplier<NoteName>> candidates = new ArrayList<>();
        if (dominant) {
            candidates.add(() -> chooseAppoggiaturaNote(currentNote));
        }
        candidates.add(() -> choosePassingNote(currentNote));
        candidates.add(() -> chooseNeighbourNote(currentNote));
        return candidates;
    }

    /**
     * Elige la nota de una posición cualquiera del compás.
     *
     * <p>Las prioridades son fijas: primero una resolución pendiente, porque es una
     * deuda ya contraída; luego nota del acorde en tiempo fuerte, que es donde el
     * acorde se oye; y solo en los huecos, nota libre con su compensación de salto.
     *
     * @param currentNote nota que acaba de sonar
     * @param strongBeat si la posición es uno de los dos tiempos fuertes
     * @return nota para esta posición
     */
    private NoteName chooseNextNote(NoteName currentNote, boolean strongBeat) {
        NoteName chosen;
        if (pendingResolution != null) {
            chosen = pendingResolution;
            pendingResolution = null;
        } else if (strongBeat) {
            chosen = chooseChordNote(currentNote);
        } else {
            chosen = applyLeapCompensation(currentNote, chooseFreeNote(currentNote));
        }
        registerInterval(currentNote, chosen);
        return countAsPlayed(chosen);
    }

    /**
     * Rellena un compás con las notas que le tocan, dado su ritmo.
     *
     * <p>Todas las notas salen en la misma octava: el movimiento melódico se sostiene
     * en un solo registro, y la melodía no sube ni baja de la ventana en la que toca.
     *
     * @param durations ritmo del compás, cuya suma es la medida completa
     * @param cadence si el compás cierra con un silencio de resolución
     * @param lastMeasure si es el último compás de la melodía
     * @param previousNote última nota del compás anterior, o {@code null} al empezar
     * @return lista de notas del compás, en orden temporal
     */
    public List<Note> generateNotes(
            List<Duration> durations,
            boolean cadence,
            boolean lastMeasure,
            NoteName previousNote
    ) {
        pendingResolution = null;
        consecutiveNonChordNotes = 0;
        requiredStepDirection = 0;

        List<Note> notes = new ArrayList<>();
        NoteName currentNote = previousNote;
        int currentNotePosition = 0;

        for (int i = 0; i < durations.size(); i++) {
            Duration duration = durations.get(i);
            boolean strongBeat = currentNotePosition == TimeSignature.FIRST_STRONG || currentNotePosition == TimeSignature.SECOND_STRONG;
            boolean lastNoteOfMelody = lastMeasure && i == durations.size() - 1;
            if (lastNoteOfMelody) {
                currentNote = chooseFinalNote();
            } else {
                currentNote = chooseNextNote(currentNote, strongBeat);
            }
            notes.add(new Note(currentNote, OCTAVE, duration));
            currentNotePosition += duration.getUnits();
        }
        if (!lastMeasure && pendingResolution != null && !notes.isEmpty()) {
            // A NCT quedó colgando al final del compás. Si se deja así, el siguiente
            // compás no la sabe resolver: el generador se crea nuevo y no comparte el
            // estado. Así que se paga aquí, sustituyendo la última nota por su resolución
            // y conservando su duración, que es lo que hace que el compás siga midiendo 16.
            Note unresolved = notes.getLast();
            notes.set(notes.size() - 1, new Note(pendingResolution, OCTAVE, unresolved.getDuration()));
        }
        if (cadence) {
            silenceAfterCadence(notes);
        }
        return notes;
    }

    /**
     * El silencio marca la resolución, y la resolución la pide la armonía, no el
     * compás: por eso va detrás de la última nota del compás que está sobre la
     * dominante, que es el acorde que crea la tensión que quiere resolverse.
     *
     * Ocupa el hueco de esa nota y hereda su duración, no se suma al final: por eso el
     * compás sigue cuadrando a 16. Si se añadiera después de la última nota, el compás
     * mediría 16 más lo que durase el silencio y se rompería R1.
     *
     * No se coloca si la última nota cae en un tiempo fuerte, 0 u 8. Ahí un silencio no
     * es una respiración: es un corte, le quita el acento al compás, y aquí no hay nada
     * que cortar.
     */
    private void silenceAfterCadence(List<Note> notes) {
        int lastIndex = notes.size() - 1;
        if (lastIndex < 0) {
            return;
        }
        int position = 0;
        for (int i = 0; i < lastIndex; i++) {
            position += notes.get(i).getDuration().getUnits();
        }
        if (position == TimeSignature.FIRST_STRONG || position == TimeSignature.SECOND_STRONG) {
            return;
        }
        Note resolved = notes.get(lastIndex);
        notes.set(lastIndex, new Note(NoteName.REST, OCTAVE, resolved.getDuration()));
    }
}
