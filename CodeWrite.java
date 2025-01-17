import java.io.*;
import java.util.Set;

public class CodeWrite{

private String inputFile;
private String outputFile;
private BufferedWriter writer;
private static int labelCounter = 0; // Static counter for unique labels
private boolean writeBootstrap;      // Flag to indicate whether to write bootstrap code
private String returnAddressLabel;
private String currentFileName;


    public CodeWrite(String outputFile, boolean writeBootstrap) throws IOException{
        this.outputFile = outputFile;
        this.writer = new BufferedWriter(new FileWriter(outputFile));
        this.writeBootstrap = writeBootstrap; // Initialize the flag
    }
    
    public void setCurrentFileName(String fileName) {
        // Strip the .vm extension and store the name
        this.currentFileName = fileName.replace(".vm", "");
    }
    
    public void writeComment(String comment) throws IOException {
        writer.write("// " + comment);
        writer.newLine();
    }

    private String generateUniqueLabel(String baseName) {
        return baseName + "." + labelCounter++;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }


    public void writeBootstrap() throws IOException {
        if (!writeBootstrap) return; // Skip bootstrap code if flag is false
        String sysInitLabel = generateUniqueLabel("SysInitLable");
        // Initialize SP to 256
        writer.write("// Bootstrap code\n");
        writer.write("@256\n");
        writer.write("D=A\n");
        writer.write("@SP\n");
        writer.write("M=D\n");
    // by convention, the Sys.init function is called "automatically" by the bootstrap code generated by the VM translator.
    // Call Sys.init - so i do all the things that calling a function does ! 
       writer.write("//pushing return address onto the stack\n");
        writer.write("@"+sysInitLabel+"\n");
        writer.write("D=A\n" +
                        "@SP\n" + 
                        "A=M\n" + 
                        "M=D\n" + 
                        "@SP\n" + 
                        "M=M+1\n");
        writer.write("//Push Saved State Registers (LCL, ARG, THIS, THAT)\n");
        writer.write("@LCL\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1\n" +
                        "@ARG\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1\n" +
                        "@THIS\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1\n" +
                        "@THAT\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1\n");

        writer.write("//Reposition the ARG Register\n");
        writer.write("@SP\n" +
                        "D=M\n" +
                        "@5\n" +
                        "D=D-A\n" +
                        "@ARG\n" +
                        "M=D\n");
        
        writer.write("//Set the LCL Register\n");
        writer.write("@SP\n" +
                    "D=M\n" +
                    "@LCL\n" +
                    "M=D\n");

        writer.write("//Jump to Sys.init\n");
        writer.write("@Sys.init\n");
        writer.write("0;JMP\n");
        writer.write("//Define Return Address Label\n");
        writer.write("("+sysInitLabel+")\n");
    }

