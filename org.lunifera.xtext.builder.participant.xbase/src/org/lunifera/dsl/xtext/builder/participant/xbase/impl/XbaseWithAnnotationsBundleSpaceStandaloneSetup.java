package org.lunifera.dsl.xtext.builder.participant.xbase.impl;

import org.eclipse.xtext.xbase.annotations.XbaseWithAnnotationsStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

@SuppressWarnings("restriction")
public class XbaseWithAnnotationsBundleSpaceStandaloneSetup extends
		XbaseWithAnnotationsStandaloneSetup {

	public Injector createInjectorAndDoEMFRegistration() {
		XbaseBundleSpaceStandaloneSetup.doSetup();

		Injector injector = createInjector();
		register(injector);
		return injector;
	}

	public Injector createInjector() {
		return Guice
				.createInjector(new XbaseWithAnnotationsBundleSpaceRuntimeModule());
	}
}
