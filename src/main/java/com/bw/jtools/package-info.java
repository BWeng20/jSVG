/**
 * The main use case for this library is drawing icons and simple graphics that look great even on high resolution screens.<br>
 * It contains a simple SVG Converter, that creates Java2D-shapes from the SVG source.<br>
 * As the svg elements are simply converted to shapes, complex stuff that needs offline-rendering (like feSpecularLighting) doesn't work.
 * A lot of complex use-case will not work as specified. <br>
 * The SVG specification contains a lot of such case with a large amounts of hints how agents should render it correctly.
 * But most svg graphics doesn't use such stuff, so the conversion to Java2D shapes is a efficient way to draw
 * such simple scalable graphics.<br>
 * If you need a feature-complete renderer, use Batik.
 */
package com.bw.jtools;

