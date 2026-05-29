package compiler.backend;

import compiler.ir.TacGenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MipsGenerator {
    private TacGenerator tacGenerator;

    public MipsGenerator(TacGenerator tacGenerator) {
        this.tacGenerator = tacGenerator;
    }

    public void generateMips(String outputFile) throws IOException {
        List<String> tacInstructions = tacGenerator.getTacInstructions();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write(".data\n");
            writer.write("newline: .asciiz \"\\n\"\n");
            writer.write(".text\n");
            writer.write("main:\n");

            for (String instruction : tacInstructions) {
                writer.write(instruction + "\n");
            }

            writer.write("li $v0, 10\n"); // Exit syscall
            writer.write("syscall\n");
        }
    }
}