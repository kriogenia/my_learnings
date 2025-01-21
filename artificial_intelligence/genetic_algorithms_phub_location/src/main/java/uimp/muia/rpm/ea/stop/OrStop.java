package uimp.muia.rpm.ea.stop;

import uimp.muia.rpm.ea.EvolutionaryAlgorithm;
import uimp.muia.rpm.ea.Individual;
import uimp.muia.rpm.ea.Stop;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite Stop to stop the algorithm when any of the given Stops calls for a halt
 * @param <I> type of Individual
 */
public class OrStop<I extends Individual> implements Stop<I> {

    private final List<Stop<I>> stops;

    public OrStop() {
        this.stops = new ArrayList<>();
    }

    public OrStop<I> add(Stop<I> stop) {
        this.stops.add(stop);
        return this;
    }

    @Override
    public boolean stop() {
        return stops.stream().anyMatch(Stop::stop);
    }

    @Override
    public void update(EvolutionaryAlgorithm<I> algorithm) {
        stops.forEach(stop -> stop.update(algorithm));
    }
}
