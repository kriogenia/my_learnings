package uimp.muia.rpm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uimp.muia.rpm.ea.EvolutionaryAlgorithm;
import uimp.muia.rpm.ea.crossover.FixedPSinglePointCrossover;
import uimp.muia.rpm.ea.mutation.ReassignHubMutation;
import uimp.muia.rpm.ea.phub.SubProblem;
import uimp.muia.rpm.ea.phub.USApHMP;
import uimp.muia.rpm.ea.replacement.ElitistReplacement;
import uimp.muia.rpm.ea.selection.BinaryTournament;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Performs a single execution of the algorithm until the stop criterion is met
 */
public class Run {

    private static final Logger LOG = LoggerFactory.getLogger(Run.class);

    public static void main(String[] input) throws Exception {
        var args = Args.parse(input);
        var file = "subproblems/phub_%d.%d.txt".formatted(args.n(), args.p());
        var subproblem = Run.class.getClassLoader().getResource(file);
        if (subproblem == null) {
            throw new IllegalArgumentException("Subproblem not found. Maybe it was generated yet?");
        }

        var phub = SubProblem.fromFile(Path.of(subproblem.toURI()));
        var problem = new USApHMP(phub);
        LOG.atInfo().log("Loaded and instantiated problem {}", file);

        var builder = new EvolutionaryAlgorithm.Builder<>(problem)
                .withPopulationSize(args.population())
                .withMaxEvaluations(args.limit())
                .withSelector(new BinaryTournament<>())
                .withCrossover(new FixedPSinglePointCrossover())
                .withMutation(new ReassignHubMutation(1.0 / phub.n()))
                .withReplacement(new ElitistReplacement<>());
        args.seed().ifPresent(builder::withSeed);
        var ea = builder.build();

        var best = ea.run();
        System.out.println(Arrays.toString(best.chromosome()));
    }

    record Args(
            int n,
            int p,
            int limit,
            int population,
            Optional<Long> seed
    ) {

        private static final int MIN_N = 10;
        private static final int MAX_N = 50;

        private static final int MIN_P = 2;
        private static final int MAX_P = 5;

        private static final int DEFAULT_LIMIT = 10_000;
        private static final int DEFAULT_POPULATION_SIZE = 10;

        static Args parse(String[] args) {
            try {
                var n = Integer.parseInt(args[0]);
                if (n < MIN_N || n > MAX_N || n % MIN_N != 0) {
                    throw new IllegalArgumentException("N must be one of [10, 20, 30, 40, 50]");
                }
                assert IntStream.rangeClosed(1, 5).map(i -> i * 10).anyMatch(i -> i == n);

                var p = Integer.parseInt(args[1]);
                if (p < MIN_P || p > MAX_P) {
                    throw new IllegalArgumentException("P must be between %d and %d".formatted(MIN_P, MAX_P));
                }

                Optional<Long> seed = Optional.empty();
                int limit = DEFAULT_LIMIT;
                int population = DEFAULT_POPULATION_SIZE;

                for (int i = 2; i <= 6; i++) {
                    if (args.length < i + 1) {
                        break;
                    }

                    switch (args[i]) {
                        case "--seed": seed = Optional.of(Long.parseLong(args[++i])); break;
                        case "--limit": limit = Integer.parseInt(args[++i]); break;
                        case "--population": population = Integer.parseInt(args[++i]); break;
                        default: throw new IllegalArgumentException("Unknown option: " + args[i]);
                    }
                }

                return new Args(n, p, limit, population, seed);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid arguments: %s. The usage is: N P [--seed SEED][--limit LIMIT][--population SIZE]".formatted(e.getMessage()), e);
            }
        }

    }

}
