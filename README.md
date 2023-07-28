# jSVG
Lightweight SVG rendering to use in SWT Components.

Most Icons used are based on bitmap images that doesn't scale well 
on HiDPI displays. Most web applications use SVG for icons as they 
can be scaled without loss in quality.

Some Java apps use Apache Batik to do the same, but Batik is hugh.
For bigger applications with interactivity in the SVG elements, batik is the best option.
But I personally doesn't want to include it "only" to render icons.

jSVG tries to convert SVG elements to Java2D-Shapes. This reduced the features that can be used
a bit, but the once converted shapes render incredible fast.
If you need a feature-complete renderer, use batik or (also not complete) SVG Salamander.

The resulting Shapes can be painted on any Graphics2d-instance. Additional style information is handled by a specific "Painter" that sets colors, strokes etc. to match the SVG definitions. The Java2D-shapes (and the Painter) are fully scablable, any transformation (rotation, scale, translate) can be used without losing quality. 

The Painter can be called multiple times with different transformation. So the SVG can be used as parts in bigger set-ups. 
E.g. as parts of a frame, for edge-decorations e.t.c.

As the real rendering is done by the Java2d-engine this is fast enough to paint large amount of SVG elements. 

### Example

A pretty example for a SVG is the flag of San Marino. 
It uses clipping-regions and other nasty stuff but can be 
completely converted to Java2D-shapes.

The source can be found here: https://upload.wikimedia.org/wikipedia/commons/b/b1/Flag_of_San_Marino.svg

Converted and drawn with jSVG:<br>
![Example_Flag_of_San_Marino.png](doc%2FExample_Flag_of_San_Marino.png) ![Example_Flag_of_San_Marino.png](doc%2FExample_Flag_of_San_Marino_small.png)<br>
<sub>(not scalable) screenshots of the Painter in different scales </sub> 

The conversion results in 323 different "StyledShapes". Means each shape can also have a transformation, stroke, fill-paint and a clipping region. 
So the actual amount of Java2D-primitives is a bit higher.

On my 10 years old computer the shapes are drawn in ~20ms for both sizes.
This time goes up to ~50ms for bigger sizes like 1000x1000.

SVGs for icons are normally much simpler, so expect less time to draw it.

### A word about off-screen-images

Theoretically such SVG can be drawn to an off-screen-buffer in a fixed size
and then re-used. In __multi-display-scenarios this may fail__.
In newer Java version the app is automatically scaled and will change 
the draw-resolution on the fly if the user moves the app from on screen 
to another. Fixed bitmaps will get blurry.

Using vector graphics for the actual drawing has no issue with it. 

