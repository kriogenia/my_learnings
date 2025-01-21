package uimp.muia.rpm.ea.crossover;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uimp.muia.rpm.ea.individual.FixedPAssignedHub;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FixedPSinglePointCrossoverTest {

    private final FixedPSinglePointCrossover crossover = new FixedPSinglePointCrossover();

    @BeforeEach
    void setUp() {
        crossover.setRandom(new Random(124L));
    }

    @Test
    void apply() {
        int p = 3;
        var left = new Byte[] { 1, 1, 2, 3, 1, 2 };
        var right = new Byte[] { 2, 3, 4, 2, 3, 4 };

        var result = crossover.apply(new FixedPAssignedHub(p, left), new FixedPAssignedHub(p, right));

        assertEquals(p, result.hubs().size());
        var possibleHubs = Stream.concat(Arrays.stream(left), Arrays.stream(right)).distinct().toList();
        assertTrue(Arrays.stream(result.chromosome()).allMatch(possibleHubs::contains));
        System.out.println(result);
    }

    @Test
    void addHub() {
        var chromosome = new Byte[]{ 1, 2, 1, 2, 1, 2 };

        var result = crossover.addHub(chromosome, 2);

        assertEquals(chromosome.length, result.length);
        assertEquals(4, Arrays.stream(result).distinct().count());
        assertTrue(Arrays.stream(result).allMatch(x -> x >= 0 && x < chromosome.length));
        assertEquals(4, Arrays.stream(result).filter(x -> x == 1 || x == 2).count());
    }

    @Test
    void removeHub() {
        var chromosome = new Byte[]{ 1, 2, 3, 4, 5, 6 };

        var result = crossover.removeHub(chromosome, 2);

        assertEquals(chromosome.length, result.length);
        assertEquals(4, Arrays.stream(result).distinct().count());
    }

}