package org.eclipse.transformer;

public enum PomType {
	DEPENDENCIES("dependencies"),
	MODULES("modules"),

	NONE("none"),
	;

	private final String name;

	PomType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static PomType getByName(String name) {
		for (PomType pomType: PomType.values()) {
			if (pomType.getName().equals(name)) {
				return pomType;
			}
		}
		return PomType.NONE;
	}
}
