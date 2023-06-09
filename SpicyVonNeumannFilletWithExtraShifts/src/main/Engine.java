package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import components.Instruction;
import components.Memory;
import components.ProgramCounter;
import components.Register;

public class Engine {
    private Register[] registers;
    private ProgramCounter PC;
    private Memory memory;
    private int numberOfInstructions;

    public Engine(int numberOfInstructions) {
        registers = new Register[32];
        for (int i = 0; i < 32; i++) {
            registers[i] = new Register("R" + i);
        }
        PC = ProgramCounter.getInstance();
        memory = Memory.getInstance();

        this.numberOfInstructions = numberOfInstructions;
    }

    public void loadAssembly(String pathname) throws NumberFormatException, IOException {

        // create file with given pathname
        File file = new File(pathname);

        // read file and change each line to int
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        String format = "";
        int address = 0;

        while ((line = br.readLine()) != null && line.length() > 0) {
            int instructionInt = 0;
            boolean isShift = false;
            // process the line.
            String[] instructionStringSplit = line.split(" ");
            if (instructionStringSplit[0].equals("ADD")) {
                instructionInt = instructionInt | 0b00000000000000000000000000000000;
                format = "R";
            } else if (instructionStringSplit[0].equals("SUB")) {
                instructionInt = instructionInt | 0b00010000000000000000000000000000;
                format = "R";
            } else if (instructionStringSplit[0].equals("MULI")) {
                instructionInt = instructionInt | 0b00100000000000000000000000000000;
                format = "I";
            } else if (instructionStringSplit[0].equals("ADDI")) {
                instructionInt = instructionInt | 0b00110000000000000000000000000000;
                format = "I";
            } else if (instructionStringSplit[0].equals("BNE")) {
                instructionInt = instructionInt | 0b01000000000000000000000000000000;
                format = "I";
            } else if (instructionStringSplit[0].equals("ANDI")) {
                instructionInt = instructionInt | 0b01010000000000000000000000000000;
                format = "I";
            } else if (instructionStringSplit[0].equals("ORI")) {
                instructionInt = instructionInt | 0b01100000000000000000000000000000;
                format = "I";
            } else if (instructionStringSplit[0].equals("J")) {
                instructionInt = instructionInt | 0b01110000000000000000000000000000;
                format = "J";
            } else if (instructionStringSplit[0].equals("SLL")) {
                instructionInt = instructionInt | 0b10000000000000000000000000000000;
                instructionInt = instructionInt |
                        shiftBinary(Integer.parseInt(instructionStringSplit[3]), 13);

                format = "R";
                isShift = true;
            } else if (instructionStringSplit[0].equals("SRL")) {
                instructionInt = instructionInt | 0b10010000000000000000000000000000;
                instructionInt = instructionInt |
                        shiftBinary(Integer.parseInt(instructionStringSplit[3]), 13);
                format = "R";
                isShift = true;
            } else if (instructionStringSplit[0].equals("LW")) {
                instructionInt = instructionInt | 0b10100000000000000000000000000000;
                format = "I";
            } else if (instructionStringSplit[0].equals("SW")) {
                instructionInt = instructionInt | 0b10110000000000000000000000000000;
                format = "I";
            }

            if (format.equals("R")) {
                int register = Integer.parseInt(instructionStringSplit[1].substring(1));
                instructionInt = instructionInt | shiftBinary(register, 23);

                register = Integer.parseInt(instructionStringSplit[2].substring(1));
                instructionInt = instructionInt | shiftBinary(register, 18);

                if (!isShift) {
                    register = Integer.parseInt(instructionStringSplit[3].substring(1));
                    instructionInt = instructionInt | shiftBinary(register, 13);
                }

            } else if (format.equals("I")) {
                int register = Integer.parseInt(instructionStringSplit[1].substring(1));
                instructionInt = instructionInt | shiftBinary(register, 23);

                register = Integer.parseInt(instructionStringSplit[2].substring(1));
                instructionInt = instructionInt | shiftBinary(register, 18);
                instructionInt = instructionInt | shiftBinary(Integer.parseInt(instructionStringSplit[3]), 0);

            } else if (format.equals("J")) {
                instructionInt = instructionInt | shiftBinary(Integer.parseInt(instructionStringSplit[1]), 0);
            }

            memory.write(address, instructionInt);
            address++;
        }
    }

