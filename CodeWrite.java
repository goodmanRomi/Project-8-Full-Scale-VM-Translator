import java.io.*;


public class CodeWrite{

private String inputFile;
private String outputFile;
private BufferedWriter writer;
private static int labelCounter = 0; // Static counter for unique labels


    
    public CodeWrite(String outputFile) throws IOException{
        this.outputFile = outputFile;
        this.writer = new BufferedWriter(new FileWriter(outputFile));
    }
    
    public void writeComment(String comment) throws IOException {
        writer.write("// " + comment);
        writer.newLine();
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
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


