package org.javaopen.lisp;

public class String extends Atom implements Sexp {
	private java.lang.String value = null;
	
	public String(java.lang.String value) {
		this.value = value;
	}
	
	public java.lang.String valueOf() { return value; }

	public java.lang.String serialize() {
		return "\"" + value + "\"";
	}

}
