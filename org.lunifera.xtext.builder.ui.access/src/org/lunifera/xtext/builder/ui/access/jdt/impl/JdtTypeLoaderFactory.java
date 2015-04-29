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
package org.lunifera.xtext.builder.ui.access.jdt.impl;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.xtext.common.types.access.jdt.IJavaProjectProvider;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoader;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoaderFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class JdtTypeLoaderFactory implements IJdtTypeLoaderFactory {

	@Inject
	private IJavaProjectProvider javaProjectProvider;

	public IJdtTypeLoader createJdtTypeLoader(ResourceSet resourceSet) {
		if (resourceSet == null)
			throw new IllegalArgumentException("resourceSet may not be null.");
		IJavaProject javaProject = javaProjectProvider
				.getJavaProject(resourceSet);
		IJdtTypeLoader result = createJdtTypeLoader(javaProject);
		return result;
	}

	public IJdtTypeLoader createJdtTypeLoader(IJavaProject javaProject) {
		return new JdtTypeLoader(javaProject);
	}

	@Override
	public IJdtTypeLoader createTypeLoader(ResourceSet resourceSet) {
		return createJdtTypeLoader(resourceSet);
	}

}
