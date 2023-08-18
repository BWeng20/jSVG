## jSVG Advanced Usage

### Decorators

Java2D has the option to iterate alone the outline of shapes (see [java.awt.geom.PathIterator](https://docs.oracle.com/javase%2F7%2Fdocs%2Fapi%2F%2F/java/awt/geom/PathIterator.html)).

With ShapePainter you can draw the SVG "along" the outline of some other shape.

Simple Example:

This SVG ![Arrow.svg](Arrow.svg) is drawn along a rectangle:

To draw the SVG the base-position and the tangent angle along the outline is needed. Both values are calculated by the helper class 
[ShapeHelper.pointAtLength](../src/main/java/com/bw/jtools/svg/ShapeHelper.java).

