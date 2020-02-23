/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *
 * @author jamesostmann
 */
public class Assembler {

    int currentAddress;
    ArrayList<Instruction> instructions;
    HashMap<String, Symbol> symbolTable;

    Scanner input;
    File out;
    PrintWriter output;

    public Assembler(String fName, String ofName) {

        init(fName, ofName);
        initIntructions();
        passOne();
        passTwo();
        write();
    }

    private void init(String fName, String ofName) {

        try {

            currentAddress = 0;
            instructions = new ArrayList<>();
            symbolTable = new HashMap<>();
            input = new Scanner(new File(fName));
            output = new PrintWriter(new File(ofName));

        } catch (FileNotFoundException e) {
            System.err.println("File not found");
        }
    }

    private void passOne() {
        String currentInstruction = "";
        StringTokenizer tokenizer;

        while (input.hasNextLine()) {

            currentInstruction = input.nextLine();
            tokenizer = new StringTokenizer(currentInstruction, " ", false);

            if (currentInstruction.contains("*")) {
                instructions.add(new Instruction(currentInstruction, "", null));
                continue;
            }

            String labelText = new String();

            while (tokenizer.hasMoreTokens()) {

                labelText = tokenizer.nextToken();
                labelText = labelText.replace(',', ' ');

                if (symbolTable.containsKey(labelText)) {
                    Symbol s = symbolTable.get(labelText);
                    s.mnumonic = labelText;
                    currentInstruction = currentInstruction.replace(labelText, "");
                    instructions.add(new Instruction(currentInstruction, Integer.toHexString(currentAddress), s));
                    currentAddress += s.length;
                    break;
                }

                String realSymbolText = tokenizer.nextToken();

                Symbol actualSymbol = symbolTable.get(realSymbolText);
                Symbol label = new Symbol(actualSymbol.opcode, actualSymbol.length, actualSymbol.format);
                label.memory = Integer.toHexString(currentAddress);
                label.label = labelText;
                label.mnumonic = realSymbolText;

                if (realSymbolText.equals("data")) {
                    String value = tokenizer.nextToken();
                    label.value = Integer.toHexString(Integer.parseInt(value));
                }

                symbolTable.put(labelText, label);

                currentInstruction = currentInstruction.replace(labelText, "");
                currentInstruction = currentInstruction.replace(realSymbolText, "");

                instructions.add(new Instruction(currentInstruction, Integer.toHexString(currentAddress), symbolTable.get(labelText)));
                currentAddress += symbolTable.get(labelText).length;

                break;

            }

        }

        input.close();

    }

