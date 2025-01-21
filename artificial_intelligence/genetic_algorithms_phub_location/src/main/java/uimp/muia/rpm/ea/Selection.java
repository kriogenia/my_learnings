package uimp.muia.rpm.ea;

import java.util.List;

public interface Selection<I extends Individual> {

    I selectParent(List<I> individuals);

}
