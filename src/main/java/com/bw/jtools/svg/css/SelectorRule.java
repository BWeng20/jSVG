package com.bw.jtools.svg.css;

import com.bw.jtools.svg.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectorRule
{
	public List<Selector> selectors_ = new ArrayList<>();
	public Map<Attribute, String> styles_ = new HashMap<>();

}
