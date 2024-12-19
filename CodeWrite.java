import java.io.*;
import java.lang.foreign.MemoryLayout.PathElement;

public class CodeWrite{

private String inputFile;
private String outputFile;
private BufferedWriter writer;
private static int labelCounter = 0; // Static counter for unique labels

    
    public CodeWrite(String outputFile) throws IOException{
        this.outputFile = outputFile;
        this.writer = new BufferedWriter(new FileWriter(outputFile));
    }
    
    //public void writeComment(String comment) throws IOException {
     //   writer.write("// " + comment);
      //  writer.newLine();
   // }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public void setFileName(String fileName) {
        System.out.println(" translation of file: " + fileName);
        this.inputFile = fileName;
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
            asmCode = " ";
            String returnAddressLabel = segment + "$ret." + labelCounter++; //translation according to slide 42


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

                    case Parser.STATIC://v
                    asmCode += " //" + Parser.C_PUSH + " static " + index + "\n";
                    asmCode += " @Static." + index + "\n";
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
                    asmCode += " @Static." + index + "\n";
                    asmCode += " M=D\n";
                    break;
            }
            //caller
       
        } else if (commandType.equals(Parser.C_FUNCTION)){ //function FunctionName nVars, Create a label for the function, Initialize the local variables to 0.
            asmCode += " //command is a function declaration for fucntion " + segment + "\n";
            asmCode +=" (" +segment+ ")\n";       // Function entry point label
            //asm code that initiallizes the local variables according to the int proivded to 0s
            asmCode +=" @"+index+"\n"; //placeholder representing the number of local variables you want to initialize
            asmCode +=" D=A\n"; // used the @index to get literallly the number of arguments i will be needing to initiallize
            asmCode +=" @R13\n";//using it as a temporary register to be the counter for the loop
            asmCode +=" M=D\n";//store the number of variables i will be needing in temporary @R13
            asmCode +="(INIT_LOCALS)\n"; //start of the loop for initializtion 
            asmCode +=" @0\n D=A\n @SP\n A=M\n M=D\n @SP\n @M=M+1\n"; // using register 0 to get 0 as data to put into SP, change SP to point to 0 and also to store 0 there, and increment it
            asmCode +=" @R13\n M=M-1\n @INIT_LOCALS\n D=M\n @END_INIT\n D;JEQ\n @INIT_LOCALS\n 0;JMP\n"; //decreament counter for loop, set stop for loop, if my R13 is not yet equals 0, i will jump to start again
            asmCode +=" (END_INIT)\n"; //jump here once we finished the loop to initiate all
        

        } else if (commandType.equals(Parser.C_LABEL)){
            asmCode += segment+"\n"; // Add the label code to the assembly output

        } else if (commandType.equals(Parser.C_GOTO)){
            asmCode += " //" + Parser.C_GOTO + "\n";
            asmCode +=" @" + segment + "\n";
            asmCode +=" 0;JMP\n";
            
        } else if (commandType.equals(Parser.C_IF)){ //must write code that pushes a boolean expression onto the stack before the IF expression
            asmCode += " //" + Parser.C_IF + "\n";  //if i have true at the top of my stack, jump to execute the command just after the label
            asmCode +=" @SP\n"; //load SP 
            asmCode +=" M=M-1\n"; //move SP to point at last element placed in stack
                                  //Check if the popped value is true (non-zero).
            asmCode +=" @" + segment + "\n"; //Jump to the label if the value is true.
            asmCode +=" 0;JMP\n";
            asmCode +=" A=M\n"; //point to the top of the stack 
            asmCode +=" M=M-1\n"; //move
            asmCode +=" D=M\n"; //D = *SP (pop the top value) 
            asmCode +=" @" + segment + "\n"; //Check if the popped value is true (non-zero)
            asmCode +=" D;JNE\n"; //Jump to the label if the value in D is not 0 hence true

        } else if (commandType.equals(Parser.C_CALL)){ //Save the caller's state (return address, frame pointers), Jump to the function being called.
                //receive func name and nArgs
                
                asmCode += "//calling" + Parser.C_CALL + " " + segment + " " + index + "\n";

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
                asmCode += "@" + (5 + index) + "\n";  // ARG = SP - (nArgs + 5)
                asmCode += "D=A\n";
                asmCode += "@ARG\n";
                asmCode += "M=D\n";               // Set ARG to SP - (nArgs + 5)

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
            // Step 1: Save the return address (LCL-5) into a temporary register returnAddressLabel
            asmCode += "@LCL\n";
            asmCode += "D=M\n";            // D = LCL
            asmCode += "@5\n";
            asmCode += "A=D-A\n";          // A = LCL - 5
            asmCode += "D=M\n";            // D = return address
            asmCode += "@returnAddressLabel\n";
            asmCode += "M=D\n";            // returnAddressLabel = return address 
            
            // Step 2: Store return value in ARG[0]
             asmCode += "@SP\n";
             asmCode += "M=M-1\n";          // SP = SP - 1 (ARG place, one before SP)
             asmCode += "A=M\n";            // A = *SP
             asmCode += "D=M\n";            // D = *SP (return value)
             asmCode += "@ARG\n";
             asmCode += "A=M\n";            // ARG[0]
             asmCode += "M=D\n";            // ARG[0] = D (return value)
 
             // Reposition SP one place after ARG
             asmCode += "@ARG\n";
             asmCode += "D=M+1\n";
             asmCode += "@SP\n";
             asmCode += "M=D\n";
 
             // Step 3: Restore THAT, THIS, ARG, LCL (in reverse order)
             // Restore THAT (LCL-1)
             asmCode += "@LCL\n";
             asmCode += "D=M-1\n";          // D = LCL-1
             asmCode += "A=D\n";            // A = LCL-1
             asmCode += "D=M\n";            // D = THAT (stored at LCL-1)
             asmCode += "@THAT\n";
             asmCode += "M=D\n";            // THAT = D
 
             //Restore THIS (LCL-2)
             asmCode += "@LCL\n";
             asmCode += "D=M-2\n";
             asmCode += "A=D\n";            // A = LCL-2
             asmCode += "D=M\n";
             asmCode += "@THIS\n";
             asmCode += "M=D\n";
 
             // Restore ARG (LCL-3)
             asmCode += "@ARG\n";
             asmCode += "D=M-3\n";
             asmCode += "A=D\n";
             asmCode += "D=M\n";
 
             // Restore LCL (LCL-4)
             asmCode += "@LCL\n";
             asmCode += "D=M-4\n";          // D = LCL-4
             asmCode += "A=D\n";            // A = LCL-4
             asmCode += "D=M\n";            // D = LCL (stored at LCL-4)
             asmCode += "@LCL\n";
             asmCode += "M=D\n";            // LCL = D
 
             // Step 4: Go to the return address (stored in returnAddressLabel)
             asmCode += "@returnAddressLabel\n";
             asmCode += "A=M\n";          // D = LCL-5 (return address)
             asmCode += "0;JMP\n";          // Jump to the return address (C_RETURN ends)
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


