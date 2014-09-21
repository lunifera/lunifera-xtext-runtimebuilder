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

import org.lunifera.dsl.xtext.types.bundles.BundleSpace;
import org.lunifera.xtext.builder.types.loader.api.ITypeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class TypeLoader implements ITypeLoader {

	private static final Logger logger = LoggerFactory
			.getLogger(TypeLoader.class);

	private final BundleSpace bundleSpace;

	/**
	 * @noreference This constructor is not intended to be referenced by
	 *              clients.
	 */
	public TypeLoader(BundleSpace bundleSpace) {
		if (bundleSpace == null)
			throw new IllegalArgumentException("bundleSpace must not be null");
		this.bundleSpace = bundleSpace;
	}

	public Class<?> findTypeByName(String fullyQualifiedName) {
		if (fullyQualifiedName == null || fullyQualifiedName.equals("")) {
			return null;
		}

		try {
			return bundleSpace.forName(fullyQualifiedName);
		} catch (ClassNotFoundException e) {
			logger.error("{}", e);
		}
		return null;
	}

	@Override
	public void dispose() {
	}
}
