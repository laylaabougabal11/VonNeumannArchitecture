package components;

public class ProgramCounter extends Register {
    private static ProgramCounter PC;

    private ProgramCounter() {
        super("PC");
    }

    public void incrementPC() {
        if (getData() < -2147483647 || getData() > 2147483647)
            System.out.println("Data out of bounds");

        if (getData() > 1023)
            write(0);
        else
            write(getData() + 1);
    }

    public static ProgramCounter getInstance() {
        if (PC == null) {
            PC = new ProgramCounter();
        }
        return PC;
    }

}
