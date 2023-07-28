package com.bw.jtools.svg.css;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest
{

	@Test
	void parse()
	{
		CSSParser cssParser = new CSSParser();
		// Attribute selectors are currently ignored
		Lexer lx = new Lexer(new StringReader("[xxx=123], .class dev , #someid span { color: #123456; background: red blue; }"), true);
		CssStyleSelector ssel = new CssStyleSelector();
		cssParser.parse(lx, ssel);

		for (SelectorRule r : ssel.rules_)
		{
			for (Selector s : r.selectors_)
			{
				System.out.print("Selector");
				while (s != null)
				{
					System.out.print(" " + s.type_.name() + " " + s.id_);
					s = s.combinate_;
				}
				System.out.println("");

			}
			System.out.print("-> " + r.styles_);
		}
		// [] (attributes) are currently ignored.
		assertEquals(1, ssel.rules_.size());
		assertEquals(2, ssel.rules_.get(0).selectors_.size());
	}
}