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
package org.lunifera.xtext.builder.ui.access.impl;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.containers.JavaProjectsStateHelper;
import org.eclipse.xtext.ui.resource.XtextResourceSetProvider;
import org.eclipse.xtext.ui.shared.Access;
import org.eclipse.xtext.util.Pair;
import org.lunifera.xtext.builder.ui.access.IXtextUtilService;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoader;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoaderFactory;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoaderProvider;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.google.inject.Guice;
import com.google.inject.Injector;

@SuppressWarnings({ "unused", "restriction" })
@Component(service = { IXtextUtilService.class })
public class XtextUtilService implements IXtextUtilService {

	private ComponentContext context;
	private Injector injector;
	private IResourceDescriptions resourceDescriptions;
	private JavaProjectsStateHelper javaStateHelper;
	private XtextResourceSetProvider resourceSetProvider;
	private IJdtTypeLoaderProvider typeLoaderProvider;
	private IJdtTypeLoaderFactory typeLoaderFactory;

	public XtextUtilService() {

	}

	@Activate
	protected void activate(ComponentContext context) {
		this.context = context;

		injector = Guice.createInjector(new UiModule());
		resourceDescriptions = Access.getIResourceDescriptions().get();
		javaStateHelper = injector.getInstance(JavaProjectsStateHelper.class);
		resourceSetProvider = injector
				.getInstance(XtextResourceSetProvider.class);
		typeLoaderProvider = injector.getInstance(IJdtTypeLoaderProvider.class);
		typeLoaderFactory = injector.getInstance(IJdtTypeLoaderFactory.class);
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		this.context = null;
		injector = null;
	}

	@Override
	public IProject getProject(String qualifiedName) {
		IEObjectDescription temp = null;
		for (IEObjectDescription desc : getEObjectDescriptionsForJvmTypes(qualifiedName)) {
			temp = desc;
			break;
		}
		IProject project = getProject(temp);
		return project;
	}

	@Override
	public IProject getProject(EClass eClass, String qualifiedName) {
		IEObjectDescription temp = null;
		for (IEObjectDescription desc : getEObjectDescriptions(eClass,
				qualifiedName)) {
			temp = desc;
			break;
		}
		IProject project = getProject(temp);
		return project;
	}

	public IProject getProject(IEObjectDescription desc) {
		IProject project = null;
		if (desc != null) {
			Iterable<Pair<IStorage, IProject>> storages = javaStateHelper
					.getMapper().getStorages(
							desc.getEObjectURI().trimFragment());

			for (Pair<IStorage, IProject> pair : storages) {
				project = pair.getSecond();
				break;
			}
		}
		return project;
	}

	@Override
	public XtextResourceSet getConfiguredXtextResourceFor(EClass eClass,
			String qualifiedName) {
		IEObjectDescription firstDesc = null;
		for (IEObjectDescription desc : getEObjectDescriptions(eClass,
				qualifiedName)) {
			firstDesc = desc;
			break;
		}

		XtextResourceSet resourceSet = null;
		if (firstDesc != null) {
			IProject project = getProject(firstDesc);
			if (project != null) {
				resourceSet = (XtextResourceSet) resourceSetProvider
						.get(project);
				resourceSet.getResource(firstDesc.getEObjectURI()
						.trimFragment(), true);
			}
		}
		return resourceSet;
	}

	public Iterable<IEObjectDescription> getEObjectDescriptions(EClass eClass,
			String qualifiedName) {
		Iterable<IEObjectDescription> result = resourceDescriptions
				.getExportedObjects(eClass,
						QualifiedName.create(qualifiedName.split("\\.")), true);
		return result;
	}

	private Iterable<IEObjectDescription> getEObjectDescriptionsForJvmTypes(
			String qualifiedName) {
		Iterable<IEObjectDescription> result = resourceDescriptions
				.getExportedObjects(TypesPackage.Literals.JVM_TYPE,
						QualifiedName.create(qualifiedName.split("\\.")), true);
		return result;
	}

	public Class<?> loadClass(String qualifiedName) {
		IEObjectDescription firstDesc = null;
		for (IEObjectDescription desc : getEObjectDescriptionsForJvmTypes(qualifiedName)) {
			firstDesc = desc;
			break;
		}

		if (firstDesc != null) {
			IProject project = getProject(firstDesc);
			IJdtTypeLoader loader = typeLoaderProvider.get(JavaCore
					.create(project));
			return loader.findTypeByName(qualifiedName);
		}
		return null;
	}

	public Class<?> reloadClass(String qualifiedName) {
		IEObjectDescription firstDesc = null;
		for (IEObjectDescription desc : getEObjectDescriptionsForJvmTypes(qualifiedName)) {
			firstDesc = desc;
			break;
		}

		if (firstDesc != null) {
			IProject project = getProject(firstDesc);
			IJdtTypeLoader loader = typeLoaderFactory
					.createJdtTypeLoader(JavaCore.create(project));
			return loader.findTypeByName(qualifiedName);
		}
		return null;
	}
}
