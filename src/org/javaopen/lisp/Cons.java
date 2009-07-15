package org.javaopen.lisp;

public class Cons {
	protected Sexp car = Nil.NIL;
	protected Sexp cdr = Nil.NIL;
	
	public Sexp car() { return car; }
	public Sexp cdr() { return cdr; }
}
