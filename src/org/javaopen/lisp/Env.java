package org.javaopen.lisp;

import java.util.Hashtable;

public class Env extends Hashtable {
	public Env() { super(); }
	
	/**
	* シンボル名とそのシンボルをハッシュテーブルにセットする
	*/
	public Symbol put(Symbol value) {
	     put(value.serialize(), value);
	     return value;
	}
	
	public Symbol put(java.lang.String name, Symbol value) {
	     super.put(name, value);
	     return value;
	}
	
	/**
	* シンボルを取り出す
	*/
	public Sexp get(Symbol sym) throws Exception {
	     return get(sym.serialize());
	}
	
	/**
	* get(String)
	*/
	public Sexp get(java.lang.String name) {
	     Symbol sym = (Symbol)super.get(name);
	     if (sym != null) {
	            return sym;
	     } else {
	            return Nil.NIL;
	     }
	}
	
	/**
	* remove
	*/
	public Sexp remove(Symbol sym) {
	     super.remove(sym.serialize());
	     return sym;
	}
}
