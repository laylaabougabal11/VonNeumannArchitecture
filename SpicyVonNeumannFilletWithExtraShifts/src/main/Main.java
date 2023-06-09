package main;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws NumberFormatException, IOException {
        Engine engine = new Engine(7);

        engine.loadAssembly(System.getProperty("user.dir") + File.separator + "data" + File.separator
                + "assembly.txt");

        engine.getRegisters()[2].write(10);
        engine.getRegisters()[3].write(5);

        engine.getRegisters()[31].write(20);

        engine.getRegisters()[5].write(25);

        engine.getRegisters()[5].write(25);

        engine.run();
    }
}
