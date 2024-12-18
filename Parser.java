import java.io.*;
import java.util.regex.*; 
//
public class Parser{
    //command types
    public static final String C_ARITHMETIC = "C_ARITHMETIC";
    public static final String C_PUSH = "C_PUSH";
    public static final String C_POP = "C_POP";
    
    public static final Object C_FUNCTION = "function";
    public static final Object C_LABEL = "label";
    public static final Object C_GOTO = "goto";
    public static final Object C_IF = "if";
    public static final Object C_CALL = "call";
    public static final Object C_RETURN = "return";
    
    //types of arithmetics:
    public static final String ADD = "add";
    public static final String SUB = "sub";
    public static final String NEG = "neg";
    public static final String EQ  = "eq";
    public static final String GT  = "gt";
    public static final String LT  = "lt";
    public static final String AND = "and";
    public static final String OR  = "or";
    public static final String NOT = "not";

     // Memory Segment Constants
     public static final String CONSTANT = "constant";
     public static final String LOCAL = "local";
     public static final String ARGUMENT = "argument";
     public static final String THIS = "this";
     public static final String THAT = "that";
     public static final String POINTER = "pointer";
     public static final String TEMP = "temp";
     public static final String STATIC = "static";
 
    
    private String currentCommand; //current command being proccessed 
    private static final Pattern commentPattern = Pattern.compile("//.*$");//code defines a pattern that will match any text starting with "//" and continuing to the end of the line, effectively capturing single-line comments.

    private BufferedReader reader; //reads the file line by line

    public Parser(String inputFile) throws IOException {
        //lines = Files.readAllLines(new File(inputFile).toPath());//code snippet opens the file specified by inputFile, reads all the lines from it, and stores each line as a separate string in the lines list.
        reader = new BufferedReader(new FileReader(inputFile));
        currentCommand = null;
    }

    public boolean hasMoreCommands() throws IOException {
        return reader.ready(); // Returns true if there are more commands to process
    }

    //assuming i have more to read, otherwise i wouldn't have called the function in the first place
    public void advance() throws IOException{
        currentCommand=null; //reset current command to null, to be prepared to read the next one if availbe
        String line = reader.readLine();//reads the next line directly         
        //removes comments and whitespaces 
        line = commentPattern.matcher(line).replaceAll(" ").trim();
        if (line.isEmpty()){
            advance(); //skip empty lines in a recursive call
        } else {
            currentCommand = line;
        }
    }

    public String commandType(){ 
        if (currentCommand.startsWith("pop")){
            return "C_POP";
        } else if(currentCommand.startsWith("push")){
            return "C_PUSH";
        } else if(currentCommand.startsWith("function")){
            return "C_FUNCTION";
        } else if (currentCommand.startsWith("goto")){
            return "C_GOTO";
        } else if (currentCommand.startsWith("label")) {
            return "C_LABEL";
        } else if (currentCommand.startsWith("if-goto")){
            return "C_IF";
        } else if (currentCommand.startsWith("return")){
            return "C_RETURN"; 
        }else if (currentCommand.startsWith("call")){
            return "C_CALL";
        } else { return "C_ARITHMETIC";}
    }

    //returns the first argument of the current command
    public String arg1(){
       if (commandType().equals("C_ARITHMETIC")) {
        return currentCommand;
    } else { 
        if (!commandType().equals("C_RETURN")) {
            return currentCommand.split(" ")[1];
        } else {return " ";} //shouldnt be called if the current comand is c-return - not sure yet where and how
    }
    }

    public int arg2(){
        if ((commandType().equals("C_PUSH")) || (commandType().equals("C_POP"))|| (commandType().equals("C_FUNCTION"))|| (commandType().equals("C_CALL"))) {
        String[] parts = currentCommand.split("\\s+");
        int address= Integer.parseInt(parts[2]);
        return address;
        }
            return -1; //double check that doesnt screw us up
        }

}
