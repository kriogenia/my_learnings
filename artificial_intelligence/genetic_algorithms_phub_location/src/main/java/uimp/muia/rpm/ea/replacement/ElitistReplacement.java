package uimp.muia.rpm.ea.replacement;

import uimp.muia.rpm.ea.Individual;
import uimp.muia.rpm.ea.Replacement;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ElitistReplacement<I extends Individual> implements Replacement<I> {

    public ElitistReplacement() {}

    @Override
    public List<I> replace(List<I> population, List<I> candidates) {
        return Stream.concat(population.stream(), candidates.stream())
                .sorted(Comparator.reverseOrder())
                .distinct()
                .limit(population.size())
                .toList();
    }

}
