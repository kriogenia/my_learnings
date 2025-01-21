package uimp.muia.rpm;

import uimp.muia.rpm.ea.EvolutionaryAlgorithm;
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
 * Executes the algorithm auto-scaling the parameters while searching for the exact solution and generates the
 * benchmarking stats
 */
public class OptimalSearchBenchmark {

    private static final int BASE_SEED = 19;
    private static final int EXECUTIONS = 30;
    private static final int N_SOLUTIONS = 20;


    public static void main(String[] args) throws Exception {
        var solutions = loadSolutions();
        System.out.println("n,p,objective,mutation,population,evaluations,time,total");
        for (var solution : solutions) {
            var params = new Params();
            var totalTime = 0L;
            while (params.hasNext()) {
                var measurements = new ArrayList<Result>();
                for (int i = 0; i < EXECUTIONS; i++) {
                    var result = run(solution, BASE_SEED + i, params);
                    measurements.add(result);
                }
                var hits = filterHits(measurements, solution.allocation);
                if (!hits.isEmpty()) {
                    printResults(solution, params, hits, totalTime);
                    break;
                }
                totalTime += measurements.stream().map(Result::time).reduce(0L, Long::sum) / EXECUTIONS;
                System.err.println(params);
                params.advance();
            }

        }
    }

    private static Result run(Solution solution, long seed, Params params) throws Exception {
        var file = "subproblems/phub_%d.%d.txt".formatted(solution.n, solution.p);
        var subproblem = OptimalSearchBenchmark.class.getClassLoader().getResource(file);
        assert subproblem != null;

        var phub = SubProblem.fromFile(Path.of(subproblem.toURI()));
        var problem = new USApHMP(phub);

        var ea = new EvolutionaryAlgorithm.Builder<>(problem)
                .withSeed(seed)
                .withPopulationSize(params.population())
                .withStop(new OrStop<FixedPAssignedHub>()
                        .add(new BestSolutionStop(solution.p, solution.allocation))
                        .add(new MaxEvaluationsStop<>(params.maxEvaluations())))
                .withSelector(new BinaryTournament<>())
                .withCrossover(new FixedPSinglePointCrossover())
                .withMutation(new ReassignHubMutation(params.mutation()))
                .withReplacement(new ElitistReplacement<>())
                .build();

        var start = Instant.now();
        var best = ea.run();
        var elapsed = Duration.between(start, Instant.now()).toNanos();

        return new Result(best, ea.getEvaluations(), elapsed);
    }

    private static Set<Solution> loadSolutions() throws URISyntaxException, IOException {
        var phub3 = OptimalSearchBenchmark.class.getClassLoader().getResource("or-library/phub3.txt");
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

    private static void printResults(Solution solution, Params params, List<Result> hits, long totalTime) {
        var evaluations = hits.stream().map(Result::evaluations).reduce(0L, Long::sum) / hits.size();
        var time = hits.stream().map(Result::time).reduce(0L, Long::sum) / hits.size();
        System.out.printf("%d,%d,%.0f,%.2f,%d,%d,%d,%d%n", solution.n, solution.p, solution.objective,
                params.mutation(), params.population(), evaluations, time, totalTime + time);
    }

    private static List<Result> filterHits(Collection<Result> results, Byte[] best) {
        return results.stream().filter(r -> Arrays.equals(r.best().chromosome(), best)).toList();
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

    static class Params {

        private static final List<Double> MUTATIONS = List.of(0.5, 0.25, 0.1);
        private static final List<Integer> POPULATIONS= List.of(10, 20, 25, 40, 50, 75, 100);

        private static final long MAX_EVALUATIONS_START = 0x1000;    // 4x1024
        private static final long MAX_EVALUATIONS_LIMIT = 0x100000;  // 1024x1024

        private long maxEvaluations;
        private int mutation;
        private int population;

        boolean limit;

        Params() {
            this.mutation = 0;
            this.maxEvaluations = MAX_EVALUATIONS_START;
            this.population = 0;
            this.limit = false;
        }

        int population() {
            return POPULATIONS.get(population);
        }

        double mutation() {
            return MUTATIONS.get(mutation);
        }

        long maxEvaluations() {
            return maxEvaluations;
        }

        boolean hasNext() {
            return !limit;
        }

        void advance() {
            if (++mutation < MUTATIONS.size()) {
                return;
            }
            if (maxEvaluations * 4 <= MAX_EVALUATIONS_LIMIT) {
                mutation = 0;
                maxEvaluations *= 4;
                return;
            }
            if (++population < POPULATIONS.size()) {
                maxEvaluations = MAX_EVALUATIONS_START;
                mutation = 0;
                return;
            }
            assert !limit;
            this.limit = true;
        }

        @Override
        public String toString() {
            return "Params{" +
                    "maxEvaluations=" + maxEvaluations +
                    ", mutation=" + MUTATIONS.get(mutation) +
                    ", population=" + POPULATIONS.get(population) +
                    '}';
        }
    }

}
