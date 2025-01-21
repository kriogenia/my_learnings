package uimp.muia.rpm.ea.individual;

import uimp.muia.rpm.ea.Individual;

/**
 * Base implementation of Individuals to evade duplication of fitness logic.
 */
public abstract class BaseIndividual implements Individual {

    protected double fitness;

    BaseIndividual() {
        this.fitness = 0.0;
    }

    @Override
    public void fitness(double fitness) {
        this.fitness = fitness;
    }

    @Override
    public double fitness() {
        return fitness;
    }

}
