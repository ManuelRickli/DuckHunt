import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

class Viterbi {

    List<Integer> indices;

    Viterbi(){
        this.indices = new ArrayList<>();
    }

    void run(Matrix transitionMatrix, Matrix emmisionMatrix, Vector stateDistribution, Queue<Integer> observations) {

        Stack<Matrix> deltaMatrices = new Stack<>();
        calculateDeltas(transitionMatrix, emmisionMatrix, stateDistribution, observations, deltaMatrices);
        backtrack(deltaMatrices, indices);
    }

    private void calculateDeltas(Matrix transitionMatrix, Matrix emmisionMatrix, Vector stateDistribution, Queue<Integer> observations, Stack<Matrix> deltaMatrices){

        Vector observation = new Vector(emmisionMatrix.getColumn(observations.poll()));
        Matrix initialDeltaMatrix = initializeDeltaMatrix(stateDistribution, observation);
        deltaMatrices.add(initialDeltaMatrix);
        recursionDeltas(transitionMatrix, emmisionMatrix, observations, deltaMatrices);
    }

    private void recursionDeltas(Matrix transitionMatrix, Matrix emmisionMatrix, Queue<Integer> observations, Stack<Matrix> deltaMatrices){

        if (observations.isEmpty()){
            return;
        }

        Vector observation = new Vector(emmisionMatrix.getColumn(observations.poll()));
        Matrix previousDeltaMatrix = deltaMatrices.peek();
        Matrix deltaMatrix = new Matrix(transitionMatrix.matrix.length, 2);

        for (int i = 0; i < transitionMatrix.matrix.length; i++) {

            deltaMatrix.matrix[i][0] = -1;
            deltaMatrix.matrix[i][1] = 0;

            for (int j = 0; j < transitionMatrix.matrix[i].length; j++) {

                double dt = previousDeltaMatrix.matrix[j][0] * transitionMatrix.matrix[j][i] * observation.array[i];

                if(dt > deltaMatrix.matrix[i][0]){
                    deltaMatrix.matrix[i][0] = dt;
                    deltaMatrix.matrix[i][1] = j;
                }
            }
        }

        deltaMatrices.push(deltaMatrix);

        recursionDeltas(transitionMatrix, emmisionMatrix, observations, deltaMatrices);
    }

    private void backtrack(Stack<Matrix> deltaMatrices, List<Integer> indices){

        Matrix last = deltaMatrices.peek();
        double[] column = last.getColumn(0);
        int indexOfMax = getIndexOfMax(column);
        indices.add(indexOfMax);

        backTrackRecursion(deltaMatrices, indices);

        Collections.reverse(indices);
    }

    private void backTrackRecursion(Stack<Matrix> deltaMatrices, List<Integer> indices) {

        if (deltaMatrices.size() <= 1){
            return;
        }

        int maxIndex = indices.get(indices.size() - 1);

        Matrix deltaMatrix = deltaMatrices.pop();
        double v = deltaMatrix.matrix[maxIndex][1];

        indices.add((int) v);

        backTrackRecursion(deltaMatrices, indices);
    }

    private Matrix initializeDeltaMatrix(Vector stateDistribution, Vector observation){

        Vector multiplty = stateDistribution.multiplty(observation);

        double[][] matrix = new double[multiplty.array.length][2];

        for (int i = 0; i < multiplty.array.length; i++) {
            matrix[i][0] = multiplty.array[i];
        }

        return new Matrix(matrix);
    }

    private int getIndexOfMax(double[] array) {

        double maxValue = Double.MIN_VALUE;
        int maxValueIndex = 0;

        for (int i = 0, n = array.length; i < n; ++i) {

            double value = array[i];

            if (value <= maxValue) {
                continue;
            }

            maxValue = value;
            maxValueIndex = i;
        }

        return maxValueIndex;
    }

    @Override
    public String toString() {

        StringBuilder indicesStr = new StringBuilder();

        for (Integer index : indices) {

            indicesStr.append(index).append(" ");
        }

        return indicesStr.toString();
    }
}
