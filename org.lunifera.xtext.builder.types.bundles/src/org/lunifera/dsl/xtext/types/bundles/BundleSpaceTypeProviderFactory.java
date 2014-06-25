/**
 * Copyright (c) 2011 - 2014, Lunifera GmbH (Gross Enzersdorf), Loetz KG (Heidelberg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * 		Florian Pirchner - Initial implementation
 * 
 * Based on Xtext org.eclipse.xtext.common.types.access.reflect.ReflectionTypeProviderFactory
 */
package org.lunifera.dsl.xtext.types.bundles;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.common.types.access.AbstractTypeProviderFactory;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class BundleSpaceTypeProviderFactory extends AbstractTypeProviderFactory {

	@Inject
	private BundleSpaceResourceSetProvider bundleSpaceProvider;

	public BundleSpaceTypeProviderFactory() {
	}

	/**
	 * {@inheritDoc}
	 */
	public BundleSpaceTypeProvider createTypeProvider(ResourceSet resourceSet) {
		if (resourceSet == null) {
			throw new IllegalArgumentException("resourceSet may not be null.");
		}
		BundleSpaceTypeProvider result = createBundleSpaceTypeProvider(resourceSet);
		return result;
	}

	/**
	 * Creates the bundle space type provider.
	 * 
	 * @param resourceSet
	 * @return
	 */
	protected BundleSpaceTypeProvider createBundleSpaceTypeProvider(
			ResourceSet resourceSet) {
		return new BundleSpaceTypeProvider(
				bundleSpaceProvider.getBundleSpace(resourceSet), resourceSet,
				getIndexedJvmTypeAccess());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BundleSpaceTypeProvider createTypeProvider() {
		return (BundleSpaceTypeProvider) super.createTypeProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BundleSpaceTypeProvider findTypeProvider(ResourceSet resourceSet) {
		return (BundleSpaceTypeProvider) super.findTypeProvider(resourceSet);
	}

}
