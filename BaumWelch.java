class BaumWelch {

    private APass aPass;
    private BPass bPass;
    private Gamma gamma;

    BaumWelch() {

        this.aPass = new APass();
        this.bPass = new BPass();
        this.gamma = new Gamma();
    }

    double run(Matrix transionMatrix, Matrix emissionMatrix, Vector distributionVector, Integer[] observations) {

        double oldLogProb = Double.MIN_VALUE;
        int maxIters = 10000;

        Vector scaleFactors = new Vector(observations.length);

        for (int iter = 0; iter < maxIters; iter++) {

            aPass.run(transionMatrix, emissionMatrix, distributionVector, observations, scaleFactors);
            bPass.run(transionMatrix, emissionMatrix, observations, scaleFactors);
            gamma.run(transionMatrix, emissionMatrix, observations, aPass.alpha, bPass.beta);

            reEstimate(transionMatrix, emissionMatrix, distributionVector, observations);

            double logProb = computeLog(scaleFactors, observations);

            if (logProb < oldLogProb) {
                return oldLogProb;
            }

            oldLogProb = logProb;
        }

        return oldLogProb;
    }

    private double computeLog(Vector scaleFactors, Integer[] observations) {

        int T = observations.length;

        double logProb = 0.0;

        for (int i = 0; i < T; i++) {
            logProb = logProb + Math.log(scaleFactors.get(i));
        }

        return -logProb;
    }

    private void reEstimate(Matrix transitionMatrix, Matrix emissionMatrix, Vector distributionVector, Integer[] observations) {

        int N = transitionMatrix.len();

        for (int i = 0; i < N; i++) {

            reEstimateDistributionVector(distributionVector, i);
            reEstimateTransitionMatrix(transitionMatrix, observations, i);
            reEstimateEmmisionMatrix(emissionMatrix, observations, i);
        }
    }

    private void reEstimateDistributionVector(Vector distributionVector, int i) {

        distributionVector.array[i] = gamma.gamma.get(0, i);
    }

    private void reEstimateTransitionMatrix(Matrix transitionMatrix, Integer[] observations, int i) {

        int N = transitionMatrix.len();
        int T = observations.length;

        for (int j = 0; j < N; j++) {

            double numer = 0.000000;
            double denom = 0.000000;

            for (int t = 0; t < T - 1; t++) {
                numer += gamma.gammaT.get(t, i, j);
                denom += gamma.gamma.get(t, i);
            }

            transitionMatrix.matrix[i][j] = 0.0;

            if (denom != 0.0) {
                transitionMatrix.matrix[i][j] = numer / denom;
            }

            if (transitionMatrix.get(i, j) == 0.0) {
                transitionMatrix.matrix[i][j] = 1e-16;
            }

            transitionMatrix.scale();
        }
    }

    private void reEstimateEmmisionMatrix(Matrix emissionMatrix, Integer[] observations, int i) {

        int M = emissionMatrix.colLen();
        int T = observations.length;

        for (int j = 0; j < M; j++) {

            double numer = 0.000000;
            double denom = 0.000000;

            for (int t = 0; t < T - 1; t++) {

                if (observations[t] == j) {
                    numer += gamma.gamma.get(t, i);
                }

                denom += gamma.gamma.get(t, i);
            }

            emissionMatrix.matrix[i][j] = 0.0;

            if (denom != 0.0) {
                emissionMatrix.matrix[i][j] = numer / denom;
            }

            if (emissionMatrix.get(i, j) == 0.0) {
                emissionMatrix.matrix[i][j] = 1e-16;
            }
        }

        emissionMatrix.scale();
    }
}
