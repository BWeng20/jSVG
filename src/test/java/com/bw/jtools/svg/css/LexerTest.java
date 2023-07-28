package com.bw.jtools.svg.css;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LexerTest
{
	@Test
	void numbers() throws IOException
	{
		Lexer lx = new Lexer(new StringReader(
				"fill:#332211; width: 0.34rem"), true);

		LexerSymbol t = lx.nextSymbol();
		assertEquals("fill", t.value_);
		t = lx.nextSymbol();
		assertEquals(":", t.value_);
		t = lx.nextSymbol();
		assertEquals("#", t.value_);
		t = lx.nextSymbol();
		assertEquals("332211", t.value_);
		assertEquals(LexerSymbolType.IDENTIFIER, t.type_);
		t = lx.nextSymbol();
		assertEquals(";", t.value_);
		t = lx.nextSymbol();
		assertEquals("width", t.value_);
		t = lx.nextSymbol();
		assertEquals(":", t.value_);
		t = lx.nextSymbol();
		assertEquals("0", t.value_);
		t = lx.nextSymbol();
		assertEquals(LexerSymbolType.SEPARATOR, t.type_);
		assertEquals(".", t.value_);
		t = lx.nextSymbol();
		assertEquals("34rem", t.value_);
		assertEquals(LexerSymbolType.IDENTIFIER, t.type_);
	}

	@Test
	void nextToken() throws IOException
	{
		Lexer lx = new Lexer(new StringReader(
				"{#q500-500 --+3.00e-22symb:::{\n" +
						"  color: \"red \";\n" +
						"  width: 500px;border: 1px solid black;\n" +
						"background:url('a-()[]sdf');" +
						"}"), true);

		LexerSymbol t;
		int c = 0;
		while ((t = lx.nextSymbol()).type_ != LexerSymbolType.EOF)
		{
			c++;
			System.out.println(" '" + t + "'");
		}
		assertEquals(32, c);
		assertEquals(LexerSymbolType.EOF, t.type_);
		assertEquals("", t.value_);
	}
}