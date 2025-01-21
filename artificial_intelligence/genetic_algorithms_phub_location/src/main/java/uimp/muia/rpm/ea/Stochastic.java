package uimp.muia.rpm.ea;

import java.util.Random;

/**
 * Stochastic component that can accept a `Random` object to ensure seed consistency
 */
public interface Stochastic {

    void setRandom(Random random);

}
