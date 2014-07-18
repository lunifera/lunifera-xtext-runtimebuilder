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
package org.lunifera.xtext.builder.ui.access;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtext.resource.XtextResourceSet;

public interface IXtextUtilService {

	/**
	 * Returns the project for the given qualified name that contains the java
	 * class. Returns <code>null</code> if no project is involved.
	 * 
	 * @param eClass
	 * @param qualifiedName
	 * @return
	 */
	IProject getProject(String qualifiedName);

	/**
	 * Returns the project for the given eClass and qualified name that contains
	 * the xtext model. Returns <code>null</code> if no project is involved.
	 * 
	 * @param eClass
	 * @param qualifiedName
	 * @return
	 */
	IProject getProject(EClass eClass, String qualifiedName);

	/**
	 * Returns a fully configured XtextResourceSet for the uri defined by eClass
	 * and qualifiedName. It will hook up the java project from workspace and
	 * installs a properly configured IJdtTypeProvider.
	 * 
	 * @param eClass
	 * @param qualifiedName
	 * @return
	 */
	XtextResourceSet getConfiguredXtextResourceFor(EClass eClass,
			String qualifiedName);

	/**
	 * Loads the class for the given qualifiedName. Therefore the java project
	 * containing the java class is determined and the class is loaded from the
	 * workspace. To load the class an URLClassLoader is used. The parent
	 * classloader is the bundle classloader from
	 * org.lunifera.xtext.builder.ui.access. Since it defines a dynamic-import:
	 * "*" all classes contained in the running IDE are loaded properly.
	 * 
	 * @param qualifiedName
	 * @return
	 */
	Class<?> loadClass(String qualifiedName);

	/**
	 * See {@link #loadClass(String)}. The main difference is, that this method
	 * uses a newly prepared class loader.
	 * 
	 * @param qualifiedName
	 * @return
	 */
	Class<?> reloadClass(String qualifiedName);

	// /**
	// * Loads the class for the given eClass and qualifiedName. Therefore the
	// * java project involved is determined and the class is loaded from the
	// * workspace. To load the class an URLClassLoader is used. The parent
	// * classloader is the bundle classloader from
	// * org.lunifera.xtext.builder.ui.access. Since it defines a
	// dynamic-import:
	// * "*" all classes contained in the running IDE are loaded properly.
	// *
	// * @param eClass
	// * @param qualifiedName
	// * @return
	// */
	// Class<?> loadClass(EClass eClass, String qualifiedName);
	//
	// /**
	// * See {@link #loadClass(EClass, String)}. The main difference is, that
	// this
	// * method uses a newly prepared class loader.
	// *
	// * @param eClass
	// * @param qualifiedName
	// * @return
	// */
	// Class<?> reloadClass(EClass eClass, String qualifiedName);
}
