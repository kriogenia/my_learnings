package uimp.muia.rpm.ea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uimp.muia.rpm.ea.crossover.SinglePointCrossover;
import uimp.muia.rpm.ea.mutation.NoMutation;
import uimp.muia.rpm.ea.replacement.ElitistReplacement;
import uimp.muia.rpm.ea.selection.BinaryTournament;
import uimp.muia.rpm.ea.stop.BestSolutionStop;
import uimp.muia.rpm.ea.stop.MaxEvaluationsStop;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EvolutionaryAlgorithm<I extends Individual> {

    private static final Logger LOG = LoggerFactory.getLogger(EvolutionaryAlgorithm.class);

    private final Problem<I> problem;
    private final Supplier<Random> newRandom;
    private final int populationSize;

    private final Stop stop;
    private final Selection<I> selection;
    private final Crossover<I> crossover;
    private final Mutation<I> mutation;
    private final Replacement<I> replacement;

    private long functionEvaluations = 0;
    private I best = null;

    private EvolutionaryAlgorithm(
            Problem<I> problem,
            Supplier<Random> newRandom,
            int populationSize,
            Stop stop,
            Selection<I> selection,
            Crossover<I> crossover,
            Mutation<I> mutation,
            Replacement<I> replacement
    ) {
        this.problem = problem;
        this.newRandom = newRandom;
        this.populationSize = populationSize;
        this.stop = stop;
        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.replacement = replacement;
    }

    public I run() {
        functionEvaluations = 0L;
        restartRandomness();

        LOG.atInfo().log("Generating initial population");
        var population = generateInitialPopulation();
        best = population.stream().reduce(population.getFirst(), this::evaluateIndividual);
        LOG.atInfo().log("Selected first best solution: {}", best);

        while (!stop.stop()) {
            I leftParent = selection.selectParent(population);
            I rightParent = selection.selectParent(population);
            I child = crossover.apply(leftParent, rightParent);
            child = mutation.apply(child);
            best = evaluateIndividual(best, child);
            population = replacement.replace(population, List.of(child));
            stop.update(this);
        }

        return best;
    }

    public long getEvaluations() {
        return functionEvaluations;
    }

    public I getBest() {
        return best;
    }

    /**
     * Evaluates an individual against the current best and return the best
     *
     * @param best      current best
     * @param candidate to evaluate
     * @return new best
     */
    private I evaluateIndividual(I best, I candidate) {
        var fitness = problem.evaluate(candidate);
        candidate.fitness(fitness);
        LOG.trace("Evaluated individual: {}", candidate);
        functionEvaluations++;
        if (candidate.compareTo(best) > 0) {
            LOG.info("New best solution: {}", candidate);
            return candidate;
        }
        return best;
    }

    private void restartRandomness() {
        var rnd = newRandom.get();
        Stream.of(problem, selection, crossover, mutation, replacement)
                .filter(Stochastic.class::isInstance)
                .map(Stochastic.class::cast)
                .forEach(x -> x.setRandom(rnd));
    }

    private List<I> generateInitialPopulation() {
        return IntStream.range(0, this.populationSize).mapToObj(_x -> problem.generateRandomIndividual()).toList();
    }

    public static class Builder<I extends Individual> {

        private final Problem<I> problem;

        private Supplier<Random> newRandom;
        private int populationSize;

        private Stop stop;
        private Selection<I> selection;
        private Crossover<I> crossover;
        private Mutation<I> mutation;
        private Replacement<I> replacement;

        public Builder(Problem<I> problem) {
            this.problem = problem;
            this.newRandom = Random::new;
            this.populationSize = 10;
            this.stop = new MaxEvaluationsStop(1_000L);
            this.selection = new BinaryTournament<>();
            this.crossover = new SinglePointCrossover<>();
            this.mutation = new NoMutation<>();
            this.replacement = new ElitistReplacement<>();
        }

        public Builder<I> withPopulationSize(int populationSize) {
            this.populationSize = populationSize;
            return this;
        }

        public Builder<I> withSeed(long seed) {
            this.newRandom = () -> new Random(seed);
            return this;
        }

        public Builder<I> withMaxEvaluations(int maxEvaluations) {
            this.stop = new MaxEvaluationsStop<I>(maxEvaluations);
            return this;
        }

        public Builder<I> withStop(Stop<I> stop) {
            this.stop = stop;
            return this;
        }

        public Builder<I> withSelector(Selection<I> selection) {
            this.selection = selection;
            return this;
        }

        public Builder<I> withCrossover(Crossover<I> crossover) {
            this.crossover = crossover;
            return this;
        }

        public Builder<I> withMutation(Mutation<I> mutation) {
            this.mutation = mutation;
            return this;
        }

        public Builder<I> withReplacement(Replacement<I> replacement) {
            this.replacement = replacement;
            return this;
        }

        public EvolutionaryAlgorithm<I> build() {
            return new EvolutionaryAlgorithm<>(
                    this.problem,
                    this.newRandom,
                    this.populationSize,
                    this.stop,
                    this.selection,
                    this.crossover,
                    this.mutation,
                    this.replacement
            );
        }

    }

}
