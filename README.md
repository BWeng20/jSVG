# jSVG <a href="http://www.w3.org/Graphics/SVG/" ><img src="doc/svg-logo-v.svg" alt="W3C SVG Logo" style="height:1.5em;float:right;"/></a>
Lightweight SVG rendering to use in SWT Components.

Most Icons used are based on bitmap images that doesn't scale well 
on HiDPI displays. Most web applications use SVG for icons as they 
can be scaled without loss in quality.

Some Java apps use Apache Batik to do the same, but Batik is hugh.
For bigger applications with interactivity in the SVG elements, batik is the best option.
I had very good projects using Batik. It's a great software. But I personally doesn't want to include it "only" to render icons.

jSVG tries to convert SVG elements to Java2D-Shapes. This reduces the features that can be used
a bit, but the once converted shapes render incredible fast.

If you need a feature-complete renderer, use Apache Batik or (also not complete) SVG Salamander.

The resulting Shapes can be painted on any Graphics2d-instance. Additional style information is handled by a specific "Painter" that sets colors, strokes etc. to match the SVG definitions. The Java2D-shapes (and the Painter) are fully scablable, any transformation (rotation, scale, translate) can be used without losing quality. 

The Painter can be called multiple times with different transformation. So the SVG can be used as parts in bigger set-ups. 
E.g. as parts of a frame, for edge-decorations e.t.c.

As the real rendering is done by the Java2d-engine this is fast enough to paint large amount of SVG elements. 

### Example

A pretty example for an SVG is the flag of San Marino. 
It can be completely converted to Java2D-shapes.

The source can be found here: https://upload.wikimedia.org/wikipedia/commons/b/b1/Flag_of_San_Marino.svg

Converted and drawn with jSVG:<br>
![Example_Flag_of_San_Marino.png](doc%2FExample_Flag_of_San_Marino.png) ![Example_Flag_of_San_Marino.png](doc%2FExample_Flag_of_San_Marino_small.png)<br>
<sub>(not scalable) screenshots of the Painter in different scales </sub> 

The conversion results in 323 different "StyledShapes". Means each shape can also have a transformation, stroke, fill-paint and a clipping region. 
So the actual amount of Java2D-primitives is a bit higher.

On my 10 years old computer the shapes are drawn in ~20ms for both sizes.
This time goes up to ~50ms for bigger sizes like 1000x1000.

The San Mariona flag is a bit to detailed to be used as icon.
SVGs for icons are normally much simpler, so expect less time to draw it.

For a collection of flags, that can be used for icons, check out 
https://github.com/hampusborgos/country-flags

Clone one the repository, then start the icon tester and select the svg directories
to see how they look at buttons.



### Supported features

As said this is a lightweight SVG renderer, designed for High-Res icons.

Most complex stuff may not work. "css" is supported to some degree. "markers" or the complex filters are not supported.<br>
Usages of pixel-based filters are in any case a bad idea, because they are silly expensive to compute.
The very basic filters as "Offset", "Blur" and "Merge" are implemented (somehow) but please don't use
them. Safe your computing power and the planet.

See the SVG example for filter-effects at W3.org https://www.w3.org/TR/SVG11/filters.html#AnExample  

jSVG has _feGaussianBlur_ and _feOffset_, but doesn't have _feSpecularLighting_ or _feComposite_, so the output looks a bit simpler.

![w3_filter_example.png](doc%2Fw3_filter_example.png)

Some examples may not work at all. 

I will not give here a complete list of features that are supported or not. After each SVG-conference the list would be outdated (these guys have fun!). 
If something doesn't work, please use the functions of your SVG-editor to simplify your drawings.

### A word about off-screen-images

Theoretically such SVG can be drawn to an off-screen-buffer in a fixed size
and then re-used. In __multi-display-scenarios this may fail__.
In newer Java version the app is automatically scaled and will change 
the draw-resolution on the fly if the user moves the app from on screen 
to another. Fixed bitmaps will get blurry.

If vector graphics are used for the drawing, this is no issue.

Nevertheless, ShapePainter has a method to export a BufferedImage that
can be used this way (and can also be written to file via ImageIO). 
The Screenshots here are produced this way.  


