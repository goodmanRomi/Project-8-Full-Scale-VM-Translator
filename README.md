# **Project 8: Full-Scale VM Translator**

We built a full-scale VM translator capable of handling branching and function commands, as well as translating multi-file VM programs. This project completes the development of the VM translator, which serves as the backend for the Hack compiler.

---

## **Objective**
Our goal was to develop a full-scale VM-to-Hack translator that:
- Handles branching commands (`label`, `goto`, `if-goto`).
- Translates function commands (`function`, `call`, `return`).
- Supports multi-file VM programs with proper bootstrap code.

---

## **How to Use**
1. **Clone the Repository**  
   Clone the repository from the provided URL.

2. **Run the VM Translator**  
   Translate one or multiple `.vm` files into a single `.asm` file using our translator.

3. **Test the Output**  
   Use the [CPU Emulator](https://nand2tetris.github.io/web-ide/cpu) to load and execute the generated `.asm` files, verifying their correctness using the provided test scripts.

---

## **Key Features**
- **Branching Commands:** Supports commands like `label`, `goto`, and `if-goto`.  
- **Function Handling:** Implements commands such as `function`, `call`, and `return`.  
- **Multi-File Translation:** Combines multiple `.vm` files into a single `.asm` file with bootstrap code.  
- **Bootstrap Code:** Initializes the stack pointer and invokes `Sys.init` to start the program.  

---

## **Testing**
We validated the translator's functionality using the following test programs:
- **Branching Commands:**
  - `BasicLoop.vm`: Tests `label` and `if-goto`.
  - `FibonacciSeries.vm`: Tests `label`, `goto`, and `if-goto`.
- **Function Commands:**
  - `SimpleFunction.vm`: Tests `function` and `return`.
  - `NestedCall.vm`: Tests nested function calls.
  - `FibonacciElement.vm`: Tests recursive calls with multiple files and bootstrap code.
  - `StaticsTest.vm`: Verifies static variable handling across multiple files.

---

## **References**
- [CPU Emulator](https://nand2tetris.github.io/web-ide/cpu)  
- [VM Emulator](https://nand2tetris.github.io/web-ide/vm)  
- [Nand2Tetris Course](https://www.nand2tetris.org/)
