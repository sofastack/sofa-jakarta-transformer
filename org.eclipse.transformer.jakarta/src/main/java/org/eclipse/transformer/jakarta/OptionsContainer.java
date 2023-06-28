package org.eclipse.transformer.jakarta;

import java.net.URL;
import java.util.Map;
import java.util.function.Function;

public interface OptionsContainer {

	Function<String, URL> getRuleLoader();

	Map<String, String> getOptionDefaults();
}
