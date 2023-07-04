package org.eclipse.transformer.action.impl;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.transformer.TransformException;
import org.eclipse.transformer.action.ActionContext;
import org.eclipse.transformer.action.ActionType;
import org.eclipse.transformer.action.ByteData;
import org.apache.maven.model.Model;
import org.eclipse.transformer.action.SignatureRule;

import javax.jws.WebParam.Mode;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PomActionImpl extends ElementActionImpl {
	public PomActionImpl(ActionContext context) {
		super(context);
		List<StringReplacement> replacements = createActiveReplacements(getSignatureRule());
		this.activeReplacements = replacements.isEmpty() ? NO_ACTIVE_REPLACEMENTS : replacements;
	}

	private final List<StringReplacement> activeReplacements;

	@Override
	protected List<StringReplacement> getActiveReplacements() {
		return activeReplacements;
	}

	@Override
	public ActionType getActionType() {
		return ActionType.POM;
	}

	@Override
	public boolean acceptResource(String resourceName, File resourceFile) {
		return resourceName.endsWith("pom.xml");
	}

	protected List<StringReplacement> createActiveReplacements(SignatureRule signatureRule) {
		List<StringReplacement> replacements = new ArrayList<>();

		if ( !signatureRule.getPackageRenames().isEmpty() ) {
			replacements.add(this::packagesUpdate);
		}

		return replacements;
	}

	@Override
	public ByteData apply(ByteData inputData) throws TransformException {
		startRecording(inputData);
		Model model;
		ByteDataImpl outputData;
		MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
		try {
			model = mavenXpp3Reader.read(inputData.reader());

			transformDependency(model,inputData.name());

			MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			mavenXpp3Writer.write(byteArrayOutputStream, model);
			ByteBuffer byteBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
			outputData = new ByteDataImpl(inputData.name(), byteBuffer, inputData.charset());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (XmlPullParserException e) {
			throw new RuntimeException(e);
		}

		stopRecording(inputData);
		return outputData;
	}

	private void transformDependency(Model model, String inputName) {
		Map<Model, Model> dependenciesMap = getSignatureRule().getPomUpdates().get("dependencies");
		model.getDependencies().forEach((dependency -> {
			boolean hasChanged = false;
			for (Entry<Model, Model> entry: dependenciesMap.entrySet()) {
				Dependency currentKey = entry.getKey().getDependencies().get(0);
				Dependency currentValue = entry.getValue().getDependencies().get(0);
				if (currentKey.getGroupId().equals(dependency.getGroupId()) && currentKey.getArtifactId().equals(dependency.getArtifactId())
					&& currentKey.getVersion().equals(dependency.getVersion())) {
					dependency.setGroupId(currentValue.getGroupId());
					dependency.setArtifactId(currentValue.getArtifactId());
					dependency.setVersion(currentValue.getVersion());
					hasChanged = true;
					addReplacement();
					break;
				}
			}

			if (hasChanged) {
				return;
			}
			String newValue = updateString(inputName, "PomGroupId", dependency.getGroupId());
			if (newValue != null) {
				dependency.setGroupId(newValue);
				addReplacement();
			}
		}));
	}
}
