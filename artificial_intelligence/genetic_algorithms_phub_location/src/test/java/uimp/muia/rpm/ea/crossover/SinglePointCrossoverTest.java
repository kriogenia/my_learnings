package uimp.muia.rpm.ea.crossover;

import org.junit.jupiter.api.Test;
import uimp.muia.rpm.ea.individual.TestIndividual;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SinglePointCrossoverTest {

    private static final long SEED = 42;

    @Test
    void testApply() {
        var rand = new Random();
        rand.setSeed(SEED);
        var left = new TestIndividual(new Byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });
        var right = new TestIndividual(new Byte[] { 0, 9, 8, 7, 6, 5, 4, 3, 2, 1 });
        var pointCut = rand.nextInt(left.size() + 1);
        assertTrue(pointCut <= left.size()); // change seed if the cut generates no crossover

        var spc = new SinglePointCrossover<>();
        rand.setSeed(SEED);
        spc.setRandom(rand);

        var child = spc.apply(left, right);
        for (var i = 0; i < pointCut; i++) {
            assertEquals(left.chromosome()[i], child.chromosome()[i]);
        }
        for (var i = pointCut; i < right.size(); i++) {
            assertEquals(right.chromosome()[i], child.chromosome()[i]);
        }
    }

}