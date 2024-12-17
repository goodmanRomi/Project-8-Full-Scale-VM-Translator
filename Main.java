import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String inputPath = args[0]; // Assuming a valid input file is provided
        File inputFile = new File(inputPath);

        if (inputFile.isFile()) {
            // If it's a single VM file
            String outputFileName = inputFile.getParent() + "/" + inputFile.getName().replace(".vm", ".asm");
            CodeWrite codeWriter = new CodeWrite(outputFileName);
            codeWriter.setInputFile(inputFile.getAbsolutePath());
            codeWriter.writeTranslation();            
            codeWriter.close();
        } 

        else if (inputFile.isDirectory()) {
            // If it's a directory, output file named <directory_name>.asm
            String outputFileName = inputFile.getAbsolutePath() + "/" + inputFile.getName() + ".asm";
            CodeWrite codeWriter = new CodeWrite(outputFileName);
            File[] vmFiles = inputFile.listFiles((dir, name) -> name.endsWith(".vm")); // Filter VM files
                if (vmFiles != null) {
                    for (File vmFile : vmFiles) {
                        String fileNameWithoutExtension = vmFile.getName().replace(".vm", "");
                        codeWriter.writeComment("code for " + fileNameWithoutExtension);
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

       



//         if (pathFile.isDirectory()) {
//             System.out.println("Processing Directory: " + pathFile.getAbsolutePath());
//             // Create the output file name: <directory_name>.asm
//             File[] vmFiles = pathFile.listFiles((dir, name) -> name.endsWith(".vm"));
//             String outputFileName = pathFile.getAbsolutePath() + "/" + pathFile.getName() + ".asm";
//             pathFile= new File(pathFile.getParent(), outputFileName);
//             CodeWrite codeWriter = new CodeWrite(outputFileName);
            
//             if (vmFiles != null && vmFiles.length > 0) {                
//                 System.out.println("Found " + vmFiles.length + " .vm files.");
//                 for (File vmFile : vmFiles) { {
//                 System.out.println("Processing File: " + vmFile.getAbsolutePath());
//                 System.out.println("Output File: " + outputFileName);    
//                 codeWriter.setInputFile(vmFile.getAbsolutePath()); 
//                 }
                
//             }
//                 codeWriter.close(); 

//             } else  {  // Handle single .vm file
//                 CodeWrite codeWriter = new CodeWrite(inputFile.replace(".vm", ".asm"));
//                 codeWriter.writeTranslation();            
//                 codeWriter.close();
//             }
        
//          }
//     }
// }
