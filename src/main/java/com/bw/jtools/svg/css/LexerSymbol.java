package com.bw.jtools.svg.css;

/**
 * A Lexer symbol
 */
public final class LexerSymbol
{
	/**
	 * The value.
	 */
	public String value_;
	/**
	 * The symbol type.
	 */
	public LexerSymbolType type_;

	@Override
	public String toString()
	{
		return type_.name() + ": " + value_;
	}
}
