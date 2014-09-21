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
package org.lunifera.xtext.builder.types.loader.runtime;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.lunifera.dsl.xtext.types.bundles.BundleSpace;
import org.lunifera.xtext.builder.types.loader.api.ITypeLoader;
import org.lunifera.xtext.builder.types.loader.api.ITypeLoaderFactory;

import com.google.inject.Singleton;

@SuppressWarnings("restriction")
@Singleton
public class TypeLoaderFactory implements ITypeLoaderFactory {

	public ITypeLoader createTypeLoader(ResourceSet resourceSet) {
		if (resourceSet == null)
			throw new IllegalArgumentException("resourceSet may not be null.");

		XtextResourceSet xtextRS = (XtextResourceSet) resourceSet;
		Object context = xtextRS.getClasspathURIContext();
		if (!(context instanceof BundleSpace)) {
			throw new IllegalArgumentException(
					"No bundlespace available to load classes.");
		}

		return new TypeLoader((BundleSpace) context);
	}

}
