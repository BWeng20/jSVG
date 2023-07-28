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

As the real rendering is done by the Java2d-engine this is fast enought to paint large amount of SVG elements. 
