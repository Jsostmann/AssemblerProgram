/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assembler;



/**
 *
 * @author jamesostmann
 */
public class Instruction {
    String instruction;
    String memmoryAddress;
    String machineCode;
    Symbol symbol;
    
    
    public Instruction(String instruction, String memmoryAddress, Symbol symbol) {
    
        this.instruction = instruction;
        this.memmoryAddress = memmoryAddress;
        this.symbol = symbol;
        this.machineCode = "";
    
        
    }
    
    @Override
    public String toString() {
        
        if(symbol != null) {
            return memmoryAddress + "\t\t" + machineCode + "\t" + symbol.toString() + " "+ instruction.trim();
            
           
        }
        return instruction;
    }
}
