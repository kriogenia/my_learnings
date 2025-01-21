package uimp.muia.rpm.ea.phub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class USApHMPTest {

    private final Random rand = new Random();

    @BeforeEach
    void setUp() {
        rand.setSeed(123L);
    }

    @Test
    void evaluate() {
    }

    @Test
    void generateRandomIndividual() {
        int n = 10;
        int p = 3;

        var scenario = new SubProblem(generateNodes(n), new Double[n][n], p, 0.0, 0.0, 0.0);
        var problem = new USApHMP(scenario);
        problem.setRandom(rand);

        var ind = problem.generateRandomIndividual();
        assertEquals(n, ind.size());
        assertEquals(p, ind.hubs().size());
    }

    private SubProblem.Coordinates[] generateNodes(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> new SubProblem.Coordinates(rand.nextDouble(), rand.nextDouble()))
                .toArray(SubProblem.Coordinates[]::new);
    }
}