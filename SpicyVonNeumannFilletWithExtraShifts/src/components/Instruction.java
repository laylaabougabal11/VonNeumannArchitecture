package components;

public class Instruction {
    private String type;
    private int opcode;
    private Register r1;
    private Register r2;
    private Register r3;
    private int shamt;
    private int immediate;
    private int address;

    Memory memory = Memory.getInstance();
    ProgramCounter programCounter = ProgramCounter.getInstance();

    public Instruction() {
    }

    // create 3 constructors for each type of instruction
    public Instruction(String type, int opcode, Register r1, Register r2, Register r3, int shamt) {
        if (!type.equals("R"))
            System.out.println("Invalid instruction type");
        this.type = type;
        this.opcode = opcode;
        this.r1 = r1;
        this.r2 = r2;
        this.r3 = r3;
        this.shamt = shamt;
    }

    public Instruction(String type, int opcode, Register r1, Register r2, int immediate) {
        if (!type.equals("I"))
            System.out.println("Invalid instruction type");
        this.type = type;
        this.opcode = opcode;
        this.r1 = r1;
        this.r2 = r2;
        this.immediate = immediate;
    }

    public Instruction(String type, int opcode, int address) {
        if (!type.equals("J"))
            System.out.println("Invalid instruction type");
        this.type = type;
        this.opcode = opcode;
        this.address = address;
    }

    public int execute() {
        int result = 0;
        switch (type) {
            case "R":
                switch (opcode) {
                    case 0:
                        result = r2.read() + r3.read();
                        break;
                    case 1:
                        result = r2.read() - r3.read();
                        break;
                    case 8:
                        // shift left
                        result = r2.read() << shamt;
                        break;
                    case 9:
                        // shift right
                        result = r2.read() >>> shamt;
                        break;
                    default:
                        System.out.println("Invalid opcode");
                }
                break;
            case "I":
                switch (opcode) {
                    case 2:
                        result = r2.read() * immediate;
                        break;
                    case 3:
                        result = r2.read() + immediate;
                        break;
                    case 4:
                        if (r1.read() != r2.read())
                            programCounter.write(programCounter.getData() + immediate); // the +1 is already done in the
                                                                                        // fetch method
                        break;
                    case 5:
                        result = r2.read() & immediate;
                        break;
                    case 6:
                        result = r2.read() | immediate;
                        break;
                    default:
                        System.out.println("Invalid opcode");

                }
                break;
            case "J":
                if (opcode == 7)
                    programCounter.write(address);
                else
                    System.out.println("Invalid opcode");

            default:
                System.out.println("Invalid instruction type");

        }
        return result;
    }

    public void memoryInstruction() {
        if (type.equals("I")) {
            switch (opcode) {
                case 10:
                    r1.write(memory.memory[(r1.read() + immediate)]);
                    break;
                case 11:
                    memory.write((r2.read() + immediate), r1.read());
                    break;
                default:
                    System.out.println("Won't execute memory instruction");
            }
        } else
            System.out.println("Won't execute memory instruction");

    }

    public void writeback(int result) {
        if (type.equals("R") || (type.equals("I") && (opcode != 11 || opcode != 4)))
            r1.write(result);
        else
            System.out.println("Invalid instruction type");
    }

    public void dump() {
        switch (type) {
            case "R":
                System.out.println("[" + opcode + "] " + r1.dumpString() + " " + r2.dumpString() + " " + r3.dumpString()
                        + " " + shamt);
                break;
            case "I":
                System.out.println("[" + opcode + "] " + r1.dumpString() + " " + r2.dumpString() + " " + immediate);
                break;
            case "J":
                System.out.println("[" + opcode + "] " + address);
                break;
            default:
                System.out.println("Invalid instruction type");

        }
    }

    public String dumpString() {
        String result = "";
        switch (type) {
            case "R":
                result = "[" + opcode + "] " + r1.dumpString() + " , " + r2.dumpString() + " , " + r3.dumpString() + " "
                        + shamt;
                break;
            case "I":
                result = "[" + opcode + "] " + r1.dumpString() + " , " + r2.dumpString() + " , " + immediate;
                break;
            case "J":
                result = "[" + opcode + "] " + address;
                break;
            default:
                System.out.println("Invalid instruction type");

        }
        return result;
    }

    public String getType() {
        return type;
    }

    public int getOpcode() {
        return opcode;
    }

    public Register getR1() {
        return r1;
    }

    public Register getR2() {
        return r2;
    }

    public Register getR3() {
        if (type.equals("R"))
            return r3;
        else
            System.out.println("Invalid instruction type");
        return null;
    }

    public int getShamt() {
        if (type.equals("R"))
            return shamt;
        else
            System.out.println("Invalid instruction type");
        return 0;
    }

    public int getImmediate() {
        if (type.equals("I"))
            return immediate;
        else
            System.out.println("Invalid instruction type");
        return 0;
    }

    public int getAddress() {
        if (type.equals("J"))
            return address;
        else
            System.out.println("Invalid instruction type");
        return 0;
    }

}
