package com.bw.jtools.svg.css;

import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Simple CSS Parser.<br>
 */
public class CSSParser
{
	public static CssStyleSelector parse(String style, String type)
	{
		CssStyleSelector styleSelector = new CssStyleSelector();
		new CSSParser().parse(style, type, styleSelector);
		return styleSelector;
	}


	/**
	 * Parse a style sheet and put all style in the selector.<br>
	 * Method is not thread-safe, use separate instances on each thread!
	 */
	public void parse(String style, String type, CssStyleSelector styleSelector)
	{
		//@TODO: any other then "text/css"?
		parse(new Lexer(new StringReader(style), true), styleSelector);
	}

	private StringBuilder id;
	private StringBuilder declaration;
	private StringBuilder attribute;
	private static final Pattern styleSplitRegExp_ = Pattern.compile(";");

	private static class RuleStub
	{
		Selector selector;
		/**
		 * How the selector shall be chained to the previous one.
		 */
		CombinatorType chain;
	}

	private List<RuleStub> rules = new LinkedList<>();

	private Selector selector_;
	private CombinatorType chain;

	/**
	 * Parse a style sheet and put all style in the selector.<br>
	 * Method is not thread-safe, use separate instances on each thread!
	 */
	public void parse(Lexer lexer, CssStyleSelector styleSelector)
	{
		LexerSymbol symbol;
		LexerSymbolType lastLexType = LexerSymbolType.EOF;
		chain = null;
		id = null;
		declaration = null;
		attribute = null;
		loop:
		while (true)
		{
			symbol = lexer.nextSymbol();
			switch (symbol.type_)
			{
				case SEPARATOR:
					char c = symbol.value_.charAt(0);
					if (attribute != null)
					{
						if (c == ']')
							//@TODO: How to handle attributes in svg? Ignored for now.
							attribute = null;
						else
							attribute.append(c);
					}
					else if (declaration != null)
					{
						if (c == '}')
							styleSelector.rules_.add(generateRule());
						else
							declaration.append(c);
					}
					else
					{
						switch (c)
						{
							case '[':
								handleId();
								attribute = new StringBuilder();
								break;
							case '{':
								handleId();
								declaration = new StringBuilder();
								break;
							case ',':
								handleId();
								chain = null;
								break;
							case '#':
								startRule(SelectorType.ID, chain);
								chain = null;
								break;
							case '>':
								handleId();
								chain = CombinatorType.CHILD;
								break;
							case '~':
								handleId();
								chain = CombinatorType.SIBLING;
								break;
							case '+':
								handleId();
								chain = CombinatorType.ADJACENT_SIBLING;
								break;
							case '.':
								startRule(SelectorType.CLASS, chain);
								chain = null;
								break;
							default:
								// TODO: Space is a separator!
								// Otherwise "-" etc will not work
								// if "-" is not used in expressions, we can remove it from sep...
								// and don't need to handel spaces expl.
								if (id != null)
									id.append(c);
								break;
						}
					}
					break;
				case IDENTIFIER:
					if (attribute != null)
					{
						if (lastLexType != LexerSymbolType.SEPARATOR)
							attribute.append(' ');
						attribute.append(symbol.value_);

					}
					else if (declaration != null)
					{
						if (lastLexType != LexerSymbolType.SEPARATOR)
							declaration.append(' ');
						declaration.append(symbol.value_);
					}
					else
					{
						id = new StringBuilder();
						id.append(symbol.value_);
						handleId();
					}
					chain = CombinatorType.DESCENDANT;
					break;
				case EOF:
					break loop;
			}
			lastLexType = symbol.type_;
		}

	}

	/**
	 * Finalize a rule with current definition.
	 */
	private SelectorRule generateRule()
	{
		SelectorRule rule = new SelectorRule();

		rule.styles_ = parseStyle(declaration.toString());
		declaration = null;

		Selector lastSelector = null;
		while (!rules.isEmpty())
		{
			RuleStub stub = rules.remove(0);
			if (stub.chain != null && lastSelector != null)
			{
				lastSelector.combinate_ = stub.selector;
				lastSelector.combinateType_ = stub.chain;
			}
			else
				rule.selectors_.add(stub.selector);
			lastSelector = stub.selector;
		}
		return rule;
	}

	private void startRule(SelectorType type, CombinatorType chain)
	{
		handleId();
		RuleStub stub = new RuleStub();
		stub.selector = selector_ = new Selector();
		selector_.type_ = type;
		stub.chain = chain;
		rules.add(stub);
	}

	private Selector getCurrentSelector()
	{
		if (selector_ == null)
			startRule(SelectorType.TAG, null);
		return selector_;
	}

	private void handleId()
	{
		if (id != null)
		{
			String ids = id.toString();
			id = null;
			if (selector_ != null && selector_.id_ == null)
				selector_.id_ = ids;
			else
			{
				startRule(SelectorType.TAG, chain);
				chain = null;
				selector_.id_ = ids;
			}
		}
	}

	/**
	 * Parses a style definition, e.g. "width:10px;height30px" and returns all entries as map.
	 */
	public static Map<String, String> parseStyle(String style)
	{
		Map<String, String> attrs = new HashMap<>();
		if (style != null && !style.isEmpty())
		{
			String[] stylesAr = styleSplitRegExp_.split(style);
			for (String s : stylesAr)
			{
				final int i = s.indexOf(':');
				if (i > 0)
					attrs.put(s.substring(0, i)
							   .trim(), s.substring(i + 1));
			}
		}
		return attrs;
	}


}
