package com.danielnavia.melodygenerator.musicLogic;

import com.danielnavia.melodygenerator.model.*;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Genera una melodía completa a partir de una escala.
 *
 * <p>La melodía sale de una progresión de acordes de cuatro compases, una escolhida
 * al azar de las doce de {@link #progressionsList}. Cada compás lo genera un
 * {@link MeasureGenerator} que se conserva durante toda la melodía, porque su
 * historial rítmico es lo que hace que los compases suenen a la misma frase y no a
 * datos sueltos.
 *
 * <p>El componente es determinista: la misma semilla produce siempre la misma
 * melodía.
 */
@Component
public class MelodyMaker {

    /**
     * Progresiones de cuatro grados, en notación romana.
     *
     * <p>Todas terminan en dominante o en tónica y ninguna pasa de largo: la melodía
     * tiene que poder cerrar.
     */
    private final List<List<ScaleDegree>> progressionsList = List.of(
        List.of(ScaleDegree.I, ScaleDegree.V, ScaleDegree.VI, ScaleDegree.IV),
        List.of(ScaleDegree.V, ScaleDegree.VI, ScaleDegree.IV, ScaleDegree.I),
        List.of(ScaleDegree.VI, ScaleDegree.IV, ScaleDegree.I, ScaleDegree.V),
        List.of(ScaleDegree.IV, ScaleDegree.I, ScaleDegree.V, ScaleDegree.VI),
        List.of(ScaleDegree.I, ScaleDegree.VI, ScaleDegree.IV, ScaleDegree.V),
        List.of(ScaleDegree.IV, ScaleDegree.V, ScaleDegree.I, ScaleDegree.VI),
        List.of(ScaleDegree.IV, ScaleDegree.V, ScaleDegree.VI, ScaleDegree.I),
        List.of(ScaleDegree.I, ScaleDegree.VI, ScaleDegree.II, ScaleDegree.V),
        List.of(ScaleDegree.VI, ScaleDegree.II, ScaleDegree.V, ScaleDegree.I),
        List.of(ScaleDegree.I, ScaleDegree.IV, ScaleDegree.V, ScaleDegree.I),
        List.of(ScaleDegree.I, ScaleDegree.IV, ScaleDegree.VI, ScaleDegree.V),
        List.of(ScaleDegree.I, ScaleDegree.V, ScaleDegree.II, ScaleDegree.IV)
    );

    /**
     * Genera una melodía completa en la escala indicada.
     *
     * <p>Recorre la progresión compás a compás, encadenando la última nota de uno con
     * el primero del siguiente para que no haya cortes. La dominante por medio de la
     * progresión recibe cadencia, que es un silencio de resolución; la dominante
     * final no, porque ahí ya no queda nada que resolver.
     *
     * @param scale escala de la melodía
     * @param seed semilla del generador aleatorio, la misma da siempre el mismo
     *             resultado
     * @return melodía con un compás por cada grado de la progresión
     */
    public Melody generateMelody(Scale scale, long seed) {
        Random random = new Random(seed);
        MeasureGenerator measureGenerator = new MeasureGenerator(random);
        List<ScaleDegree> progression = progressionsList.get(random.nextInt(progressionsList.size()));
        List<Measure> measures = new ArrayList<>();
        NoteName previousNote = null;
        for (int i = 0; i < progression.size(); i++) {
            ScaleDegree degree = progression.get(i);
            boolean lastMeasure = i == progression.size() - 1;
            boolean cadence = degree == ScaleDegree.V && !lastMeasure;
            Measure measure = measureGenerator.generateMeasure(
                scale,
                degree,
                cadence,
                lastMeasure,
                previousNote
            );
            measures.add(measure);
            NoteName lastNote = measure.getNotes().getLast().getNote();
            previousNote = lastNote == NoteName.REST ? null : lastNote;
        }
        return new Melody(scale, measures);
    }
}