    public void writeTranslation() throws IOException{
    Parser parser = new Parser(inputFile);
    
    while (parser.hasMoreCommands()){
        parser.advance();
        String commandType = parser.commandType();
        String asmCode=" ";
        if (commandType.equals(Parser.C_ARITHMETIC)){
                switch (parser.arg1()) {
                case Parser.ADD:
                asmCode+=" ///"+Parser.ADD+" command\n";
                asmCode+=" @SP\n";
                asmCode+=" AM=M-1\n"; //my SP points to the next availbe spot and i need to decrement it by 1 to get to actuall value
                asmCode+=" D=M\n";
                asmCode+=" @SP\n";
                asmCode+=" AM=M-1\n";
                asmCode+=" M=D+M\n";
                asmCode += "@SP\n"; //added
                asmCode += "M=M+1\n"; //added increment pointer
                break;

                case Parser.SUB:
                asmCode+=" ////"+Parser.SUB+" command\n";
                asmCode+=" @SP\n";
                asmCode+=" AM=M-1\n"; 
                asmCode+=" D=M\n";
                asmCode+=" @SP\n";
                asmCode+=" AM=M-1\n";
                asmCode+=" M=M-D\n";
                asmCode += "@SP\n"; //added
                asmCode += "M=M+1\n"; //added - increment pointer
                break;

                case Parser.NEG:
                asmCode+=" ////"+Parser.NEG+" command\n";
                asmCode+=" @SP\n";
                asmCode+=" AM=M-1\n"; 
                asmCode+=" M=-M\n";
                asmCode += "@SP\n"; //added
                asmCode += "M=M+1\n"; //added, increment pointer
                    break;

                case Parser.EQ: // RETURNS -1 if true, 0 if false
                int eqLabel = labelCounter++;
                asmCode += "////"+Parser.EQ+ " command\n";
                asmCode += "@SP\n";
                asmCode += "AM=M-1\n";
                asmCode += "D=M\n";
                asmCode += "@SP\n";
                asmCode += "AM=M-1\n";
                asmCode += "D=M-D\n";
                asmCode += "@EQ_TRUE"+ eqLabel + "\n";      // If D == 0 (i.e., the values are equal), jump to EQ_TRUE label
                asmCode += "D;JEQ\n";         // Jump if equal
                asmCode += "@SP\n";           // We get to here if not equal (else)
                asmCode += "A=M\n";
                asmCode += "M=0\n";           // Set result to 0 (false) // added
                asmCode += "@EQ_END"+ eqLabel +"\n";       // added
                asmCode += "0;JMP\n";         // Jump to EQ_END to skip the true case // added
                asmCode += "(EQ_TRUE"+ eqLabel + ")\n";     // added
                asmCode += "@SP\n";           // added
                asmCode += "A=M\n";           // Set result to -1 (true) // added
                asmCode += "M=-1\n";          // added
                asmCode += "(EQ_END"+ eqLabel + ")\n";      // added
                asmCode += "@SP\n";           // added
                asmCode += "M=M+1\n";         // Increment SP to point to the next available slot // added
                break;
                
                case Parser.GT: // RETURNS -1 if true, 0 if false
                int gtLabel = labelCounter++;
                asmCode += "// gt command\n";
                asmCode += "@SP\n";
                asmCode += "AM=M-1\n";
                asmCode += "D=M\n";
                asmCode += "@SP\n";
                asmCode += "AM=M-1\n";
                asmCode += "D=M-D\n";        // Subtract to compare the two values // added
                asmCode += "@GT_TRUE"+ gtLabel +"\n";     // If D > 0, jump to GT_TRUE label
                asmCode += "D;JGT\n";        // Jump if greater than
                asmCode += "@SP\n";          // We get here if the comparison is false (else) // added
                asmCode += "A=M\n";          // added
                asmCode += "M=0\n";          // Set result to 0 (false) // added
                asmCode += "@GT_END"+ gtLabel + "\n";      // added
                asmCode += "0;JMP\n";        // Jump to GT_END to skip the true case // added
                asmCode += "(GT_TRUE" + gtLabel + ")\n";    // Label for the true case // added
                asmCode += "@SP\n";          // added
                asmCode += "A=M\n";          // added
                asmCode += "M=-1\n";         // Set result to -1 (true) // added
                asmCode += "(GT_END" + gtLabel + ")\n";    // Label for the end of the comparison // added
                asmCode += "@SP\n";          // added
                asmCode += "M=M+1\n";        // Increment SP to point to the next available slot // added
                break;

                case Parser.LT:
                int ltLabel = labelCounter++;
                asmCode += "/// lt command\n";
                asmCode += "@SP\n";
                asmCode += "AM=M-1\n";
                asmCode += "D=M\n";
                asmCode += "@SP\n";
                asmCode += "AM=M-1\n";
                asmCode += "D=M-D\n";        // Subtract to compare the two values // added
                asmCode += "@LT_TRUE" + ltLabel + "\n";    // If D < 0, jump to LT_TRUE label
                asmCode += "D;JLT\n";        // Jump if less than
                asmCode += "@SP\n";          // We get here if the comparison is false (else) // added
                asmCode += "A=M\n";          // added
                asmCode += "M=0\n";          // Set result to 0 (false) // added
                asmCode += "@LT_END" + ltLabel + "\n";      // added
                asmCode += "0;JMP\n";        // Jump to LT_END to skip the true case // added
                asmCode += "(LT_TRUE" + ltLabel + ")\n";    // Label for the true case // added
                asmCode += "@SP\n";          // added
                asmCode += "A=M\n";          // added
                asmCode += "M=-1\n";         // Set result to -1 (true) // added
                asmCode += "(LT_END" + ltLabel + ")\n";     // Label for the end of the comparison // added
                asmCode += "@SP\n";          // added
                asmCode += "M=M+1\n";        // Increment SP to point to the next available slot // added
                break;

                case Parser.AND:
                asmCode += "/// and command\n";
                asmCode += "@SP\n";
                asmCode += "AM=M-1\n";       // Decrement SP and load the top value into M
                asmCode += "D=M\n";          // Store the top value in D
                asmCode += "@SP\n";          // added
                asmCode += "AM=M-1\n";       // Decrement SP again and load the next value into M
                asmCode += "M=D&M\n";        // Perform bitwise AND and store the result in M
                asmCode += "@SP\n";          // added
                asmCode += "M=M+1\n";        // Increment SP to point to the next available slot // added
                break;

                case Parser.OR:
                asmCode += "/// or command\n";
                asmCode += "@SP\n";
                asmCode += "AM=M-1\n";       // Decrement SP and load the top value into M
                asmCode += "D=M\n";          // Store the top value in D
                asmCode += "@SP\n";          // added
                asmCode += "AM=M-1\n";       // Decrement SP again and load the next value into M
                asmCode += "M=D|M\n";        // Perform bitwise OR and store the result in M
                asmCode += "@SP\n";          // added
                asmCode += "M=M+1\n";        // Increment SP to point to the next available slot // added
                break;

                case Parser.NOT:
                asmCode += "/// not command\n";
                asmCode += "@SP\n";
                asmCode += "AM=M-1\n";       // Decrement SP and load the top value into M
                asmCode += "M=!M\n";         // Perform bitwise NOT and store the result in M
                asmCode += "@SP\n";          // added
                asmCode += "M=M+1\n";        // Increment SP to point to the next available slot // added
                break;

                default:
                    break;
            }
            } else {
            
            String segment = parser.arg1();
            int index = parser.arg2();
            asmCode = "";

            if (commandType.equals(Parser.C_PUSH)) {
                switch (segment) {
                    case Parser.CONSTANT: //v
                    asmCode += " //" + Parser.C_PUSH + " " + segment + " " + index + "\n";
                    asmCode += " @" + index + "\n";
                    asmCode += " D=A\n";
                    asmCode += " @SP\n";
                    asmCode += " A=M\n";
                    asmCode += " M=D\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M+1\n";
                    break;

                    case Parser.LOCAL: //v
                    asmCode += " //" + Parser.C_PUSH + " " + segment + " " + index + "\n";
                    asmCode += " @" + index + "\n";
                    asmCode += " D=A\n";
                    asmCode += " @LCL\n";
                    asmCode += " A=D+M\n";
                    asmCode += " D=M\n";
                    asmCode += " @SP\n";
                    asmCode += " A=M\n";
                    asmCode += " M=D\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M+1\n";
                    break;

                    case Parser.ARGUMENT: //v
                    asmCode += " //" + Parser.C_PUSH + " argument " + index + "\n";
                    asmCode += " @" + index + "\n";
                    asmCode += " D=A\n";
                    asmCode += " @ARG\n";
                    asmCode += " A=D+M\n";
                    asmCode += " D=M\n";
                    asmCode += " @SP\n";
                    asmCode += " A=M\n";
                    asmCode += " M=D\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M+1\n";
                    break;

                    case Parser.THIS: //v
                    asmCode += " //" + Parser.C_PUSH + " this " + index + "\n";
                    asmCode += " @" + index + "\n";
                    asmCode += " D=A\n";
                    asmCode += " @THIS\n";
                    asmCode += " A=D+M\n";
                    asmCode += " D=M\n";
                    asmCode += " @SP\n";
                    asmCode += " A=M\n";
                    asmCode += " M=D\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M+1\n";
                    break;

                    case Parser.THAT: //v
                    asmCode += " //" + Parser.C_PUSH + " that " + index + "\n";
                    asmCode += " @" + index + "\n";
                    asmCode += " D=A\n";
                    asmCode += " @THAT\n";
                    asmCode += " A=D+M\n";
                    asmCode += " D=M\n";
                    asmCode += " @SP\n";
                    asmCode += " A=M\n";
                    asmCode += " M=D\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M+1\n";
                    break;

                    case Parser.POINTER: //v
                    asmCode += " //" + Parser.C_PUSH + " pointer " + index + "\n";
                    asmCode += (index == 0) ? " @THIS\n" : " @THAT\n";
                    asmCode += " D=M\n";
                    asmCode += " @SP\n";
                    asmCode += " A=M\n";
                    asmCode += " M=D\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M+1\n";
                    break;

                    case Parser.TEMP: //v
                    asmCode += " //" + Parser.C_PUSH + " temp " + index + "\n";
                    asmCode += " @" + (5+index) + "\n";
                    asmCode += " D=M\n";
                    asmCode += " @SP\n";
                    asmCode += " A=M\n";
                    asmCode += " M=D\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M+1\n";
                    break;

                    case Parser.STATIC://Each .vm file has its own "namespace" for static variables, which ensures no conflicts between files
                    
                    asmCode += " //" + Parser.C_PUSH + " static " + index + "\n";
                    asmCode += " @Static." + currentFileName + "."+index + "\n";
                    asmCode += " D=M\n";
                    asmCode += " @SP\n";
                    asmCode += " A=M\n";
                    asmCode += " M=D\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M+1\n";
                    break;
                    }

                } else if (commandType.equals(Parser.C_POP)) { 
                    switch (segment) {
                    case Parser.LOCAL: //v
                    asmCode += " //" + Parser.C_POP + " local " + index + "\n";
                    asmCode += "@" + index + "\n";   // Load the position
                    asmCode += "D=A\n";                 // D = position
                    asmCode += "@LCL\n";                // Load base address of local segment
                    asmCode += "D=D+M\n";               // D = LCL + position
                    asmCode += "@R12\n";                // Use R12 as a temporary storage
                    asmCode += "M=D\n";                 // R12 = LCL + position
                    asmCode += "@SP\n";                 // Access the stack pointer
                    asmCode += "M=M-1\n";               // Decrement SP
                    asmCode += "A=M\n";                 // Access the top of the stack
                    asmCode += "D=M\n";                 // D = *SP
                    asmCode += "@R12\n";                // Load the address in R12
                    asmCode += "A=M\n";                 // Go to the calculated address (LCL + position)
                    asmCode += "M=D\n";                 // Store the popped value in the calculated address
                    break;
                    
                    case Parser.ARGUMENT: //v
                    asmCode += " ////" + Parser.C_POP + " argument " + index + "\n";
                    asmCode += " @" + index + "\n"; //load the position 
                    asmCode += " D=A\n";        //D=position
                    asmCode += " @ARG\n";       //Load base adress of the argument segment
                    asmCode += " D=D+M\n";      //D=ARG+position
                    asmCode += " @R13\n";       //use R13 as temporary storage
                    asmCode += " M=D\n";        //R13=ARG+Position
                    asmCode += " @SP\n";        //Access the stack pointer
                    asmCode += " AM=M-1\n";      //decrement stack pointer
                    asmCode += " D=M\n";         // Access the top of the stack   
                    asmCode += " @R13\n";       // Load the address in R13
                    asmCode += " A=M\n";       // Go to the calculated address (ARG + position)
                    asmCode += " M=D\n";      // Store the popped value in the calculated address

                    break;

                    case Parser.THIS: //v
                    asmCode += " //" + Parser.C_POP + " this " + index + "\n";
                    asmCode += " @" + index + "\n";
                    asmCode += " D=A\n";
                    asmCode += " @THIS\n";
                    asmCode += " D=D+M\n";
                    asmCode += " @R13\n";
                    asmCode += " M=D\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M-1\n";
                    asmCode += " A=M\n";
                    asmCode += " D=M\n";
                    asmCode += " @R13\n";
                    asmCode += " A=M\n";
                    asmCode += " M=D\n";
                    break;

                    case Parser.THAT: //v
                    asmCode += " //" + Parser.C_POP + " that " + index + "\n";
                    asmCode += " @" + index + "\n";
                    asmCode += " D=A\n";
                    asmCode += " @THAT\n";
                    asmCode += " D=D+M\n";
                    asmCode += " @R13\n";
                    asmCode += " M=D\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M-1\n";
                    asmCode += " A=M\n";
                    asmCode += " D=M\n";
                    asmCode += " @R13\n";
                    asmCode += " A=M\n";
                    asmCode += " M=D\n";
                    break;

                    case Parser.POINTER: //v
                    asmCode += " //" + Parser.C_POP + " pointer " + index + "\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M-1\n";
                    asmCode += " A=M\n";
                    asmCode += " D=M\n";
                    asmCode += (index == 0) ? " @THIS\n" : " @THAT\n";
                    asmCode += " M=D\n";
                    break;

                    case Parser.TEMP: //v
                    asmCode += " //" + Parser.C_POP + " temp " + index + "\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M-1\n";
                    asmCode += " A=M\n";
                    asmCode += " D=M\n";
                    asmCode += " @" + (5+index) + "\n";
                    asmCode += " M=D\n";
                    break;

                    case Parser.STATIC: //v
                    asmCode += " //" + Parser.C_POP + " static " + index + "\n";
                    asmCode += " @SP\n";
                    asmCode += " M=M-1\n";
                    asmCode += " A=M\n";
                    asmCode += " D=M\n";
                    asmCode += " @Static." + currentFileName +"."+index + "\n";
                    asmCode += " M=D\n";
                    break;
            }
            
            //caller
       
        } else if (commandType.equals(Parser.C_FUNCTION)){  //function FunctionName nVars, Create a label for the function, Initialize the local variables to 0.
            String initLocalsLable ="INIT_LOCALS."+(labelCounter++);         
            String endInitLable ="END_INIT."+(labelCounter++);         

            asmCode += " //this vm command is a function declaration for fucntion " + segment + "\n";
                    asmCode +=" (" +segment+ ")\n";       // Function entry point label
                    //asm code that initiallizes the local variables according to the int proivded to 0s
                    asmCode +=" @"+index+"\n"; //placeholder representing the number of local variables you want to initialize
                    asmCode +=" D=A\n"; // used the @index to get literallly the number of arguments i will be needing to initiallize
                    asmCode +=" @R13\n";//using it as a temporary register to be the counter for the loop
                    asmCode +=" M=D\n";//store the number of variables i will be needing in temporary @R13
                    asmCode +="("+initLocalsLable+")\n"; //start of the loop for initializtion 
                    asmCode +=" @R13\n ";
                    asmCode +=" D=M\n ";
                    asmCode += "@"+endInitLable+"\n";
                    asmCode += " D;JEQ\n";
                    asmCode +=" @0\n";
                    asmCode +=" D=A\n";
                    asmCode +=" @SP\n";
                    asmCode +=" A=M\n";
                    asmCode +=" M=D\n";
                    asmCode +=" @SP\n";
                    asmCode +=" M=M+1\n"; // using register 0 to get 0 as data to put into SP, change SP to point to 0 and also to store 0 there, and increment it
                    asmCode +=" @R13\n";
                    asmCode +=" M=M-1\n";
                    asmCode += " @"+initLocalsLable+"\n";
                    asmCode += " 0;JMP\n"; //decreament counter for loop, set stop for loop, if my R13 is not yet equals 0, i will jump to start again
                    asmCode +=" ("+endInitLable+")\n"; //jump here once we finished the loop to initiate all
                    
        } else if (commandType.equals(Parser.C_LABEL)){
                    asmCode += " //" + Parser.C_LABEL + "\n";
                    asmCode += "("+segment+")\n"; // Add the label code to the assembly output


        } else if (commandType.equals(Parser.C_GOTO)){
                    asmCode += " //" + Parser.C_GOTO + "\n";
                    asmCode +=" @" + segment + "\n";
                    asmCode +=" 0;JMP\n";

            
        } else if (commandType.equals(Parser.C_IF)){ //must write code that pushes a boolean expression onto the stack before the IF expression
                    asmCode += " //" + Parser.C_IF + "\n";  //if i have true at the top of my stack, jump to execute the command just after the label
                    asmCode += "@SP\n";
                    asmCode += "AM=M-1\n";    // Decrement SP and point to the top of the stack
                    asmCode += "D=M\n";       // D = *SP
                    asmCode += "@" + segment + "\n"; //Set the target label to jump to.
                    asmCode += "D;JNE\n";     // Jump if D != 0

        } else if (commandType.equals(Parser.C_CALL)){ //Save the caller's state (return address, frame pointers), Jump to the function being called.
                
                String returnAddressLabel = generateUniqueLabel("returnLable");

                asmCode += "// Calling function " + segment + " with " + index + " arguments\n";
                    // 1. Push the return address onto the stack
                    asmCode += "@" + returnAddressLabel + "\n";
                    asmCode += "D=A\n";               // D = return address
                    asmCode += "@SP\n";
                    asmCode += "A=M\n";               // A = *SP
                    asmCode += "M=D\n";               // *SP = return address
                    asmCode += "@SP\n";
                    asmCode += "M=M+1\n";             // Increment SP
                    
                    // 2. Push the LCL (frame pointer) onto the stack
                    asmCode += "@LCL\n";
                    asmCode += "D=M\n";               // D = LCL
                    asmCode += "@SP\n";
                    asmCode += "A=M\n";               // A = *SP
                    asmCode += "M=D\n";               // *SP = LCL
                    asmCode += "@SP\n";
                    asmCode += "M=M+1\n";             // Increment SP

                    // 3. Push the ARG (argument pointer) onto the stack
                    asmCode += "@ARG\n";
                    asmCode += "D=M\n";               // D = ARG
                    asmCode += "@SP\n";
                    asmCode += "A=M\n";               // A = *SP
                    asmCode += "M=D\n";               // *SP = ARG
                    asmCode += "@SP\n";
                    asmCode += "M=M+1\n";             // Increment SP

                    // 4. Push the THIS (this pointer) onto the stack
                    asmCode += "@THIS\n";
                    asmCode += "D=M\n";               // D = THIS
                    asmCode += "@SP\n";
                    asmCode += "A=M\n";               // A = *SP
                    asmCode += "M=D\n";               // *SP = THIS
                    asmCode += "@SP\n";
                    asmCode += "M=M+1\n";             // Increment SP

                    // 5. Push the THAT (that pointer) onto the stack
                    asmCode += "@THAT\n";
                    asmCode += "D=M\n";               // D = THAT
                    asmCode += "@SP\n";
                    asmCode += "A=M\n";               // A = *SP
                    asmCode += "M=D\n";               // *SP = THAT
                    asmCode += "@SP\n";
                    asmCode += "M=M+1\n";             // Increment SP

               // 6. Adjust the ARG pointer to account for the arguments being pushed (beginning of nArgs)
                    asmCode += "@SP\n";
                    asmCode += "D=M\n";
                    asmCode += "@" + (5 + index) + "\n";  // Load (nArgs + 5) into D
                    asmCode += "D=D-A\n";
                    asmCode += "@ARG\n";
                    asmCode += "M=D\n";                  // ARG = SP - (nArgs + 5)

                    // 7. Adjust the LCL pointer to point to the current frame
                    asmCode += "@SP\n";
                    asmCode += "D=M\n";               // D = SP (top of stack after all pushes)
                    asmCode += "@LCL\n";
                    asmCode += "M=D\n";               // Set LCL to the current SP

                    // 8. Jump to the function
                    asmCode += "@" + segment + "\n";
                    asmCode += "0;JMP\n";             // Jump to the function

                    // 9. Label for the return address
                    asmCode += "(" + returnAddressLabel + ")\n";  // Return address label
                    
               
        } else if (commandType.equals(Parser.C_RETURN)){ //Restore the caller’s state,
            String endFrameLabel = generateUniqueLabel("ENDFRAME");
            String returnAddrLabel = generateUniqueLabel("retAddr");

            asmCode += "// returning function " + segment + "\n";
    
                    asmCode += "@LCL\n";
                    asmCode += "D=M\n";  //D holds the data of the register LCL
                    asmCode += "@"+endFrameLabel+"\n";  //a new lable that now holds the address of LCL
                    asmCode += "M=D\n";     //ENDFRAME=LCL ()       
                
            // Save the return address in endframe (FRAME = LCL, RET = *(FRAME - 5))
                    asmCode += "@"+endFrameLabel+"\n";
                    asmCode += "D=M\n";             //D = endframe 
                    asmCode += "@5\n";              //to get number 5
                    asmCode += "D=D-A\n";           // D = ENDFRAME - 5
                    asmCode += "A=D\n";             // A = ENDFRAME - 5
                    asmCode += "D=M\n";             //Dereferences the address in A and loads the value at that address into D.
                    asmCode += "@"+returnAddrLabel+"\n"; //new lable to hold the return address
                    asmCode += "M=D\n";           //  retAddr = *(ENDFRAME - 5)
                    
                    //put the return value for the caller
                    asmCode += "@SP\n";
                    asmCode += "M=M-1\n";          // SP = SP - 1 (ARG place, one before SP)
                    asmCode += "A=M\n";            // A = *SP
                    asmCode += "D=M\n";            // D = *SP (return value)
                    asmCode += "@ARG\n";
                    asmCode += "A=M\n";            // Address where the caller expects the result
                    asmCode += "M=D\n";            // Store the return value in ARG[0]
                    
                    // Reposition SP one place after ARG
                    asmCode += "@ARG\n";            //
                    asmCode += "D=M\n";            // 
                    asmCode += "@SP\n";            // 
                    asmCode += "M=D+1\n";          // SP = ARG + 1 
        
                    // Step 3: Restore THAT, THIS, ARG, LCL (in reverse order)
                    // Restore THAT (LCL-1)
                    asmCode += "@"+endFrameLabel+"\n";
                    asmCode += "D=M-1\n";          // D = LCL-1
                    asmCode += "A=D\n";            // A = LCL-1
                    asmCode += "D=M\n";            // D = THAT (stored at LCL-1)
                    asmCode += "@THAT\n";
                    asmCode += "M=D\n";            // THAT = D
        
                    //Restore THIS (LCL-2)
                    asmCode += "@"+endFrameLabel+"\n";
                    asmCode += "D=M\n"; // D = LCL
                    asmCode += "@2\n";
                    asmCode += "D=D-A\n";    // D = LCL - 2
                    asmCode += "A=D\n";     // A = LCL - 2
                    asmCode += "D=M\n";     // D = *(LCL - 2)
                    asmCode += "@THIS\n"; // THIS = *(LCL - 2)
                    asmCode += "M=D\n";
        
                    // Restore ARG (LCL-3)
                    asmCode += "@"+endFrameLabel+"\n";
                    asmCode += "D=M\n"; // D = LCL
                    asmCode += "@3\n";
                    asmCode += "D=D-A\n";    // D = LCL - 3
                    asmCode += "A=D\n";
                    asmCode += "D=M\n";
                    asmCode += "@ARG\n"; // ARG = *(LCL - 3)
                    asmCode += "M=D\n";

                    // Restore LCL (LCL-4)
                    asmCode += "@"+endFrameLabel+"\n";
                    asmCode += "D=M\n"; // D = LCL
                    asmCode += "@4\n";
                    asmCode += "D=D-A\n";   // D = LCL - 4
                    asmCode += "A=D\n";    // A = LCL-4
                    asmCode += "D=M\n";     // D = LCL (stored at LCL-4)
                    asmCode += "@LCL\n";
                    asmCode += "M=D\n";     // LCL = D
        
                    // Step 4: Go to the return address (stored at LCL-5)
                    asmCode += "@"+returnAddrLabel+"\n";
                    asmCode += "A=M\n";            // A = LCL-5
                    asmCode += "0;JMP\n";        // Jump to the return address (C_RETURN ends)
                        }             

                }

            writer.write(asmCode);
            writer.newLine();      

        } 
    }
        public void close() throws IOException {
            if (writer != null) {
                writer.close();
            }
        }
    }


