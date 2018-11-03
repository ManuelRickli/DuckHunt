import java.util.Arrays;

class Vector {

    double[] array;

    Vector(double[] array) {

        this.array = array;
    }

    Vector(int len) {

        this.array = new double[len];
    }

    Vector(int len, double value) {

        this.array = new double[len];

        for (int i = 0; i < this.array.length; i++) {
            this.array[i] = value;
        }
    }

    static Vector newVector(String vectorStr[]) {

        double[] array = new double[Integer.parseInt(vectorStr[1])];

        for (int i = 0; i < array.length; i++) {

            array[i] = Double.parseDouble(vectorStr[i + 2]);
        }

        return new Vector(array);
    }

    Vector multiplty(final Vector vectorToMultiplyWith) {

        double[] result = new double[array.length];

        for (int i = 0; i < array.length; i++) {

            result[i] = array[i] * vectorToMultiplyWith.array[i];
        }

        return new Vector(result);
    }

    double sum() {

        return Arrays.stream(array).sum();
    }

    int len() {

        return array.length;
    }

    void scale() {

        double sum = Arrays.stream(array).sum();

        if (sum == 0.0) {
            return;
        }

        for (int i = 0; i < array.length; i++) {
            array[i] /= sum;
        }
    }

    int getMaxIndex() {

        int maxValueIndex = 0;
        double maxValue = Double.MIN_VALUE;

        for (int i = 0; i < array.length; i++) {

            double value = array[i];

            if (value <= maxValue) {
                continue;
            }

            maxValue = value;
            maxValueIndex = i;
        }

        return maxValueIndex;
    }

    double get(int i) {

        return array[i];
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder("1" + " " + array.length + " ");

        for (double element : array) {
            stringBuilder.append(element).append(" ");
        }

        return stringBuilder.toString().trim();
    }
}
