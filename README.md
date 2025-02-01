# jSVG <a href="http://www.w3.org/Graphics/SVG/"><img src="doc/svg-logo-v.svg" alt="W3C SVG Logo"/></a>
Lightweight SVG rendering to use in Java UI Widgets. 

Some re-usable widgets and examples are provided for Swing. 
However, since the base technology behind it is Java2D, it should work with
any framework that uses Java Graphics2D. 

In SWT it can be used... but only via some Java2D integration. 
SWT does not directly support Java2D and the SWT graphics system itself lacks some necessary basic functions.

### Usage

The library is available as maven artifact.

#### Maven
```xml
<dependency>
 <groupId>io.github.bweng20</groupId>
 <artifactId>jSVG</artifactId>
 <version>1.5</version>
</dependency>
```

#### Gradle
```
implementation 'io.github.bweng20:jSVG:1.5'
```

The artifact has different classifier.

| Classifier   | Description                                                                 |
|--------------|-----------------------------------------------------------------------------|
| *none*       | The default jar contains the minimalistic implementation of the renderer.   |
| allFeatures  | Contains the renderer with all features turned on.                          |
| examples     | Contains the demos and tools. See "_Included Tools & Demos_" below.                                 |
| javadoc      | Contains the documentation, downloaded by most IDE automatically if needed. |
| sources      | Contains the sources, downloaded by most IDE automatically if needed.       |

For programmatic usage see sources below [Examples](src/main/java/com/bw/jtools/examples) 


### Motivation

Most Icons used are based on bitmap images that doesn't scale well 
on __HiDPI displays__. Most web applications use SVG for icons as they 
can be scaled without loss in quality.

Some Java apps use Apache Batik to do the same, but Batik is hugh.
For bigger applications with interactivity with the SVG elements, batik is the best option.
I had very good projects using Batik. It's still a great software. But I personally doesn't want to include it "only" to render icons.

It is also easy to create __gauge like widgets__ with SVG - as with JavaFX. 
Elements in SVG can be addressed via their id and animated (see Clock example below).

As the SVGs can be transformed in any ways, it is also easy to use them for __decorations__. E.g. to give a box some fancy frame.


### How it is done

__jSVG tries to convert SVG elements to Java2D-Shapes__. This reduces the supported features that can be used,
but the once converted shapes render incredible fast.

The resulting Shapes can be painted on any Graphics2d-instance. Additional style information is handled by a specific "Painter" that sets colors, strokes etc. to match the SVG definitions. The Java2D-shapes (and the Painter) are fully scablable, any transformation (rotation, scale, translate) can be used without losing quality. 

The Painter can be called multiple times with different transformation. So the SVG can be used as parts in bigger set-ups. 
E.g. as parts of a frame, for edge-decorations e.t.c.

As the real rendering is done by the Java2d-engine this is fast enough to paint large amount of SVG elements. 

If you need a feature-complete renderer, use
* Apache Batik
* SVG Salamander &rarr;https://github.com/blackears/svgSalamander
* JSVG &rarr;https://github.com/weisJ/jsvg

Specially jsvg - yes, same name, I discovered this too late, sorry - is nearly feature-complete.
But all of them a larger and render slower, which - I assume - is normal for real SVG-rendering.

