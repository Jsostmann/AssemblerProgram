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
public class Symbol {
    String mnumonic;
    String label;
    String opcode;
    int format;
    int length;
    String memory;
    String value;
    public Symbol(String opcode, int length, int format) {
    
        this.opcode = opcode;
        this.format = format;
        this.length = length;
        this.memory = "";
        this.value = "";
        this.label = "";
    }
    
    public Symbol() {
        this("",0,0);
       
    }
    
    @Override public String toString() {
       
       return this.label + "\t" + this.mnumonic;
      
    }
    
    
}
