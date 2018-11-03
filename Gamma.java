class Gamma {

    Matrix gamma;
    Matrix3D gammaT;

    void run(Matrix transitionMatrix, Matrix emissionMatrix, Integer[] observations, Matrix alpha, Matrix beta) {

        int N = transitionMatrix.len();
        int T = observations.length;

        gamma = new Matrix(T, N);
        gammaT = new Matrix3D(T, N, N);

        for (int t = 0; t < T - 1; t++) {

            run(transitionMatrix, emissionMatrix, observations, alpha, beta, t);
        }
    }

    private void run(Matrix transitionMatrix, Matrix emissionMatrix, Integer[] observations, Matrix alpha, Matrix beta, int t){

        double denom = 0.0;
        int N = transitionMatrix.len();

        for (int i = 0; i < N; i++) {

            for (int j = 0; j < N; j++) {
                denom += alpha.get(t, i) * transitionMatrix.get(i, j) * emissionMatrix.get(j, observations[t+1]) * beta.get(t+1, j);
            }
        }

        for (int i = 0; i < N; i++) {

            gamma.matrix[t][i] = 0.0;

            for (int j = 0; j < N; j++) {
                if (denom != 0) {
                    gammaT.matrix[t][i][j] = alpha.get(t, i) * transitionMatrix.get(i, j) * emissionMatrix.get(j, observations[t + 1]) * beta.get(t + 1, j) / denom;
                }else{
                    gammaT.matrix[t][i][j] = 1e-16;
                }
                gamma.matrix[t][i] += gammaT.matrix[t][i][j];
            }
        }
    }
}