### Usage in OpenIde/NetBeans
Class [com.bw.jtools.ui.OpenIdeSVGLoader](src/main/java/com/bw/jtools/ui/OpenIdeSVGLoader.java) implements &rarr;[org.openide.util.spi.SVGLoader](https://bits.netbeans.org/19/javadoc/org-openide-util-ui/index.html?org/openide/util/spi/SVGLoader.html) 
which is used in OpenIde classes to load SVG icons. E.g. in the fabulous &rarr;[org-netbeans-swing-outline](https://mvnrepository.com/artifact/org.netbeans.api/org-netbeans-swing-outline) component.

### SVG Examples

A pretty example for an SVG is the flag of San Marino. 
It can be completely converted to Java2D-shapes.

The source can be found here: &rarr;https://upload.wikimedia.org/wikipedia/commons/b/b1/Flag_of_San_Marino.svg

Converted and drawn with jSVG:<br>
![Example_Flag_of_San_Marino.png](doc%2FExample_Flag_of_San_Marino.png) ![Example_Flag_of_San_Marino.png](doc%2FExample_Flag_of_San_Marino_small.png)<br>
<sub>(not scalable) screenshots of the Painter in different scales </sub> 

The conversion results in 323 different "StyledShapes". Means each shape can also have a transformation, stroke, fill-paint and a clipping region. 
So the actual amount of Java2D-primitives is a bit higher.

On my 10 years old computer the shapes are drawn in ~20ms for both sizes.
This time goes up to ~50ms for bigger sizes like 1000x1000.

The San Marino flag is a bit to detailed to be used as icon.
SVGs for icons are normally much simpler, so expect less time to draw it.

For a collection of flags, that can be used for icons, check out
&rarr;https://github.com/hampusborgos/country-flags

Clone the repository, then start the icon tester and select the svg directories
to see how they look at buttons.


### (Not) Supported features

As said this is a lightweight SVG renderer, designed for High-Res icons.

Most complex stuff may not work. "css" is supported to some degree. "markers" or filters are not supported.

I will not give here a complete list of features that are supported or not. After each SVG-conference the list would be outdated.
If something doesn't work, try to use the functions of your SVG-editor to simplify your drawings.

#### Filters

Usages of pixel-based filters are in any case a bad idea, because they are silly expensive to compute.
The very basic filters as "Offset", "Blur" and "Merge" are implemented (somehow and only if [SVGConverter.experimentalFeaturesEnabled_](src/main/java/com/bw/jtools/svg/SVGConverter.java)  is set to true). But please don't use
them. Safe your computing power and the planet.

See the SVG example for filter-effects at W3.org &rarr;https://www.w3.org/TR/SVG11/filters.html#AnExample  

jSVG has _feGaussianBlur_ and _feOffset_, but doesn't have _feSpecularLighting_ or _feComposite_, so the output looks a bit simpler.

![w3_filter_example.png](doc%2Fw3_filter_example.png)

And some examples may not work at all. 

#### Clipping

Some SVG may use clip-paths to "cut" away parts for the shapes. 
Java2Ds "clip" doesn't support antialiasing for that (the so-called "soft clipping"). The clip-edges 
will get jagged.

The image below shows a rendered circle on the left and the result of a clip-path with the same circle on the right.  

![clip-path-antialiasing.png](doc%2Fclip-path-antialiasing.png)

You can use your SVG editor to create a normal path from the clipped result.

#### Animation

As SVG animations involve executing scripts, it is not supported. 
Clearly it's possible to execute Scripts with the Script-API, but this would be another project.

This project includes a tiny animation-framework that supports programmatic animations to support
animated widgets. See the Clock-example below. 

### A word about off-screen-images

Theoretically the SVG can be drawn to an off-screen-buffer in a fixed size
and then re-used. In __multi-display-scenarios this may fail__.
In newer Java version the app is automatically scaled and will change 
the draw-resolution on the fly if the user moves the app from on screen 
to another. Fixed bitmaps will get blurry.

If vector graphics are used for the drawing, this is no issue.

Nevertheless, ShapePainter has a method to export a BufferedImage that
can be used this way (and can also be written to file via ImageIO). 
The Screenshots here are produced this way.

### Included Tools & Demos

The project contains some tools to give examples and demonstrate some features. 
Specially of the painter-interface.

* [SVGViewer](src/main/java/com/bw/jtools/examples/SVGViewer.java)<br> 
  Shows one SVG file. The drawing can be rotated and scaled. 
  The app shows time statistics in the status bar.
 
  The app has also an option to paint the SVG "along a path" witch demonstrates [PaintAlongShapePainter](src/main/java/com/bw/jtools/shape/PaintAlongShapePainter.java).
  In Java2D the outline of each Shape can be iterated. With some maths it's easy to calculate
  the position and angle to draw some other shape "along" it. With the buttons you can control
  start-, end- and distance-offsets from the PaintAlongShapePainter-Interface.<br>
  All transformations on the original painter are kept, you can rotate and scale the source image in the main window until 
  size and orientation meet the required values.
  <br>
  ![PaintAlong_WithArrow.png](doc%2FPaintAlong_WithArrow.png)<br>
  ![PaintAlong_WithArrow_Rotated.png](doc%2FPaintAlong_WithArrow_Rotated.png)

* [SVGIconTester](src/main/java/com/bw/jtools/examples/SVGIconTester.java)<br>
  Shows multiple SVG files (you can select a directory) as Icons on Buttons.
  A press on the button will show the original svg in a separate window - using the same painter instance.<br>
  ![SVGIconTester_Demo.png](doc%2FSVGIconTester_Demo.png)<br>
  You can test different sizes<br>
  ![SVGIconTester_Demo64.png](doc%2FSVGIconTester_Demo64.png)<br>
  and the "disabled" mode (all colors will get gray).<br>
  ![SVGIconTester_DemoDisabled.png](doc%2FSVGIconTester_DemoDisabled.png)


* [SVGClock](src/main/java/com/bw/jtools/examples/SVGClock.java)<br>
  Demonstrates animations.<br>
  ![Clock_Demo.png](doc%2FClock_Demo.png)
  <br> You can get the used file from
  &rarr;https://commons.wikimedia.org/wiki/File:Swiss_railway_clock.svg
  In order to show "correct" time with such a clock, you must remove any rotation from the clock hand elements,
  so that the hands show “12:00:00”.<br>
  The animation simply rotates the elements and does not know the original orientation.
  This is a programmatic animation, NOT an scripted SVG animation. The animator-classes access sub-elements by id and modify 
  attributes of the shape (transformation-matrix or other). Not more.

  You have to select the matching elements from the combos and the rotation center (in the elements user-space).  



