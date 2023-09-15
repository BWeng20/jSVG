package com.bw.jtools.svg.css;

import com.bw.jtools.svg.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A selector rule.
 */
public class SelectorRule
{
	/**
	 * List of selectors in this rule.
	 */
	public List<Selector> selectors_ = new ArrayList<>();

	/**
	 * List of attributes to set if rules match.
	 */
	public Map<Attribute, String> styles_ = new HashMap<>();

}
