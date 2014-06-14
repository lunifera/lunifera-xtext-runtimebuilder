package org.lunifera.dsl.xtext.builder.participant.xbase.impl;

import org.eclipse.xtext.xbase.XbaseStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

@SuppressWarnings("restriction")
public class XbaseBundleSpaceStandaloneSetup extends XbaseStandaloneSetup {

	public Injector createInjector() {
		return Guice.createInjector(new XbaseBundleSpaceRuntimeModule());
	}
}
