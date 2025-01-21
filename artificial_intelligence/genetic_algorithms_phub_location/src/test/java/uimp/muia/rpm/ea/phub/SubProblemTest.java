package uimp.muia.rpm.ea.phub;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubProblemTest {

    private static final double DELTA = 0.001;

    @Test
    void fromFile() throws Exception {
        var uri = Objects.requireNonNull(getClass().getClassLoader().getResource("20.3.txt")).toURI();
        var phub = SubProblem.fromFile(Path.of(uri));
        assertEquals(20, phub.n());
        assertEquals(12944.330389, phub.nodes[0].x(), DELTA);
        assertEquals(19522.690462, phub.nodes[0].y(), DELTA);
        assertEquals(10.396320, phub.flows[1][3], DELTA);
        assertEquals(3, phub.p());
        assertEquals(3, phub.collectionCost(), DELTA);
        assertEquals(0.75, phub.transferCost(), DELTA);
        assertEquals(2, phub.distributionCost(), DELTA);
    }

    @Test
    void distanceTo() {
        var from = new SubProblem.Coordinates(0.0, 0.0);
        var to = new SubProblem.Coordinates(3000.0, 4000.0);
        assertEquals(5.0, from.distanceTo(to), DELTA);
    }
}