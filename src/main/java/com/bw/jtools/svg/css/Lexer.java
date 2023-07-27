

package com.bw.jtools.svg.css;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

/**
 * Lexer: reads characters and returns token,
 * The lexer doesn't know about datatypes like numbers. It simply tokenize the input-stream by stop-characters.
 * E.g. a floating-point-mumber "-123.3e22 will be split into a sequence of "-", "123", ".", "3", "e", "22".<br>
 * Identifiers bound by a " or ' quote are retuend as one token (also including stop characters and white-spaces).<br>
 * E.g. 'abc,123.2 ' will be returned as one token "abc,123.2 " (without the quotes).
 */
public class Lexer
{
	private Reader reader_;

	private static final int[] separators_;

	static
	{
		// +,- are no separators in css.
		separators_ = new int[]{'\t', ' ', ':', '\n', ';', '.', ',', '(', ')', '{', '}', '[', ']', '#', '*', '?', '=', '%', '@', '!', -1};
		Arrays.sort(separators_);
	}

	private LexerSymbol reused_;
	private int stringDelimiter = -100;

	public Lexer(Reader reader, boolean reUseSymbol)
	{
		reader_ = reader;
		if (reUseSymbol)
			reused_ = new LexerSymbol();
	}

	StringBuilder buffer = new StringBuilder();

	public LexerSymbol nextSymbol()
	{
		LexerSymbol result = reused_ == null ? new LexerSymbol() : reused_;

		// at start of new symbol, eat all spaces
		eatSpace();
		int c;
		while (true)
		{
			if (isStop(c = nextChar()))
			{
				if (c > 0)
				{
					// If not an EOF
					if (buffer.length() == 0)
						// if empty, return the current stop as symbol
						buffer.append((char) c);
					else if (!isStringDelimiter(c))
						// otherwise handle it next call
						pushBack(c);
				}
				break;
			}
			// append until stop is found.
			buffer.append((char) c);
		}

		// Return current buffer
		result.value_ = buffer.toString();
		buffer.setLength(0);

		if (result.value_.isEmpty())
			result.type_ = LexerSymbolType.EOF;
		else if (result.value_.length() > 1)
			result.type_ = LexerSymbolType.IDENTIFIER;
		else
			result.type_ = isStopChar(result.value_.charAt(0)) ? LexerSymbolType.SEPARATOR : LexerSymbolType.IDENTIFIER;
		return result;
	}

	/**
	 * Check if the character is a stop in current state.
	 */
	protected boolean isStop(int c)
	{
		if (c < 0)
			return true;

		if (stringDelimiter > 0)
		{
			if (c == stringDelimiter)
			{
				stringDelimiter = -100;
				return true;
			}
			else
				// EOF is in any case a stop
				return c < 0;
		}
		else if (isStopChar(c))
		{
			if (isStringDelimiter(c))
				stringDelimiter = c;
			return true;
		}
		else
			return false;
	}

	/**
	 * Check if the character is in list of stop characters
	 */
	protected boolean isStopChar(int c)
	{
		// Binary search
		int startIndex = 0;
		int endIndex = separators_.length - 1;
		int midIndex;

		while (startIndex <= endIndex)
		{
			midIndex = (startIndex + endIndex) >>> 1;
			int midVal = separators_[midIndex];
			if (midVal > c)
				endIndex = midIndex - 1;
			else if (midVal < c)
				startIndex = midIndex + 1;
			else
				return true;
		}
		return false;
	}

	protected boolean isStringDelimiter(int c)
	{
		return (c == '\'' || c == '"');
	}

	protected void eatSpace()
	{
		if (stringDelimiter < 0)
		{
			int c;
			while (Character.isWhitespace(c = nextChar())) ;

			if (isStringDelimiter(c))
				stringDelimiter = c;
			else if (c > 0)
				pushBack(c);
		}
	}

	/**
	 * Push back stack.
	 * Currently only 1 char is needed.
	 */
	private int[] pushback_ = new int[10];
	private int pushbackPos_ = -1;

	private void pushBack(int c)
	{
		pushback_[++pushbackPos_] = c;
	}

	private int nextChar()
	{
		if (pushbackPos_ < 0)
		{
			if (reader_ == null)
				return -1;
			try
			{
				return reader_.read();
			}
			catch (IOException e)
			{
				exception_ = e;
				reader_ = null;
				return -1;
			}
		}
		else
			return pushback_[pushbackPos_--];
	}

	private Exception exception_;

}