    private void passTwo() {
        int memoryNum;

        for (int i = 0; i < instructions.size(); i++) {

            Instruction tempInstruction = instructions.get(i);

            if (tempInstruction.symbol == null) {
                continue;
            }

            String[] params;

            switch (tempInstruction.symbol.format) {

                case 0:
                    tempInstruction.machineCode += "    ";
                    tempInstruction.machineCode += tempInstruction.symbol.opcode;
                    params = tempInstruction.instruction.split(",");

                    for (int z = 0; z < 3; z++) {
                        memoryNum = Integer.parseInt(params[z].trim().substring(1, params[z].trim().length()));
                        tempInstruction.machineCode += Integer.toHexString(memoryNum);

                    }

                    break;

                case 1:
                    tempInstruction.machineCode += tempInstruction.symbol.opcode;
                    params = tempInstruction.instruction.split(",");
                    String reg = params[0].trim();

                    tempInstruction.machineCode += reg.substring(1, reg.length());

                    String indexReg = params[1].trim();
                    if (indexReg.contains("]")) {

                        String num = indexReg.substring(indexReg.indexOf("R") + 1, indexReg.indexOf("]"));
                        indexReg = indexReg.substring(0, indexReg.length() - 4);
                        tempInstruction.machineCode += num;

                        tempInstruction.machineCode += "0";

                        if (num.length() < 2) {
                            tempInstruction.machineCode += "0";
                        }

                        if (reg.substring(1, reg.length()).length() < 2) {
                            tempInstruction.machineCode += "0";
                        }

                    } else {
                        if (reg.substring(1, reg.length()).length() < 2) {
                            tempInstruction.machineCode += "0000";
                        } else {
                            tempInstruction.machineCode += "000";
                        }

                    }

                    Symbol get = symbolTable.get(indexReg);

                    if (get.memory.length() < 2) {

                        tempInstruction.machineCode += "0";

                    }

                    tempInstruction.machineCode += get.memory;
                    Symbol store = symbolTable.get(reg);
                    store.memory = get.memory;
                    break;

                case 2:
                    tempInstruction.machineCode += tempInstruction.symbol.opcode;
                    tempInstruction.machineCode += "0000";
                    String addr = tempInstruction.instruction.trim();
                    String register = "0";

                    if (addr.contains("]")) {
                        register = addr.substring(addr.indexOf("R") + 1, addr.indexOf("]"));
                        addr = addr.substring(0, addr.length() - 4);

                    }

                    tempInstruction.machineCode += register;

                    Symbol param = symbolTable.get(addr);

                    if (param.memory.length() < 2) {
                        tempInstruction.machineCode += "0";
                    }

                    tempInstruction.machineCode += param.memory;

                    break;
                case 3:
                    tempInstruction.machineCode += "      ";
                    tempInstruction.machineCode += tempInstruction.symbol.opcode;
                    memoryNum = Integer.parseInt(tempInstruction.instruction.trim().substring(1, tempInstruction.instruction.trim().length()));
                    tempInstruction.machineCode += Integer.toHexString(memoryNum);
                    break;
                case 4:
                    tempInstruction.machineCode += "      ";
                    tempInstruction.machineCode += tempInstruction.symbol.opcode;
                    tempInstruction.machineCode += "0";
                    break;
                case 5:
                    
                    for (int j = 0; j < 8 - tempInstruction.symbol.value.length(); j++) {
                        tempInstruction.machineCode += " ";
                    }
                    
                    tempInstruction.machineCode += tempInstruction.symbol.value;
                    break;
            }

            
        }

    }

    private void initIntructions() {
        Symbol currentSymbol;

        currentSymbol = new Symbol("0", 2, 0);
        symbolTable.put("add", currentSymbol);

        currentSymbol = new Symbol("1", 2, 0);
        symbolTable.put("sub", currentSymbol);

        currentSymbol = new Symbol("2", 2, 0);
        symbolTable.put("mult", currentSymbol);

        currentSymbol = new Symbol("3", 2, 0);
        symbolTable.put("div", currentSymbol);

        currentSymbol = new Symbol("4", 4, 1);
        symbolTable.put("load", currentSymbol);

        currentSymbol = new Symbol("5", 4, 1);
        symbolTable.put("laddr", currentSymbol);

        currentSymbol = new Symbol("6", 4, 1);
        symbolTable.put("store", currentSymbol);

        currentSymbol = new Symbol("7", 4, 2);
        symbolTable.put("call", currentSymbol);

        currentSymbol = new Symbol("8", 1, 4);
        symbolTable.put("rtn", currentSymbol);

        currentSymbol = new Symbol("9", 4, 2);
        symbolTable.put("jump", currentSymbol);

        currentSymbol = new Symbol("a", 4, 1);
        symbolTable.put("jz", currentSymbol);

        currentSymbol = new Symbol("b", 4, 1);
        symbolTable.put("jn", currentSymbol);

        currentSymbol = new Symbol("c", 1, 3);
        symbolTable.put("push", currentSymbol);

        currentSymbol = new Symbol("d", 1, 3);
        symbolTable.put("pop", currentSymbol);

        currentSymbol = new Symbol("e", 1, 3);
        symbolTable.put("lpsw", currentSymbol);

        currentSymbol = new Symbol("f", 1, 3);
        symbolTable.put("spsw", currentSymbol);

        currentSymbol = new Symbol("", 4, 5);
        symbolTable.put("data", currentSymbol);

        for (int i = 0; i < 16; i++) {
            currentSymbol = new Symbol("R" + (i + 1), 0, 0);
            symbolTable.put("R" + (i + 1), currentSymbol);
        }
    }

    @Override
    public String toString() {
        String result = "";
        result += "address \t machine \t source" + System.lineSeparator();
        for (int i = 0; i < instructions.size(); i++) {
            result += instructions.get(i).toString();
            result += System.lineSeparator();
        }
        return result;
    }

    private void write() {
        output.write(this.toString());
        output.close();
    }
}
