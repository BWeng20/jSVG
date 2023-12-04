package com.bw.jtools.ui;

import com.bw.jtools.svg.SVGConverter;
import com.bw.jtools.svg.SVGException;

import javax.swing.Icon;
import java.io.IOException;
import java.net.URL;

/**
 * Implements OpenIde SVG Loader.
 */
public class OpenIdeSVGLoader implements org.openide.util.spi.SVGLoader
{
	@Override
	public Icon loadIcon(URL url) throws IOException
	{
		try
		{
			return new ShapeIcon(SVGConverter.convert(url.openStream()));
		}
		catch (SVGException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
