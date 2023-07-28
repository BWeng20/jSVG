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


