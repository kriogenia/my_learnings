package uimp.muia.rpm.ea;

import java.util.List;

public interface Replacement<I extends Individual> {

    List<I> replace(List<I> population, List<I> candidates);

}
