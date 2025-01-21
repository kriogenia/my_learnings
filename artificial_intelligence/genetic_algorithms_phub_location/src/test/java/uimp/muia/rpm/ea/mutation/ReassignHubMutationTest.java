package uimp.muia.rpm.ea.mutation;

import org.junit.jupiter.api.Test;
import uimp.muia.rpm.ea.individual.FixedPAssignedHub;

import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ReassignHubMutationTest {

    @Test
    void apply() {
        var chromosome = new FixedPAssignedHub(1, new Byte[]{ 2, 2, 2 });
        var mutation = new ReassignHubMutation(1.0);
        mutation.setRandom(new Random(12L));

        var result = mutation.apply(chromosome);

        assertEquals(1, result.hubs().size());
        assertEquals(2, Stream.concat(chromosome.hubs().stream(), result.hubs().stream()).distinct().count());
        assertTrue(chromosome.hubs().stream().noneMatch(result.hubs()::contains));
    }
}