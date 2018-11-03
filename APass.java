class APass {

    Matrix alpha;

    void run(Matrix transitionMatrix, Matrix emissionMatrix, Vector distributionVector, Integer[] observations, Vector scaleFactors) {

        int N = transitionMatrix.len();
        int T = observations.length;

        alpha = new Matrix(T, N);

        for (int i = 0; i < N; i++) {
            alpha.matrix[0][i] = distributionVector.array[i] * emissionMatrix.get(i, observations[0]);
            scaleFactors.array[0] += alpha.get(0, i);
        }

        alpha.scale(scaleFactors, 0);

        for (int t = 1; t < T; t++) {
            run(transitionMatrix, emissionMatrix, observations, scaleFactors, t);
            alpha.scale(scaleFactors, t);
        }
    }

    void run(Matrix transitionMatrix, Matrix emissionMatrix, Vector distributionVector, Integer[] observations) {

        int N = transitionMatrix.len();
        int T = observations.length;

        alpha = new Matrix(T, N);

        for (int i = 0; i < N; i++) {
            alpha.matrix[0][i] = distributionVector.array[i] * emissionMatrix.get(i, observations[0]);
        }

        for (int t = 1; t < T; t++) {
            run(transitionMatrix, emissionMatrix, observations, t);
        }
    }

    private void run(Matrix transitionMatrix, Matrix emissionMatrix, Integer[] observations, int t) {

        int N = transitionMatrix.len();

        for (int i = 0; i < N; i++) {

            alpha.matrix[t][i] = 0.00000;

            for (int j = 0; j < N; j++) {
                alpha.matrix[t][i] += alpha.get(t - 1, j) * transitionMatrix.get(j, i);
            }

            alpha.matrix[t][i] = alpha.get(t, i) * emissionMatrix.get(i, observations[t]);

        }
    }

    private void run(Matrix transitionMatrix, Matrix emissionMatrix, Integer[] observations, Vector scaleFactors, int t) {

        int N = transitionMatrix.len();

        scaleFactors.array[t] = 0;

        for (int i = 0; i < N; i++) {

            alpha.matrix[t][i] = 0.00000;

            for (int j = 0; j < N; j++) {
                alpha.matrix[t][i] += alpha.get(t - 1, j) * transitionMatrix.get(j, i);
            }

            alpha.matrix[t][i] = alpha.get(t, i) * emissionMatrix.get(i, observations[t]);

            scaleFactors.array[t] += alpha.get(t, i);
        }
    }
}
