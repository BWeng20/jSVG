package com.bw.jtools.svg.css;


import com.bw.jtools.svg.Attribute;
import com.bw.jtools.svg.ElementCache;
import com.bw.jtools.svg.ElementWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CssStyleSelector
{
	public List<SelectorRule> rules_ = new ArrayList<>();

	private static class SelectorEntry
	{
		SelectorRule rule_;
		Selector selector_;

		public SelectorEntry(Selector s, SelectorRule rule)
		{
			selector_ = s;
			rule_ = rule;
		}
	}

	public void apply(Node root, ElementCache cache)
	{

		// As css is not used in svg very often, this is not implemented in an optimized way.

		for (SelectorRule rule : rules_)
		{
			for (Selector s : rule.selectors_)
			{
				apply(rule, s, root, cache, new Specificity());
			}
			//....
		}
	}

	protected void apply(SelectorRule rule, Selector s, Node n, ElementCache cache, Specificity spec)
	{
		if (s == null)
		{
			// No further condition.
			setStyles(rule, cache.getElementWrapper(n), spec);
		}
		else
		{
			Specificity specificity = new Specificity(spec);
			switch (s.type_)
			{
				case ID:
					ElementWrapper w = cache.getElementWrapperById(s.id_);
					if (w != null && isAnchestor(n, w.getNode()))
					{
						specificity.addIdMatch();
						apply(rule, s.combinate_, w.getNode(), cache, specificity);
					}
					break;
				case CLASS:
					specificity.addClassMatch();
					cache.forSubTree(n, elementWrapper ->
					{
						if (elementWrapper.hasClass(s.id_))
							apply(rule, s.combinate_, elementWrapper.getNode(), cache, specificity);
					});
					break;
				case TAG:
					while (n != null && !(n instanceof Element || n instanceof Document))
					{
						if (n.getNextSibling() == null)
							n = n.getFirstChild();
						else
							n = n.getNextSibling();
					}
					if (n != null)
					{
						specificity.addTagMatch();
						NodeList nodes = (n instanceof Element) ? ((Element) n).getElementsByTagName(s.id_) : ((Document) n).getElementsByTagName(s.id_);
						for (int i = 0; i < nodes.getLength(); ++i)
						{
							Node node = nodes.item(i);
							apply(rule, s.combinate_, node, cache, specificity);
						}
					}
					break;
			}
		}
	}

	private boolean isAnchestor(Node anchestor, Node descendant)
	{
		while (descendant != null && !descendant.isSameNode(anchestor))
		{
			descendant = descendant.getParentNode();
		}
		return descendant != null;
	}

	protected void setStyles(SelectorRule rule, ElementWrapper w, Specificity specificity)
	{
		if (w != null)
		{
			Map<Attribute, StyleValue> styles = w.getStyleAttributes();
			for (Map.Entry<Attribute, String> entry : rule.styles_.entrySet())
			{
				final Attribute style = entry.getKey();
				final String value = entry.getValue();
				StyleValue sv = styles.get(style);
				if (sv == null)
					styles.put(style, sv = new StyleValue(value, new Specificity(specificity)));
				else if (specificity.isMoreSpecificOrEqual(sv.specificity_))
				{
					sv.specificity_.setTo(specificity);
					sv.value_ = value;
				}
			}
		}
	}

}
