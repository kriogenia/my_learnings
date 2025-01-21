package uimp.muia.rpm.ea;

/**
 * Handler to discern if the algorithm should terminate or continue
 * @param <I> type of Individual
 */
public interface Stop<I extends Individual> {

    boolean stop();
    void update(EvolutionaryAlgorithm<I> algorithm);

}
