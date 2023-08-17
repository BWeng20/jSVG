package com.bw.jtools.ui;

import com.bw.jtools.svg.SVGConverter;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Very simple JFileChooser preview. Don't use this in real projects.
 * The code is only for demonstration.<br>
 * For real previews images should be (weakly) cached, loaded in background and
 * (very important) there should be a size limit.
 */
public class SVGFilePreview extends JLabel implements PropertyChangeListener
{
	protected File fileToShow_;
	protected boolean showPreview_ = false;
	protected ShapeIcon icon_;

	protected static Pattern svgFileRegExp = Pattern.compile("(?i).*\\.svg");

	public SVGFilePreview(JFileChooser chooser)
	{
		icon_ = new ShapeIcon(Collections.EMPTY_LIST);
		chooser.addPropertyChangeListener(this);
		chooser.setAccessory(this);
		Font f = getFont();
		setFont(f.deriveFont(Font.ITALIC, f.getSize2D() * 2));
		setForeground(Color.GRAY);
	}

	/**
	 * Listens
	 *
	 * @param e A PropertyChangeEvent object describing the event source
	 *          and the property that has changed.
	 */
	public void propertyChange(PropertyChangeEvent e)
	{
		final String propertyName = e.getPropertyName();
		showPreview_ = false;
		//If the directory changed, don't show an image.
		if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(propertyName))
		{
			File newFile = (File) e.getNewValue();
			if (newFile != null && svgFileRegExp.matcher(newFile.getAbsolutePath())
												.matches())
			{
				if (Objects.equals(fileToShow_, newFile))
				{
					showPreview_ = true;
				}
				else
				{
					fileToShow_ = newFile;
					long start = System.currentTimeMillis();
					try (FileInputStream fis = new FileInputStream(fileToShow_))
					{
						Dimension d = getSize();
						SVGConverter svg = new SVGConverter(new BufferedInputStream(fis));
						icon_.setShapes(svg.getShapes());
						icon_.setScale(1, 1);
						Rectangle2D fileArea = icon_.getPainter()
													.getArea();
						double scale = Math.min(
								d.height / fileArea.getHeight(),
								d.width / fileArea.getWidth()
						);
						icon_.setScale(scale, scale);
						showPreview_ = true;
						long end = System.currentTimeMillis();
						System.out.println(String.format("Loading %s took %dms",
								fileToShow_.getName(), (end - start)));
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
		if (showPreview_)
		{
			setIcon(icon_);
			setText(null);
		}
		else
		{
			setIcon(null);
			setText("No Preview");
		}
	}

}
