class BPass {

    Matrix beta;

    void run(Matrix transitionMatrix, Matrix emissionMatrix, Integer[] observations, Vector scalingFactors) {

        int N = transitionMatrix.len();
        int T = observations.length;

        beta = new Matrix(T, N);

        for (int i = 0; i < N; i++) {
            beta.matrix[T-1][i] = scalingFactors.get(T-1);
        }

        for (int t = T - 2; t >= 0; t--) {
            run(transitionMatrix, emissionMatrix, observations, scalingFactors, t);
        }
    }

    private void run(Matrix transitionMatrix, Matrix emissionMatrix, Integer[] observations, Vector scalingFactors, int t){

        int N = transitionMatrix.len();

        for (int i = 0; i < N; i++) {

            beta.matrix[t][i] = 0.0;

            for (int j = 0; j < N; j++) {
                beta.matrix[t][i] = beta.get(t, i) + transitionMatrix.get(i, j) * emissionMatrix.get(j, observations[t+1]) * beta.get(t+1, j);
            }

            beta.matrix[t][i] = scalingFactors.get(t) * beta.get(t, i);
        }
    }
}