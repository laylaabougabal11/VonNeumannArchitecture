package components;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Memory {
    int[] memory;
    private static Memory mem;

    private Memory() {
        memory = new int[2048];
    }

    public static Memory getInstance() {
        if (mem == null) {
            mem = new Memory();
        }
        return mem;
    }

    public int read(int address) {
        return memory[address];
    }

    public void write(int address, int data) {
        if (address < 0 || address > 2047) {
            System.out.println("Memory address out of bounds");
        }

        if (data < -2147483647 || data > 2147483647) {
            System.out.println("Data out of bounds");
        }

        memory[address] = data;
    }

    public void dump() {
        for (int i = 0; i < 2048; i++) {
            if (memory[i] != 0)
                System.out.println("[" + i + "] " + memory[i]);
        }
    }

    public void dump(int start, int end) {
        for (int i = start; i < end; i++) {
            System.out.println("[" + i + "] " + memory[i]);
        }
    }

    public void dump(int start) {
        for (int i = start; i < 2048; i++) {
            System.out.println("[" + i + "] " + memory[i]);
        }
    }

}
