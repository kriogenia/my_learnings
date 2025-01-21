package uimp.muia.rpm;

import uimp.muia.rpm.ea.EvolutionaryAlgorithm;
import uimp.muia.rpm.ea.Individual;
import uimp.muia.rpm.ea.Stop;
import uimp.muia.rpm.ea.crossover.FixedPSinglePointCrossover;
import uimp.muia.rpm.ea.individual.FixedPAssignedHub;
import uimp.muia.rpm.ea.mutation.ReassignHubMutation;
import uimp.muia.rpm.ea.phub.SubProblem;
import uimp.muia.rpm.ea.phub.USApHMP;
import uimp.muia.rpm.ea.replacement.ElitistReplacement;
import uimp.muia.rpm.ea.selection.BinaryTournament;
import uimp.muia.rpm.ea.stop.BestSolutionStop;
import uimp.muia.rpm.ea.stop.MaxEvaluationsStop;
import uimp.muia.rpm.ea.stop.OrStop;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Executes the algorithm a fixed number of times and generates the benchmark stats
 */
public class MaxEvaluationsBenchmark {

    private static final int BASE_SEED = 19;
    private static final int EXECUTIONS = 30;
    private static final int N_SOLUTIONS = 20;

    private static final List<Double> MUTATIONS = List.of(0.05, 0.1, 0.25, 0.5);

    public static void main(String[] args) throws Exception {
        var solutions = loadSolutions();
        System.out.println("n,p,mutation,objective,avg,gap,hit_rate,time");
        for (var solution : solutions) {
            for (var mutation : MUTATIONS) {
                var measurements = new ArrayList<Result>();
                for (int i = 0; i < EXECUTIONS; i++) {
                    var result = run(solution, BASE_SEED + i, mutation);
                    measurements.add(result);
                }
                printResults(solution, mutation, measurements);
            }
        }
    }

    private static Result run(Solution solution, long seed, double mutation) throws Exception {
        var file = "subproblems/phub_%d.%d.txt".formatted(solution.n, solution.p);
        var subproblem = MaxEvaluationsBenchmark.class.getClassLoader().getResource(file);
        assert subproblem != null;

        var phub = SubProblem.fromFile(Path.of(subproblem.toURI()));
        var problem = new USApHMP(phub);

        var ea = new EvolutionaryAlgorithm.Builder<>(problem)
                .withSeed(seed)
                .withPopulationSize(solution.n + 1)
                .withStop(new MaxEvaluationsStop<>(100_000))
                .withSelector(new BinaryTournament<>())
                .withCrossover(new FixedPSinglePointCrossover())
                .withMutation(new ReassignHubMutation(mutation))
                .withReplacement(new ElitistReplacement<>())
                .build();

        var start = Instant.now();
        var best = ea.run();
        var elapsed = Duration.between(start, Instant.now()).toNanos();

        return new Result(best, ea.getEvaluations(), elapsed);
    }

    private static Set<Solution> loadSolutions() throws URISyntaxException, IOException {
        var phub3 = MaxEvaluationsBenchmark.class.getClassLoader().getResource("or-library/phub3.txt");
        assert phub3 != null;
        var lines = Files.readAllLines(Path.of(phub3.toURI())).stream().skip(695).iterator();

        var solutions = new TreeSet<Solution>();
        while (solutions.size() < N_SOLUTIONS) {
            var header = lines.next();
            var n = Integer.parseInt(header.substring(15, 17));
            var p = Integer.parseInt(header.substring(21, 22));
            var objective = Double.parseDouble(lines.next().replace("Objective  : ", ""));
            var splits = lines.next().replace("Allocation : ", "").split(", ");
            var allocations = Arrays.stream(splits).map(Integer::parseInt).map(i -> (byte)(int)i).toArray(Byte[]::new);
            solutions.add(new Solution(n, p, objective, allocations));
            lines.next();
        }

        return solutions;
    }

    private static void printResults(Solution solution, double mutation, List<Result> results) {
        var solutions = results.stream().map(Result::best).toList();
        var hits = results.stream().filter(s -> Arrays.equals(solution.allocation, s.best.chromosome())).toList();
        var avg = -solutions.stream().map(Individual::fitness).reduce(0.0, Double::sum) / solutions.size();
        var gap = Math.abs(1 - solution.objective / avg);
        var hitRate = ((double) hits.size()) / results.size();
        var time = results.stream().map(Result::time).reduce(0L, Long::sum) / results.size();
        System.out.printf("%d,%d,%.2f,%.0f,%.0f,%.6f,%.3f,%d%n", solution.n, solution.p, mutation,  solution.objective,
                avg, gap, hitRate, time);
    }

    record Solution(
            int n,
            int p,
            double objective,
            Byte[] allocation
    ) implements Comparable<Solution> {

        Solution {
            // the provided solutions are based on index starting at 1
            allocation = Arrays.stream(allocation).map(x -> (byte)(x - 1)).toArray(Byte[]::new);
        }

        @Override
        public int compareTo(Solution o) {
            return (this.n + this.p) - (o.n + o.p);
        }
    }

    record Result(FixedPAssignedHub best, long evaluations, long time) {}

}
