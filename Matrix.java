import java.util.Arrays;

class Matrix {

    double[][] matrix;

    Matrix(double[][] matrix) {

        this.matrix = matrix;
    }

    Matrix(int x, int y) {

        this.matrix = new double[x][y];
    }

    static Matrix newMatrix(String matrixStr[]) {

        int rows = Integer.parseInt(matrixStr[0]);
        int columns = Integer.parseInt(matrixStr[1]);

        double[][] matrix = new double[rows][columns];

        for (int row = 0; rows > row; row++) {

            int step = row * columns + 2;

            for (int column = 0; column < columns; column++) {

                matrix[row][column] = Double.parseDouble(matrixStr[step + column]);
            }
        }

        return new Matrix(matrix);
    }

    void scale(Vector scaleFactors, int index) {

        double scaleFactor = scaleFactors.get(index);

        if (scaleFactor == 0) {
            return;
        }

        scaleFactor = 1.0 / scaleFactor;
        scaleFactors.array[index] = scaleFactor;

        for (int i = 0; i < matrix[0].length; i++) {
            matrix[index][i] *= scaleFactor;
        }
    }

    void scale() {

        for (int i = 0; i < len(); i++) {

            double sum = Arrays.stream(matrix[i]).sum();

            if (sum == 0) {
                continue;
            }

            for (int j = 0; j < colLen(); j++) {
                matrix[i][j] /= sum;
            }
        }
    }

    double[] getColumn(int columnIndex) {

        double[] column = new double[matrix.length];

        for (int i = 0; i < matrix.length; i++) {

            column[i] = matrix[i][columnIndex];
        }

        return column;
    }

    double[] getRow(int rowIndex) {

        double[] row = new double[colLen()];

        System.arraycopy(matrix[rowIndex], 0, row, 0, colLen());

        return row;
    }

    int len() {

        return matrix.length;
    }

    int colLen() {

        return matrix[0].length;
    }

    double get(int i, int j) {

        return matrix[i][j];
    }

    Vector multiply(Vector vectorToMultiplyWith) {

        double[][] transpose = this.transpose();

        int rows = transpose.length;
        int columns = transpose[0].length;

        double[] multiplication = new double[rows];

        for (int row = 0; row < rows; row++) {

            double sum = 0;

            for (int column = 0; column < columns; column++) {
                sum += transpose[row][column] * vectorToMultiplyWith.array[column];
            }

            multiplication[row] = sum;
        }

        return new Vector(multiplication);
    }

    private double[][] transpose() {

        int m = matrix.length;
        int n = matrix[0].length;

        double[][] traspose = new double[n][m];

        for (int x = 0; x < n; x++) {

            traspose[x] = new double[m];

            for (int y = 0; y < m; y++) {
                traspose[x][y] = matrix[y][x];
            }
        }

        return traspose;
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder(matrix.length + " " + matrix[0].length + " ");

        for (double[] aMatrix : matrix) {
            for (int j = 0; j < matrix[0].length; j++) {
                stringBuilder.append(Utils.round(aMatrix[j])).append(" ");
            }
        }

        return stringBuilder.toString().trim();
    }
}
