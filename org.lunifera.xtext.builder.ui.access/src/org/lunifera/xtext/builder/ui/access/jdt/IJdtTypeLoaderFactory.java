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
package org.lunifera.xtext.builder.ui.access.jdt;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.lunifera.xtext.builder.types.loader.api.ITypeLoaderFactory;

@SuppressWarnings("restriction")
public interface IJdtTypeLoaderFactory extends ITypeLoaderFactory {

	IJdtTypeLoader createJdtTypeLoader(ResourceSet resourceSet);

	IJdtTypeLoader createJdtTypeLoader(IJavaProject javaProject);

}
