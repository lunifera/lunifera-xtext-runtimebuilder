/**
 * Copyright (c) 2011 - 2014, Lunifera GmbH (Gross Enzersdorf), Loetz KG (Heidelberg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * 		Florian Pirchner - Initial implementation
 */
package org.lunifera.xtext.builder.metadata.services;

import java.net.URL;
import java.util.List;

import org.osgi.framework.Bundle;

public abstract class AbstractBuilderParticipant implements IBuilderParticipant {

	@Override
	public List<URL> getModels(Bundle suspect) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void notifyLifecyle(LifecycleEvent event) {
		throw new UnsupportedOperationException();
	}

}
