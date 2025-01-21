package uimp.muia.rpm.ea.mutation;

import uimp.muia.rpm.ea.Individual;
import uimp.muia.rpm.ea.Mutation;

public class NoMutation<I extends Individual> implements Mutation<I> {
    @Override
    public I apply(I individual) {
        return individual;
    }
}
