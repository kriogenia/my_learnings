package uimp.muia.rpm.ea.phub;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Represents the certain p-hub subproblem, containing all the logic given in the file and certain functions providing
 * calculated info from this data
 */
public final class SubProblem {

    final Coordinates[] nodes; // visibleForTest
    private final double[][] distances;
    final Double[][] flows; // visibleForTest
    private final int p;
    private final double collectionCost;
    private final double transferCost;
    private final double distributionCost;

    SubProblem(
            Coordinates[] nodes,
            Double[][] flows,
            int p,
            double collectionCost,
            double transferCost,
            double distributionCost
    ) {
        this.nodes = nodes;
        this.flows = flows;
        this.p = p;
        this.collectionCost = collectionCost;
        this.transferCost = transferCost;
        this.distributionCost = distributionCost;
        this.distances = calculateDistances(nodes);
    }

    public static SubProblem fromFile(Path path) throws IOException {
        var lines = Files.readAllLines(path);

        var numNodes = Integer.parseInt(lines.getFirst());
        var nodes = lines.stream().skip(1).limit(numNodes).map(Coordinates::parse).toArray(Coordinates[]::new);
        var flows = lines.stream()
                .skip(1 + numNodes)
                .limit(numNodes)
                .map(SubProblem::parseDoubles)
                .toArray(Double[][]::new);
        var hubs = Integer.parseInt(lines.get(1 + 2 * numNodes));
        var rest = lines.stream().skip(2 + 2L * numNodes).map(Double::parseDouble).iterator();

        return new SubProblem(nodes, flows, hubs, rest.next(), rest.next(), rest.next());
    }

    public int n() {
        return nodes.length;
    }

    public int p() {
        return p;
    }

    public double collectionCost() {
        return collectionCost;
    }

    public double transferCost() {
        return transferCost;
    }

    public double distributionCost() {
        return distributionCost;
    }

    public double distanceBetween(int origin, int destination) {
        return distances[origin][destination];
    }

    public double flowBetween(int origin, int destination) {
        return flows[origin][destination];
    }

    private static Double[] parseDoubles(String line) {
        return Arrays.stream(line.split(" ")).map(Double::parseDouble).toArray(Double[]::new);
    }

    private static double[][] calculateDistances(Coordinates[] nodes) {
        var length = nodes.length;
        var distances = new double[length][length];
        for (var i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                distances[i][j] = nodes[i].distanceTo(nodes[j]);
            }
        }
        return distances;
    }

    record Coordinates(double x, double y) {

        // normalizing distances is needed to find the best solution
        private static final double DISTANCE_UNIT = 1_000.0;

        static Coordinates parse(String line) {
            var splits = parseDoubles(line);
            return new Coordinates(splits[0], splits[1]);
        }

        public double distanceTo(Coordinates destiny) {
            var distance = Math.sqrt(Math.pow(destiny.x - this.x, 2) + Math.pow(destiny.y - this.y, 2));
            return distance / DISTANCE_UNIT;
        }
    }

}

