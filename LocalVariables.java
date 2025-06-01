public class LocalVariables {
    private int[] variables;

    public LocalVariables(int size) {
        this.variables = new int[size];
    }

    public void setInt(int index, int value) {
        if (index < 0 || index >= variables.length) {
            throw new ArrayIndexOutOfBoundsException("Local variable index out of bounds: " + index);
        }
        variables[index] = value;
    }

    public int getInt(int index) {
        if (index < 0 || index >= variables.length) {
            throw new ArrayIndexOutOfBoundsException("Local variable index out of bounds: " + index);
        }
        return variables[index];
    }

    public int size() {
        return variables.length;
    }
}
