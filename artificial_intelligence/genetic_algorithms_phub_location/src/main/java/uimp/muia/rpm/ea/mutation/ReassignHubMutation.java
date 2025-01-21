package uimp.muia.rpm.ea.mutation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uimp.muia.rpm.ea.Mutation;
import uimp.muia.rpm.ea.Stochastic;
import uimp.muia.rpm.ea.individual.FixedPAssignedHub;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Mutates a chromosome taking a node assigned to a hub and making it the hub of the nodes assigned to it
 */
public class ReassignHubMutation implements Mutation<FixedPAssignedHub>, Stochastic {

    private static final Logger LOG = LoggerFactory.getLogger(ReassignHubMutation.class);

    private final double probability;

    private Random rand;

    public ReassignHubMutation(double probability) {
        this.probability = probability;
        this.rand = new Random();
    }

    @Override
    public FixedPAssignedHub apply(FixedPAssignedHub individual) {
        if (rand.nextDouble() >= probability) {
            return individual;
        }

        var currentHubs = individual.hubs();
        var chromosome = individual.chromosome();
        var randomNode = rand.ints(0, chromosome.length).boxed()
                .map(x -> (byte)(int)x)
                .filter(Predicate.not(currentHubs::contains))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unlimited stream returned empty"));
        var previousHub = chromosome[randomNode];
        IntStream.range(0, individual.size())
                .filter(i -> chromosome[i].equals(previousHub))
                .forEach(i -> chromosome[i] = randomNode);

        LOG.atTrace().log("Mutated chromosome {}", Arrays.toString(chromosome));
        return new FixedPAssignedHub(individual.p(), chromosome);
    }

    @Override
    public void setRandom(Random random) {
        this.rand = random;
    }
}
