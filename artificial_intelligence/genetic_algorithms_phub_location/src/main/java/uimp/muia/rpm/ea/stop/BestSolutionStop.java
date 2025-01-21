package uimp.muia.rpm.ea.stop;

import uimp.muia.rpm.ea.EvolutionaryAlgorithm;
import uimp.muia.rpm.ea.Stop;
import uimp.muia.rpm.ea.individual.FixedPAssignedHub;

/**
 * Stops the algorithm when a solution matching the given one is found
 */
public class BestSolutionStop implements Stop<FixedPAssignedHub> {

    private final FixedPAssignedHub objective;

    private boolean found;

    public BestSolutionStop(int p, Byte[] allocations) {
        this.objective = new FixedPAssignedHub(p, allocations);
        this.found = false;
    }

    @Override
    public boolean stop() {
        return found;
    }

    @Override
    public void update(EvolutionaryAlgorithm<FixedPAssignedHub> algorithm) {
        this.found = objective.equals(algorithm.getBest());
    }

}
