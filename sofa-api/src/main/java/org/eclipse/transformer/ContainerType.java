package org.eclipse.transformer;

import org.eclipse.transformer.jakarta.JakartaOptionsContainer;
import org.eclipse.transformer.jakarta.OptionsContainer;

public enum ContainerType {

	Jakarta(JakartaOptionsContainer.JAKARTA_OPTIONS_CONTAINER),
	None(null);

	private final OptionsContainer optionsContainer;

	public OptionsContainer getOptionsContainer() {
		return optionsContainer;
	}

	ContainerType(OptionsContainer optionsContainer) {
		this.optionsContainer = optionsContainer;
	}
}
