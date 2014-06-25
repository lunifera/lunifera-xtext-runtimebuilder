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
package org.lunifera.dsl.xtext.builder.participant.xbase.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.XtextPackage;
import org.eclipse.xtext.XtextStandaloneSetup;
import org.lunifera.dsl.xtext.builder.participant.xbase.IXbaseMetadataService;
import org.lunifera.xtext.builder.metadata.services.AbstractBuilderParticipant;
import org.lunifera.xtext.builder.metadata.services.IBuilderParticipant;
import org.lunifera.xtext.builder.metadata.services.IMetadataBuilderService;
import org.lunifera.xtext.builder.xbase.setups.XbaseWithAnnotationsBundleSpaceStandaloneSetup;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
@Component(service = { IBuilderParticipant.class })
public class XbaseBuilderParticipant extends AbstractBuilderParticipant {

	@Inject
	private IMetadataBuilderService metadataBuilderService;

	private ComponentContext context;
	private ServiceRegistration<IXbaseMetadataService> serviceRegister;

	public XbaseBuilderParticipant() {

	}

	@Activate
	protected void activate(ComponentContext context) {
		this.context = context;
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		metadataBuilderService.removeFromBundleSpace(context.getBundleContext()
				.getBundle());

		this.context = null;
	}

	@Override
	public List<URL> getModels(Bundle suspect) {

		List<URL> results = new ArrayList<URL>();
		BundleWiring wiring = suspect.adapt(BundleWiring.class);
		results.addAll(wiring.findEntries("/", "*.xtext",
				BundleWiring.LISTRESOURCES_RECURSE));
		results.addAll(wiring.findEntries("/", "*.___xtype",
				BundleWiring.LISTRESOURCES_RECURSE));
		results.addAll(wiring.findEntries("/", "*.___xbase",
				BundleWiring.LISTRESOURCES_RECURSE));
		results.addAll(wiring.findEntries("/", "*.___xbasewithannotations",
				BundleWiring.LISTRESOURCES_RECURSE));

		Set<String> fragments = new HashSet<String>();
		for (Iterator<URL> iterator = results.iterator(); iterator.hasNext();) {
			URL url = iterator.next();
			URI uri = URI.createURI(url.toString());
			if (fragments.contains(uri.lastSegment())) {
				iterator.remove();
			}
			fragments.add(uri.lastSegment());
		}

		return results;
	}

	@Override
	public void notifyLifecyle(LifecycleEvent event) {
		if (event.getState() == IBuilderParticipant.LifecycleEvent.INITIALIZE) {
			initialize();
		} else if (event.getState() == IBuilderParticipant.LifecycleEvent.ACTIVATED) {
			XBaseService xbaseService = new XBaseService();
			serviceRegister = context.getBundleContext().registerService(
					IXbaseMetadataService.class, xbaseService, null);
		} else {
			if (serviceRegister != null) {
				serviceRegister.unregister();
				serviceRegister = null;
			}

			if (metadataBuilderService != null) {
				metadataBuilderService.removeFromBundleSpace(context
						.getBundleContext().getBundle());
			}
		}
	}

	private void initialize() {
		XtextStandaloneSetup.doSetup();
		XbaseWithAnnotationsBundleSpaceStandaloneSetup.doSetup();
		metadataBuilderService.addToBundleSpace(context.getBundleContext()
				.getBundle());
	}

	/**
	 * Provided as an OSGi service to return {@link Grammar Grammars} for the
	 * given qualified name.
	 */
	private class XBaseService implements IXbaseMetadataService {

		@Override
		public Grammar getGrammar(String qualifedName) {
			return (Grammar) metadataBuilderService.getMetadata(qualifedName,
					XtextPackage.Literals.GRAMMAR);
		}

	}

}
