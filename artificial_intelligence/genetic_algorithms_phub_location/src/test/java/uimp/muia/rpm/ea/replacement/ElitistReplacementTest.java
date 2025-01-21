package uimp.muia.rpm.ea.replacement;

import org.junit.jupiter.api.Test;
import uimp.muia.rpm.ea.Individual;
import uimp.muia.rpm.ea.individual.TestIndividual;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ElitistReplacementTest {

    private final ElitistReplacement<TestIndividual> er = new ElitistReplacement<>();

    private final List<TestIndividual> population = List.of(
            new TestIndividual(10.0),
            new TestIndividual(5.0),
            new TestIndividual(15.0),
            new TestIndividual(0.0)
    );

    @Test
    void replace() {
        var candidates = List.of(
                new TestIndividual(20.0),
                new TestIndividual(1.0),
                new TestIndividual(12.5)
        );

        var elite= er.replace(population, candidates);
        assertEquals(population.size(), elite.size());
        assertEquals(10.0, elite.stream().min(Individual::compareTo).map(Individual::fitness).orElseThrow(), 0.1);
        assertEquals(20.0, elite.stream().max(Individual::compareTo).map(Individual::fitness).orElseThrow(), 0.1);
    }

    @Test
    void replace_alreadyPresent() {
        var elite = er.replace(population, List.of(new TestIndividual(10.0)));
        assertEquals(population, elite);
    }

}