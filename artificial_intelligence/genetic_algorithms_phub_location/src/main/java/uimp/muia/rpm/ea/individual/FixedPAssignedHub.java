package uimp.muia.rpm.ea.individual;

import uimp.muia.rpm.ea.Individual;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Individual representation where each node points to its unique assigned hub and there's always `p` different hubs.
 * This Individual follows the restrictions of the Single Allocation p-Hub Median Problems like the USApHMP.
 */
public class FixedPAssignedHub extends BaseIndividual {

    private final int p;
    private final Byte[] assignedHubs;

    public FixedPAssignedHub(int p, Byte[] assignedHubs) {
        super();
        this.p = p;
        this.assignedHubs = assignedHubs;
        assert assertP() : "invalid chromosome p: %s, %s".formatted(p, Arrays.toString(assignedHubs));
    }

    @Override
    public Byte[] chromosome() {
        return assignedHubs.clone();
    }

    @Override
    public Individual replica() {
        return new FixedPAssignedHub(p, assignedHubs.clone());
    }

    public int p() {
        return p;
    }

    public List<Byte> hubs() {
        return Arrays.stream(assignedHubs).distinct().toList();
    }

    private boolean assertP() {
        return Arrays.stream(assignedHubs).distinct().count() == p;
    }

    @Override
    public String toString() {
        return "%s -> %f".formatted(Arrays.toString(assignedHubs), this.fitness);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FixedPAssignedHub that = (FixedPAssignedHub) o;
        return Objects.deepEquals(assignedHubs, that.assignedHubs);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(assignedHubs);
    }
}
