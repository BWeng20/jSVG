package com.bw.jtools.svg;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ElementCache
{
	private final HashMap<String, ElementWrapper> wrapperById_ = new HashMap<>();
	private static AtomicLong idGenerator_ = new AtomicLong(9999);
	private final HashMap<String, Marker> markerById_ = new HashMap<>();


	private String generateId()
	{
		return "_#" + idGenerator_.incrementAndGet() + "Generated__";
	}

	public static boolean isGenerated(String id)
	{
		return id != null && id.startsWith("_#") && id.endsWith("Generated__");
	}

	public void scanForIds(Node node)
	{
		if (node.getNodeType() == Node.ELEMENT_NODE)
		{
			String id = ((Element) node).getAttribute("id");
			if (ElementWrapper.isNotEmpty(id))
			{
				if (wrapperById_.containsKey(id))
				{
					// Duplicate ids are no hard error, as svg seems to allow it.
					// As we handle the element-wrapper via id, we need to remove the id.
					SVGConverter.warn("SVG: Duplicate %s", id);
					((Element) node).removeAttribute("id");
				}
				else
					wrapperById_.put(id, new ElementWrapper(this, (Element) node));
			}
		}
		Node next = node.getNextSibling();
		if (next != null) scanForIds(next);
		next = node.getFirstChild();
		if (next != null) scanForIds(next);
	}

	public ElementWrapper getElementWrapper(Node node)
	{
		if (node instanceof Element)
		{
			Element element = (Element) node;
			String id = element.getAttribute("id");
			if (ElementWrapper.isNotEmpty(id))
				return wrapperById_.get(id);
			else
			{
				element.setAttribute("id", id = generateId());
				ElementWrapper ew = new ElementWrapper(this, element);
				wrapperById_.put(id, ew);
				return ew;
			}
		}
		else
			return null;
	}

	public ElementWrapper getElementWrapperById(String id)
	{
		if (ElementWrapper.isNotEmpty(id))
			return wrapperById_.get(id);
		else
			return null;
	}

	/**
	 * Calls a function on each element of a sub-tree.
	 * Includes the root and all sub-elements.
	 *
	 * @param consumer The function to call.
	 */
	public void forSubTree(Node root, Consumer<ElementWrapper> consumer)
	{
		final Queue<Node> unvisited = new LinkedList<>();
		unvisited.add(root);
		while (!unvisited.isEmpty())
		{
			Node n = unvisited.remove();
			final ElementWrapper w = getElementWrapper(n);
			if (w != null)
				consumer.accept(w);
			n = n.getFirstChild();
			while (n != null)
			{
				unvisited.add(n);
				n = n.getNextSibling();
			}
		}
	}

	public Marker getMarkerById(String id)
	{
		return markerById_.get(id);
	}

	public void addMarker(String id, Marker marker)
	{
		markerById_.put(id, marker);
	}
}