    /// convert int to binary
    private static int shiftBinary(int value, int shiftAmount) {

        if (value < 0)
            value = (int) (Math.pow(2, 32) + value);

        return value << shiftAmount;

    }

    // total no of cycles: 7 + ((n − 1) ∗ 2) n: no. of instructions
    public void run() {
        int totalCycles = 7 + ((numberOfInstructions - 1) * 2);
        int instructionFetch = 0;
        Instruction instructionDecode = new Instruction();
        int result = 0;
        Instruction instructionDecodeAfterExecuting = new Instruction();
        boolean branch = false;
        for (int cycle = 1; cycle <= totalCycles; cycle++) {
            System.out.println("Cycle " + cycle);
            System.out.println("PC: " + PC.getData());

            // Write back
            if (cycle > 6 && cycle % 2 == 1) {
                writeback(instructionDecodeAfterExecuting, result);
                System.out.println("Write back stage: Instruction " + ((cycle - 5) / 2));
                System.out.println("Input: " + result);
                System.out.println("Output: " + instructionDecodeAfterExecuting.getR1().getData());
                // we should drop all other stages other than fetch and write back if the
                // instruction executed is a branch or a jump
                if (instructionDecodeAfterExecuting.getOpcode() == 7
                        || instructionDecodeAfterExecuting.getOpcode() == 4) {
                    branch = true;
                }
            }
            // Memory
            if (cycle > 5 && cycle % 2 == 0) {

                System.out.println("Memory stage: Instruction " + ((cycle - 4) / 2));
                System.out.println("Input: " + instructionDecodeAfterExecuting.dumpString());

                memoryInstruction(instructionDecodeAfterExecuting);
                if (instructionDecodeAfterExecuting.getOpcode() == 10)
                    System.out.println("Output: " + instructionDecodeAfterExecuting.getR1().getData());
                else
                    System.out.println("No output");
            }
            // Execute number 1
            if (cycle > 3 && cycle < (numberOfInstructions * 2) + 3 && cycle % 2 == 0 && !branch) {
                System.out.println("Execute stage: Instruction " + (int) Math.ceil(((cycle - 3) / 2)));
                // (cycle-2)/2 is the number of the instruction
                // what to do in the first execute?
                // divided them into 2 executes as suggested in the remarks sent on the whatsapp
                // group (piazzas')
                // what are the inputs and the outputs
            }
            // Execute number 2
            if (cycle > 4 && cycle < (numberOfInstructions * 2) + 4 && cycle % 2 == 1 && !branch) {
                // if(instructionDecode.getOpcode()==7 || instructionDecode.getOpcode()==4){//if
                // the instruction executed is a branch or a jump we must drop the decoding and
                // fetching stages
                // instructionDecode=new Instruction();//not sure if this is the correct way to
                // drop the fetch and decode as they might affect the next cycle's register
                // values leading to unwanted actions
                // instructionFetch=0;
                // } mazonesh bardo el mafrood a drop el hagat de hena wala laa
                System.out.println("Execute stage: Instruction " + (int) Math.ceil(((cycle - 3) / 2)));
                System.out.println("The input for this execute stage is: " + instructionDecode.dumpString());

                result = execute(instructionDecode);

                System.out.println("Output: " + result);
                instructionDecodeAfterExecuting = instructionDecode;
            }
            // Decode number 1
            if (cycle > 1 && cycle < (numberOfInstructions * 2) + 1 && cycle % 2 == 0 && !branch) {
                System.out.println("Decode stage: Instruction " + (cycle / 2));
                // the number of the instruction is (cycle/2)
                // what to do in the first decode?
                // what are the inputs and the outputs
            }
            // Decode number 2
            if (cycle > 2 && cycle < (numberOfInstructions * 2) + 2 && cycle % 2 == 1 && !branch) {
                instructionDecode = decode(instructionFetch);
                System.out.println("Decode stage: Instruction " + (cycle / 2));
                System.out.println("The input for this decode stage is: " + toBinary(instructionFetch));
                System.out.println("The output for this decode stage is: " + instructionDecode.dumpString());
            }
            // Fetch
            if (cycle % 2 == 1 && cycle < numberOfInstructions * 2) {
                instructionFetch = fetch();
                System.out.println("Fetch stage: Instruction " + ((cycle + 1) / 2));
                System.out.println("Then output for this fetch stage is: " + toBinary(instructionFetch));

                branch = false;
            }

        }

        System.out.println("Registers:");
        for (int i = 0; i < 32; i++) {
            System.out.println(registers[i].getName() + ": " + registers[i].getData());
        }
        System.out.println("PC: " + PC.getData());
        System.out.println("Memory:");
        memory.dump();
    }

