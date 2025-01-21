package uimp.muia.rpm.ea.individual;

import uimp.muia.rpm.ea.Individual;

import java.util.Arrays;
import java.util.Objects;

public class TestIndividual implements Individual {

    private double fitness = 0.0;
    private Byte[] chromosome = new Byte[10];

    public TestIndividual(double fitness) {
        this.fitness = fitness;
    }

    public TestIndividual(Byte[] chromosome) {
        this.chromosome = chromosome;
    }

    @Override
    public void fitness(double fitness) {
        this.fitness = fitness;
    }

    @Override
    public double fitness() {
        return fitness;
    }

    @Override
    public Byte[] chromosome() {
        return chromosome;
    }

    @Override
    public Individual replica() {
        return new TestIndividual(chromosome);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestIndividual that = (TestIndividual) o;
        return Double.compare(fitness, that.fitness) == 0 || Objects.deepEquals(chromosome, that.chromosome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fitness, Arrays.hashCode(chromosome));
    }

}
