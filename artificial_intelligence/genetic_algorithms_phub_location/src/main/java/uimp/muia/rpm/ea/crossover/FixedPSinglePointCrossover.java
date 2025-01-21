package uimp.muia.rpm.ea.crossover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uimp.muia.rpm.ea.Crossover;
import uimp.muia.rpm.ea.Stochastic;
import uimp.muia.rpm.ea.individual.FixedPAssignedHub;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Version of a single point crossover that corrects the chromosome of the child if its number of hubs goes above
 * or below `p`.
 */
public class FixedPSinglePointCrossover implements Crossover<FixedPAssignedHub>, Stochastic {

    private static final Logger LOG = LoggerFactory.getLogger(FixedPSinglePointCrossover.class);

    private Random rand;

    public FixedPSinglePointCrossover() {
        this.rand = new Random();
    }

    @Override
    public FixedPAssignedHub apply(FixedPAssignedHub left, FixedPAssignedHub right) {
        assert left.size() == right.size() && left.p() == right.p();
        LOG.atTrace().log("Crossing {} and {}", left.chromosome(), right.chromosome());
        var size = left.size();
        var p = left.p();

        var newChromosome = left.chromosome();
        var cutPoint = rand.nextInt(size + 1);
        if (cutPoint < size) {
            System.arraycopy(right.chromosome(), cutPoint, newChromosome, cutPoint, size - cutPoint);
        }

        // correct chromosome
        var corrected = correctChromosome(newChromosome, p).get();
        LOG.atDebug().log("Generated child {}", Arrays.toString(corrected));
        return new FixedPAssignedHub(p, corrected);
    }

    @Override
    public void setRandom(Random random) {
        this.rand = random;
    }


    private Supplier<Byte[]> correctChromosome(Byte[] chromosome, int target) {
        var difference = target - Arrays.stream(chromosome).distinct().count();
        if (difference > 0) {
            return () -> addHub(chromosome, difference);
        } else if (difference < 0 ) {
            return () -> removeHub(chromosome, -difference);
        } else {
            return () -> chromosome;
        }

    }

    /**
     * Adds `missing` hubs to the chromosome and assigns their nodes to themselves
     *
     * @param chromosome assigned hubs
     * @param missing    number of missing hubs (`p` - `current`)
     * @return modified chromosome
     */
    // Visible for testing
    Byte[] addHub(Byte[] chromosome, long missing) {
        LOG.atTrace().log("Child chromosome {} is below P, adding {} hubs", Arrays.toString(chromosome), missing);
        var hubs = Arrays.stream(chromosome).distinct().collect(Collectors.toCollection(ArrayList::new));

        for (var i = 0; i < missing; i++) {
            var newHub = rand.ints(0, chromosome.length)
                    .filter(x -> !hubs.contains((byte) x))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unlimited stream returned empty"));
            chromosome[newHub] = (byte) newHub;
            hubs.add((byte) newHub);
            // considering sending half the nodes of the most used node to the new one
        }

        return chromosome;
    }

    /**
     * Removes `exceeding` hubs and reassigns their nodes to a different hub
     * @param chromosome assigned hubs
     * @param exceeding  number of hubs to remove (`current` - `p`)
     * @return modified chromosome
     */
    Byte[] removeHub(Byte[] chromosome, long exceeding) {
        LOG.atTrace().log("Chromosome {} is above P, removing {} hubs", Arrays.toString(chromosome), exceeding);
        var hubs = Arrays.stream(chromosome).distinct().collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < exceeding; i++) {
            var randomHub = rand.nextInt(hubs.size());
            var hubToDelete = hubs.get(randomHub);
            var relocation = rand.ints(0, hubs.size())
                    .map(hubs::get)
                    .filter(x -> x != hubToDelete)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unlimited stream returned empty"));
            IntStream.range(0, chromosome.length)
                    .filter(x -> chromosome[x].equals(hubToDelete))
                    .forEach(x -> chromosome[x] = (byte) relocation);
            hubs.remove(randomHub);
        };

        return chromosome;
    }

}
