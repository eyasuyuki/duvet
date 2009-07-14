package org.javaopen.lisp;

public class List extends Cons implements Sexp {

	public List() { super(); }// セルの生成

	public List(Sexp kar, Sexp kdr) {
		super();
		super.car = kar;
		super.cdr = kdr;
	}

	/**
	* car 部をセット
	*/
	public Sexp setCar(Sexp sexp) { return car = sexp; }

	/**
	* cdr 部をセット
	*/
	public Sexp setCdr(Sexp sexp) { return cdr = sexp; }

	public java.lang.String serialize() {
		StringBuffer str = new StringBuffer();
		List list = this;
		str.append("("); // Open "("
		for (;;) {
			str.append(list.car.serialize());// Car 部
			if ((Sexp)list.cdr == Nil.NIL) {
				 str.append(")");// Close ")"
				 break;
			} else if (((Sexp)list.cdr) instanceof Atom) {
				 str.append(" . ");// ドット対
				 str.append(list.cdr.serialize()); // Cdr 部
				 str.append(")");// Close ")"
				 break;
			} else {
				str.append(" "); // 空白
				list = (List)list.cdr; // 次の Cdr 部へ
			}
		} // end of for
		return "" + str;
	}

	public int size() {
		   List list = this;
		   for (int i = 1; ; i++) {
		      if (list.cdr instanceof Nil || (list.cdr) instanceof Atom) return i; 
		      list = (List)list.cdr;
		   }
		}

}
