class Matrix3D {

    double[][][] matrix;

    Matrix3D(int i,int j,int k){
        this.matrix = new double[i][j][k];
    }

    double get(int i, int j, int k){
        return matrix[i][j][k];
    }
}
