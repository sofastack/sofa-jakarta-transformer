/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.transformer.action.impl;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.transformer.PomType;
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
import java.util.*;
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

		return resourceName.endsWith("pom.xml") || resourceName.endsWith(".pom");
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

			transformModuleGAV(model);

			transformDependency(model,inputData.name());

			MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			mavenXpp3Writer.write(byteArrayOutputStream, model);

			ByteBuffer byteBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
			outputData = new ByteDataImpl(inputData.name(), byteBuffer, inputData.charset());
		} catch (IOException | XmlPullParserException e) {
			throw new TransformException("Failed to parse pom.xml [ " + inputData.name() + " ]", e);
		}

		stopRecording(inputData);
		return outputData;
	}

	private void transformDependency(Model model, String inputName) {
		Map<Model, Model> dependenciesMap = getSignatureRule().getPomUpdates().get(PomType.DEPENDENCIES.getName());
		model.getDependencies().forEach((dependency -> {
			boolean hasChanged = false;
			for (Entry<Model, Model> entry: dependenciesMap.entrySet()) {
				Dependency currentKey = entry.getKey().getDependencies().get(0);
				Dependency currentValue = entry.getValue().getDependencies().get(0);
				if (currentKey.getGroupId().equals(dependency.getGroupId()) &&
					currentKey.getArtifactId().equals(dependency.getArtifactId())) {
					dependency.setGroupId(currentValue.getGroupId() == null ? dependency.getGroupId() :
						currentValue.getGroupId());
					dependency.setArtifactId(currentValue.getArtifactId() == null ? dependency.getArtifactId() :
						currentValue.getArtifactId());
					dependency.setVersion(currentValue.getVersion() == null ? dependency.getVersion() :
						currentValue.getVersion());
					hasChanged = true;
					addReplacement();
					break;
				}
			}
			// If the dependencies have been transformed by custom pom rules, just return.
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

	private void transformModuleGAV(Model model) {
		Map<Model, Model> modulesMap = getSignatureRule().getPomUpdates().get(PomType.MODULES.getName());
		for (Entry<Model, Model> entry: modulesMap.entrySet()) {
			if (entry.getKey().getArtifactId().equals(model.getArtifactId())) {
				model.setGroupId(entry.getValue().getGroupId() == null ? model.getGroupId() :
					entry.getValue().getGroupId());
				model.setArtifactId(entry.getValue().getArtifactId() == null ? model.getArtifactId() :
					entry.getValue().getArtifactId());
				model.setVersion(entry.getValue().getVersion() == null ? model.getVersion() :
					entry.getValue().getVersion());
				addReplacement();
				break;
			}
		}
	}
}
