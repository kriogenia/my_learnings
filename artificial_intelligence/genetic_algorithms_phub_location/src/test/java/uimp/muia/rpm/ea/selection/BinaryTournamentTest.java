package uimp.muia.rpm.ea.selection;

import org.junit.jupiter.api.Test;
import uimp.muia.rpm.ea.individual.TestIndividual;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BinaryTournamentTest {

    private static final long SEED = 1;

    @Test
    void selectParent() {
        var bt = new BinaryTournament<TestIndividual>();
        var individuals = List.of(
                new TestIndividual(1.0),
                new TestIndividual(2.0),
                new TestIndividual(3.0)
        );

        var rand = new Random();
        rand.setSeed(SEED);
        var first = individuals.get(rand.nextInt(individuals.size()));
        var second = individuals.get(rand.nextInt(individuals.size()));
        assertNotEquals(first, second); // if equals, use different seed
        var expectedWinner = (first.fitness() >= second.fitness()) ? first : second;

        rand.setSeed(SEED);
        bt.setRandom(rand);
        var selected = bt.selectParent(individuals);

        assertEquals(expectedWinner, selected);
    }
}