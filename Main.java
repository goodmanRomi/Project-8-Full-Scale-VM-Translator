import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String inputPath = args[0]; // Assuming a valid input file is provided
        File inputFile = new File(inputPath);

        if (inputFile.isFile()) {
            // If it's a single VM file
            String outputFileName = inputFile.getParent() + "/" + inputFile.getName().replace(".vm", ".asm");
            CodeWrite codeWriter = new CodeWrite(outputFileName, false);
            codeWriter.setInputFile(inputFile.getAbsolutePath());
            codeWriter.writeTranslation();            
            codeWriter.close();
    
        } 

        else if (inputFile.isDirectory()) {
            // If it's a directory, output file named <directory_name>.asm
            String outputFileName = inputFile.getAbsolutePath() + "/" + inputFile.getName() + ".asm";
            File[] vmFiles = inputFile.listFiles((dir, name) -> name.endsWith(".vm")); // Filter VM files
            boolean writeBootstrap = (vmFiles.length > 1); //DO boostrap only if i have more than 1 vm file 
            CodeWrite codeWriter = new CodeWrite(outputFileName, writeBootstrap); 
            if (vmFiles != null) {
                    codeWriter.writeBootstrap();
                    for (File vmFile : vmFiles) {
                        String fileNameWithoutExtension = vmFile.getName().replace(".vm", "");
                        codeWriter.setCurrentFileName(fileNameWithoutExtension); // Set current file name
                        codeWriter.writeComment("code for " + fileNameWithoutExtension);
                        // codeWriter.setFileName(fileNameWithoutExtension);
                        codeWriter.setInputFile(vmFile.getAbsolutePath());
                        codeWriter.writeTranslation();   
                    }
                }
                codeWriter.close();

            } else {
            System.out.println("Invalid input. Must be a .vm file or directory.");
            }
        }
    }