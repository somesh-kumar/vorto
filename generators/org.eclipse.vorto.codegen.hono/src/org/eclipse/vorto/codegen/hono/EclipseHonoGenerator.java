/**
 * Copyright (c) 2015-2016 Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Bosch Software Innovations GmbH - Please refer to git log
 */
package org.eclipse.vorto.codegen.hono;

import org.eclipse.vorto.codegen.api.ChainedCodeGeneratorTask;
import org.eclipse.vorto.codegen.api.GenerationResultZip;
import org.eclipse.vorto.codegen.api.GeneratorTaskFromFileTemplate;
import org.eclipse.vorto.codegen.api.IGeneratedWriter;
import org.eclipse.vorto.codegen.api.IGenerationResult;
import org.eclipse.vorto.codegen.api.IVortoCodeGenProgressMonitor;
import org.eclipse.vorto.codegen.api.IVortoCodeGenerator;
import org.eclipse.vorto.codegen.api.InvocationContext;
import org.eclipse.vorto.codegen.api.VortoCodeGeneratorException;
import org.eclipse.vorto.codegen.arduino.ArduinoCodeGenerator;
import org.eclipse.vorto.codegen.hono.model.FunctionblockTemplate;
import org.eclipse.vorto.codegen.hono.model.InformationModelTemplate;
import org.eclipse.vorto.codegen.hono.model.JavaClassGeneratorTask;
import org.eclipse.vorto.codegen.hono.model.JavaEnumGeneratorTask;
import org.eclipse.vorto.codegen.hono.service.IDataServiceTemplate;
import org.eclipse.vorto.codegen.hono.service.hono.HonoDataService;
import org.eclipse.vorto.codegen.hono.service.hono.HonoMqttClientTemplate;
import org.eclipse.vorto.codegen.mqtt.python.PythonGenerator;
import org.eclipse.vorto.codegen.utils.GenerationResultBuilder;
import org.eclipse.vorto.codegen.utils.Utils;
import org.eclipse.vorto.core.api.model.datatype.Entity;
import org.eclipse.vorto.core.api.model.datatype.Enum;
import org.eclipse.vorto.core.api.model.functionblock.FunctionBlock;
import org.eclipse.vorto.core.api.model.informationmodel.FunctionblockProperty;
import org.eclipse.vorto.core.api.model.informationmodel.InformationModel;

/**
 * Generates source code for various device platforms that sends a JSON to the Hono MQTT Connector. The
 * data is compliant to a Vorto & Ditto format.
 *
 */
public class EclipseHonoGenerator implements IVortoCodeGenerator {

	@Override
	public IGenerationResult generate(InformationModel model, InvocationContext context,
			IVortoCodeGenProgressMonitor monitor) throws VortoCodeGeneratorException {
		GenerationResultZip output = new GenerationResultZip(model, getServiceKey());

		GenerationResultBuilder result = GenerationResultBuilder.from(output);

		String platform = context.getConfigurationProperties().getOrDefault("language","java");
		if (platform.equalsIgnoreCase("arduino")) {
			result.append(generateArduino(model, context, monitor));
		} else if (platform.equalsIgnoreCase("python")) {
			result.append(generatePython(model, context, monitor));
		} else {
			result.append(generateJava(model, context, monitor));
		}

		return output;
	}

	private IGenerationResult generateJava(InformationModel infomodel, InvocationContext context,
			IVortoCodeGenProgressMonitor monitor) {
		GenerationResultZip output = new GenerationResultZip(infomodel, getServiceKey());
		ChainedCodeGeneratorTask<InformationModel> generator = new ChainedCodeGeneratorTask<InformationModel>();

		generator.addTask(new GeneratorTaskFromFileTemplate<InformationModel>(new PomFileTemplate()));
		generator.addTask(new GeneratorTaskFromFileTemplate<InformationModel>(new Log4jTemplate()));
		generator.addTask(new GeneratorTaskFromFileTemplate<InformationModel>(new CertificateTemplate()));

		generator.addTask(new GeneratorTaskFromFileTemplate<InformationModel>(new AppTemplate()));
		generator.addTask(new GeneratorTaskFromFileTemplate<InformationModel>(new IDataServiceTemplate()));
		generator.addTask(new GeneratorTaskFromFileTemplate<InformationModel>(new HonoDataService()));
		generator.addTask(new GeneratorTaskFromFileTemplate<InformationModel>(new HonoMqttClientTemplate()));
		generator.addTask(new GeneratorTaskFromFileTemplate<InformationModel>(new InformationModelTemplate()));

		generator.generate(infomodel, context, output);

		for (FunctionblockProperty fbProperty : infomodel.getProperties()) {
			new GeneratorTaskFromFileTemplate<>(new FunctionblockTemplate(infomodel)).generate(fbProperty.getType(),
					context, output);
			
			FunctionBlock fb = fbProperty.getType().getFunctionblock();
			
			for (Entity entity : Utils.getReferencedEntities(fb)) {
				generateForEntity(infomodel, entity, output);
			}
			for (Enum en : Utils.getReferencedEnums(fb)) {
				generateForEnum(infomodel, en, output);
			}
		}

		return output;
	}

	private IGenerationResult generatePython(InformationModel infomodel, InvocationContext context,
			IVortoCodeGenProgressMonitor monitor) throws VortoCodeGeneratorException {
		PythonGenerator pythonGenerator = new PythonGenerator();
		return pythonGenerator.generate(infomodel,context,monitor);
	}

	private IGenerationResult generateArduino(InformationModel infomodel, InvocationContext context,
			IVortoCodeGenProgressMonitor monitor) throws VortoCodeGeneratorException {
		ArduinoCodeGenerator arduinoGenerator = new ArduinoCodeGenerator();
		return arduinoGenerator.generate(infomodel,context,monitor);
	}
	
	private void generateForEntity(InformationModel infomodel, Entity entity, IGeneratedWriter outputter) {
		new JavaClassGeneratorTask(infomodel).generate(entity, null, outputter);
	}

	private void generateForEnum(InformationModel infomodel, Enum en, IGeneratedWriter outputter) {
		new JavaEnumGeneratorTask(infomodel).generate(en,null, outputter);
				
	}

	@Override
	public String getServiceKey() {
		return "eclipsehono";
	}
}
