package uimp.muia.rpm.ea;

public interface Mutation<I extends Individual> {

    I apply(I individual);

}
