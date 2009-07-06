package org.javaopen.lisp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Reader {
	final int CharBuffSize = 4096; // 文字処理バッファのサイズ
	private char[] charBuff = null; // 文字処理バッファ
	private char ch; // 1文字バッファ
	private java.lang.String line; // 1行入力バッファ
	private int indexOfLine = 0; // 1行内の位置
	private int lineLength = 0; // 1行の文字数
	BufferedReader br = null; // java リーダ
	Env env = null; // 環境

	/**
	 * リスプリーダ
	 */
	public Reader() {
		env = new Env();
		charBuff = new char[CharBuffSize];
		br = new BufferedReader(new InputStreamReader(System.in));
	}

	/**
	 * リスプリーダ(環境)
	 */
	public Reader(Env environment) {
		env = environment;
		charBuff = new char[CharBuffSize];
		br = new BufferedReader(new InputStreamReader(System.in));
	}

	void getChar() {
		ch = charBuff[indexOfLine++];
	}

	char nextChar() {
		return charBuff[indexOfLine];
	}

	public Sexp read() throws IOException {
		getSexpPrepare();
		return getSexp();
	}

	/**
	 * S 式リーダ from 文字列
	 */
	public Sexp readFromString(java.lang.String input) throws IOException {
		getSexpPrepareString(input);
		return getSexp();
	}

	/**
	 * getSexp の準備　getSexpPrepare
	 */
	void getSexpPrepare() throws IOException {
		line = br.readLine(); // 1行読み込み
		gSP();
	}

	// getSexp の内部関数
	void gSP() {
		indexOfLine = 0;
		lineLength = line.length();
		// 効率化のために charArray へ格納する
		line.getChars(0, lineLength, charBuff, 0);
		charBuff[lineLength] = '\0'; // 終了マーク
		getChar();
	}

	/**
	 * 文字列からの読み込みの準備　getSexpPrepare
	 */
	void getSexpPrepareString(java.lang.String input) {
		line = input;
		gSP();
	}

	Sexp getSexp() throws IOException {
		for (; indexOfLine <= lineLength; getChar()) {
			switch (ch) {
			case '(':
				return makeList();
			case '\'':
				return makeQuote();
			case '-':
				return makeMinusNumber();
			case '"':
				getChar();
				return makeString();
			default:
				if (Character.isWhitespace(ch))
					break;
				if (Character.isDigit(ch))
					return makeNumber();
				return makeSymbol();
			} // end of switch
		} // end of for
		return Nil.NIL; // not reaech for javac
	}

	Sexp makeList() throws IOException {
		List top = new List();
		List list = top;
		getChar();
		while (true) {
			list.setCar(getSexp()); // car 部の読み込み
			if (ch == ')')
				break; // close が来れば終了
			if (indexOfLine == lineLength)
				return Nil.NIL; // 読み込み途中のときは NIL
			if (ch == '.') { // dot pair の読み込み
				getChar(); // dot の読み飛ばし
				list.setCdr(getSexp());
				getChar(); // close の読み飛ばし
				return top;
			}
			list.setCdr((Sexp) new List());
			list = (List) list.cdr;
		}
		getChar(); // close の読み飛ばし
		return top;
	}

	Sexp makeNumber() throws IOException {
		StringBuffer str = new StringBuffer();
		for (; indexOfLine <= lineLength; getChar()) {
			if (ch == '(' || ch == ')')
				break;
			if (Character.isWhitespace(ch))
				break;
			if (!Character.isDigit(ch)) {
				// nextChar()だけで制御するのは効率が悪いのでここだけ直接制御
				indexOfLine--;
				return makeSymbolInternal(str);
			}
			str.append(ch);
		}
		int value = new java.lang.Integer("" + str).intValue();
		return (Sexp) new Integer(value);
	}
	
	Sexp makeString() throws IOException {
		StringBuffer str = new StringBuffer();
		
		for (; indexOfLine < lineLength; getChar()) {
			if (ch == '"') {
				getChar();
				break;
			}
			if (ch == '\\') {
				if (nextChar() != '"')	    continue;
				else 					    getChar();
			}
			str.append(ch);
		}
		
		return new String(str.toString());
	}
	
	Sexp makeSymbol() throws IOException {
		//ch = Character.toUpperCase(ch);
		StringBuffer str = new StringBuffer().append(ch);
		return makeSymbolInternal(str);
	}

	/**
	 * 途中の文字列を渡してのシンボルの読み込み MakeSymbolInternal(StringBuffer)
	 */
	Sexp makeSymbolInternal(StringBuffer str) throws IOException {
		while (indexOfLine < lineLength) {
			ch = charBuff[indexOfLine++];
			if (ch == '(' || ch == ')')
				break;
			if (Character.isWhitespace(ch))
				break;
			//ch = Character.toUpperCase(ch);
			str.append(ch);
		}
		java.lang.String symStr = "" + str;

		if (symStr.equals("T"))
			return T.T; // T は特別に処理
		if (symStr.equals("NIL"))
			return Nil.NIL; // NIL は特別に処理

		Sexp sym = env.get(symStr); // intern されていれば、取得する
		if (sym == Nil.NIL)
			return env.put(new Symbol(symStr));
		return sym;
	}

	Sexp makeMinusNumber() throws IOException {
		char nch = charBuff[indexOfLine]; // 次の文字
		// - (マイナス) の処理
		if (Character.isDigit(nch) == false)
			return makeSymbolInternal(new StringBuffer().append(ch));
		return makeNumber();
	}

	/**
	 * makeQuote
	 */
	Sexp makeQuote() throws IOException {
		List top = new List();
		List list = top;
		list.setCar((Symbol) env.get("QUOTE"));
		list.setCdr((Sexp) new List());
		list = (List) list.cdr;
		ch = charBuff[indexOfLine++];
		list.setCar(getSexp());
		return top;
	}
}