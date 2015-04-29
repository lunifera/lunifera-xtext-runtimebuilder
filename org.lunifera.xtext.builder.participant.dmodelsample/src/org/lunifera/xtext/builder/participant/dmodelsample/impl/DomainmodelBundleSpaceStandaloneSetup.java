
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



package org.lunifera.xtext.builder.participant.dmodelsample.impl;

import org.eclipse.xtext.example.domainmodel.DomainmodelStandaloneSetup;
import org.lunifera.xtext.builder.xbase.setups.XbaseBundleSpaceStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

@SuppressWarnings("restriction")
public class DomainmodelBundleSpaceStandaloneSetup extends
		DomainmodelStandaloneSetup {

	public static void doSetup() {
		new DomainmodelBundleSpaceStandaloneSetup()
				.createInjectorAndDoEMFRegistration();
	}

	public Injector createInjector() {
		return Guice.createInjector(new DomainmodelBundleSpaceRuntimeModule());
	}

	public Injector createInjectorAndDoEMFRegistration() {
		XbaseBundleSpaceStandaloneSetup.doSetup();

		Injector injector = createInjector();
		register(injector);
		return injector;
	}
}
