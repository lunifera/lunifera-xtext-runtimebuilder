/**
 * Copyright (c) 2011 - 2015, Lunifera GmbH (Gross Enzersdorf), Loetz KG (Heidelberg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *         Florian Pirchner - Initial implementation
 */
package org.lunifera.xtext.builder.xbase.setups;

import org.eclipse.xtext.xbase.XbaseStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class XbaseBundleSpaceStandaloneSetup extends XbaseStandaloneSetup {

	public Injector createInjector() {
		return Guice.createInjector(new XbaseBundleSpaceRuntimeModule());
	}
}