    public static String toBinary(int value) {
        StringBuilder binary = new StringBuilder();

        for (int i = 31; i >= 0; i--) {
            int bit = (value >> i) & 1;
            binary.append(bit);

            if (i % 4 == 0 && i != 0) {
                binary.append(" ");
            }
        }

        return binary.toString();
    }

    // fetch the decode from memory
    private int fetch() {
        int instructionFetch = memory.read(PC.getData());
        PC.incrementPC();
        return instructionFetch;
    }

    // decode the instruction
    private Instruction decode(int instructionFetch) {
        int opcode = 0; // bits31:28
        int rs = 0; // bits27:24
        int rt = 0; // bit23:20
        int rd = 0; // bits19:16
        int shamt = 0; // bits15:12
        int imm = 0; // bits19:0
        int address = 0; // bits27:0

        // extract the bits
        opcode = (instructionFetch & 0b11110000000000000000000000000000) >> 28;

        if (opcode == -7)
            opcode = 9;
        else if (opcode == -8)
            opcode = 8;

        rs = (instructionFetch & 0b00001111100000000000000000000000) >> 23;
        rt = (instructionFetch & 0b00000000011111000000000000000000) >> 18;
        rd = (instructionFetch & 0b00000000000000111110000000000000) >> 13;
        shamt = (instructionFetch & 0b00000000000000000001111111111111);

        imm = (instructionFetch & 0b00000000000000111111111111111111);
        address = (instructionFetch & 0b00001111111111111111111111111111);

        // create the instruction
        Instruction instructionDecode = null;

        // R-type
        if (opcode == 0 || opcode == 1 || opcode == 8 || opcode == 9) {

            instructionDecode = new Instruction("R", opcode, registers[rs], registers[rt], registers[rd], shamt);

        }
        // I-type
        else if (opcode == 2 || opcode == 3 || opcode == 4 || opcode == 5 || opcode == 6 || opcode == 10
                || opcode == 11) {

            instructionDecode = new Instruction("I", opcode, registers[rs], registers[rt], imm);

        }
        // J-type
        else if (opcode == 7) {

            instructionDecode = new Instruction("J", opcode, address);

        }

        return instructionDecode;

    }

    // execute the instruction
    private int execute(Instruction instructionDecode) {
        return instructionDecode.execute();
    }

    // memory access
    private void memoryInstruction(Instruction instructionDecode) {
        instructionDecode.memoryInstruction();
    }

    // writeback to register
    private void writeback(Instruction instructionDecode, int result) {
        instructionDecode.writeback(result);
    }

    public Register[] getRegisters() {
        return registers;
    }

    public void setRegisters(Register[] registers) {
        this.registers = registers;
    }

    public ProgramCounter getPC() {
        return PC;
    }

    public void setPC(ProgramCounter pC) {
        PC = pC;
    }

    public Memory getMemory() {
        return memory;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public int getNumberOfInstructions() {
        return numberOfInstructions;
    }

    public void setNumberOfInstructions(int numberOfInstructions) {
        this.numberOfInstructions = numberOfInstructions;
    }

}
