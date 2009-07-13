package org.javaopen.lisp;

public class Symbol extends Atom {
    java.lang.String name = null;
    Sexp value = null;

    /**
    * シンボルの生成
    */
    public Symbol() {
    }

    /**
    * シンボルの生成(String)
    */
    public Symbol(java.lang.String s) {
      name = s;
    }

    /**
    * 値のセット
    */
    public Sexp setValue(Sexp val) {
      value = val;
      return value;
    }

    /**
    * 値のゲット
    */
    public Sexp getValue() {
      return value;
    }

    /**
    * シリアライズ（文字列化）
    */
    public java.lang.String serialize() { 
      return name;
    }

    public Sexp intern(Env env) {
      return env.put(this);
    }

    /**
    * シンボルのアンインターン
    */
    public Sexp unintern(Env env) {
      return env.remove(this);
    }
    
    public Sexp unbound() {
        value = null;
        return this;
      }

}
