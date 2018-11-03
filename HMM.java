import java.util.*;

class HMM {

    private static final int STATES = 5;
    private static final int EMISSIONS = Constants.COUNT_MOVE;

    private Matrix transition;
    private Matrix emission;
    private Vector stateDistribution;

    HMM() {
        initializeTranstion();
        initializeEmission();
        initializeDistribution();
    }

    private HMM(Matrix transitionMatrix, Matrix emissionMatrix, Vector distributionVector) {

        transition = transitionMatrix;
        emission = emissionMatrix;
        stateDistribution = distributionVector;
    }

    double estimateBird(Bird bird){

        Integer[] observations = observe(bird);

        APass apass = new APass();
        apass.run(transition, emission, stateDistribution, observations);

        Vector alphaT = new Vector(apass.alpha.getRow(apass.alpha.len() - 1));

        return alphaT.sum();
    }

    double trainModel(Bird bird){

        Integer[] observations = observe(bird);

        return new BaumWelch().run(transition, emission, stateDistribution, observations);
    }

    void trainModel(Integer[] observations){

        BaumWelch baumWelch = new BaumWelch();
        baumWelch.run(transition, emission, stateDistribution, observations);
    }

    Vector predictNextEmissionDistribution(Bird bird) {

        Queue<Integer> obs = new LinkedList<>();
        Collections.addAll(obs,  observe(bird));

        Viterbi viterbi = new Viterbi();
        viterbi.run(transition, emission, stateDistribution, obs);

        int currentStateIndex = viterbi.indices.get(viterbi.indices.size() - 1);

        double[] currDistribution = new double[stateDistribution.array.length];
        currDistribution[currentStateIndex] = 1.0;

        Vector transitionDistribution = transition.multiply(new Vector(currDistribution));

        return emission.multiply(transitionDistribution);
    }

     private Integer[] observe(Bird bird) {

        List<Integer> observationsList = new ArrayList<>();

        for (int i = 0; i < bird.getSeqLength(); i++) {

            if (bird.wasAlive(i)) {
                observationsList.add(bird.getObservation(i));
            }
        }

        Integer[] observations = new Integer[observationsList.size()];

        for (int j = 0; j < observations.length; j++) {
            observations[j] = observationsList.get(j);
        }

        return observations;
    }

    private void initializeTranstion(){

        Random random = new Random();

        transition = new Matrix(STATES, STATES);

        for (int i = 0; i < STATES; ++i) {

            for (int j = 0; j < STATES; ++j) {
                transition.matrix[i][j] = random.nextDouble();
            }
        }

        transition.scale();
    }

    private void initializeEmission(){

        Random random = new Random();

        emission = new Matrix(STATES, EMISSIONS);

        for (int i = 0; i < STATES; ++i) {

            for (int j = 0; j < EMISSIONS; ++j) {
                emission.matrix[i][j] = random.nextDouble();
            }
        }

        emission.scale();
    }

    private void initializeDistribution(){

        Random random = new Random();

        stateDistribution = new Vector(STATES);

        for (int i = 0; i < stateDistribution.len(); i++) {
            stateDistribution.array[i] = random.nextDouble();
        }

        stateDistribution.scale();
    }

    static HMM merge(List<HMM> hmmsToMerge) {

        Matrix transitionMatrix = mergeTransitionMatrix(hmmsToMerge);
        Matrix emissionMatrix = mergeEmissionMatrix(hmmsToMerge);
        Vector distributionVector = mergeDistributionVector(hmmsToMerge);

        return new HMM(transitionMatrix, emissionMatrix, distributionVector);
    }

    private static Matrix mergeTransitionMatrix(List<HMM> hmmsToMerge){

        Matrix transitionMatrix = new Matrix(STATES, STATES);

        for (int i = 0; i < STATES; ++i) {

            for (int j = 0; j < STATES; ++j) {

                double res = 0;

                for (HMM aHmmsToMerge : hmmsToMerge) {
                    res += aHmmsToMerge.transition.get(i, j);
                }

                transitionMatrix.matrix[i][j] = res / hmmsToMerge.size();
            }
        }

        return transitionMatrix;
    }

    private static Matrix mergeEmissionMatrix(List<HMM> hmmsToMerge){

        Matrix emissionMatrix = new Matrix(STATES, EMISSIONS);

        for (int i = 0; i < STATES; ++i) {

            for (int j = 0; j < EMISSIONS; ++j) {

                double res = 0;

                for (HMM aHmmsToMerge : hmmsToMerge) {
                    res += aHmmsToMerge.emission.get(i, j);
                }

                emissionMatrix.matrix[i][j] = res / hmmsToMerge.size();
            }
        }

        return emissionMatrix;
    }

    private static Vector mergeDistributionVector(List<HMM> hmmsToMerge){

        Vector distributionVevtor = new Vector(STATES);

        for (int i = 0; i < STATES; ++i) {

            double res = 0;

            for (HMM aHmmsToMerge : hmmsToMerge) {
                res += aHmmsToMerge.stateDistribution.get(i);
            }

            distributionVevtor.array[i] = res / hmmsToMerge.size();
        }

        return distributionVevtor;
    }
}