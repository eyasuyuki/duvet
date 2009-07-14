package org.javaopen.lisp;


public class Nil extends List implements Sexp {
	public static final Nil NIL = new Nil();    // static 変数版 NIL

	public java.lang.String serialize() {
		return "NIL";
	}
}
