

package com.bw.jtools.svg.css;

/**
 * A Lexer symbol
 */
public final class LexerSymbol
{
	public String value_;
	public LexerSymbolType type_;

	@Override
	public String toString()
	{
		return type_.name() + ": " + value_;
	}
}
