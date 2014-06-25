package org.lunifera.xtext.builder.participant.dmodelsample.impl;

import org.eclipse.xtext.example.domainmodel.DomainmodelStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
