package org.javaopen.lisp;

public class Integer extends Number {
	private int value;

	  public Integer() { value = 0; }
	  public Integer(int i) { value = i; }
	  public Integer(long l) { value = (int)l; }
	  public Integer(java.lang.String str) { 
	    value = new java.lang.Integer(str).intValue();
	  }

	  public int valueOf() { return value; }

	  public Sexp equal(Integer i)  {
		    if (value == i.valueOf()) return T.T; else return Nil.NIL;
		  }
	  
	  public java.lang.String serialize() {
		return java.lang.Integer.toString(value);
	}

}
