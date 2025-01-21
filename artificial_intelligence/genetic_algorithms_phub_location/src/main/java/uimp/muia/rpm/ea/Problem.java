package uimp.muia.rpm.ea;

/**
 * Version of es.uma.informatica.misia.ae.simpleea.Problem with generic to support different Individual
 * implementations without casting
 *
 * @param <I> Individual
 */
public interface Problem<I extends Individual> extends Stochastic {

    double evaluate(I individual);
    I generateRandomIndividual();

}
