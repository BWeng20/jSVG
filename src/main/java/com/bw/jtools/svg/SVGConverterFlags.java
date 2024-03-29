package com.bw.jtools.svg;

/**
 * <b>Compile-time</b> flags to control supported features.
 * If changed, a re-build of the project is needed.
 */
public interface SVGConverterFlags
{
	/**
	 * If true the calculated segments of shapes are added for debugging.
	 *
	 * @see ShapeHelper#getSegmentPath()
	 */
	boolean addPathSegments_ = false;

	/**
	 * If true details error information is printed. E.g. the stacktrace.
	 *
	 * @see SVGConverter#error(Throwable, String, Object...)
	 */
	boolean detailedErrorInformation_ = false;

	/**
	 * If true experimental features are enabled.
	 */
	boolean experimentalFeaturesEnabled_ = false;

	/**
	 * If true namespaces needed to be used correctly. Otherwise, namespaces are ignored.
	 */
	boolean namespaceAware_ = false;

	/**
	 * If enabled, attribute "requiredFeatures" is supported (with SVG1.1 feature strings).
	 */
	boolean requiredFeaturesEnabled_ = false;

	/**
	 * If enabled, attribute "requiredExtensions" is supported.
	 */
	boolean requiredExtensionsEnabled_ = false;

	/**
	 * If enabled, attribute "systemLanguage" is supported.
	 */
	boolean systemLanguageEnabled_ = false;

	/**
	 * If enabled, switch tag is enabled.
	 * Make sonly sense if "requiredFeatures" or "systemLanguage" is also enabled.
	 */
	boolean switchEnabled_ = true && (requiredFeaturesEnabled_ || systemLanguageEnabled_);


}
