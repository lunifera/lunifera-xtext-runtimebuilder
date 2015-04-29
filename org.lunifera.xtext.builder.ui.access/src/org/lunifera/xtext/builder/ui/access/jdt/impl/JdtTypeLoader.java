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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoader;

public class JdtTypeLoader implements IJdtTypeLoader {

	private final IJavaProject javaProject;
	private URLClassLoader classLoader;

	/**
	 * @since 2.4
	 * @noreference This constructor is not intended to be referenced by
	 *              clients.
	 */
	public JdtTypeLoader(IJavaProject javaProject) {
		if (javaProject == null)
			throw new IllegalArgumentException("javaProject may not be null");
		this.javaProject = javaProject;
	}

	public Class<?> findTypeByName(String fullyQualifiedName) {
		if (fullyQualifiedName == null || fullyQualifiedName.equals("")) {
			return null;
		}
		try {
			synchronized (this) {
				ClassLoader classloader = getClassloader();
				return classloader.loadClass(fullyQualifiedName);
			}
		} catch (JavaModelException e) {
			throw new IllegalStateException(e);
		} catch (ClassNotFoundException e) {
			// nothing to do
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		} catch(NoClassDefFoundError e){
			// nothing to do
		}

		return null;
	}

	/**
	 * Returns the class loader if not available.
	 * 
	 * @return
	 * @throws CoreException
	 * @throws MalformedURLException
	 */
	private ClassLoader getClassloader() throws CoreException,
			MalformedURLException {
		if (classLoader != null) {
			return classLoader;
		}

		String[] classPathEntries = JavaRuntime
				.computeDefaultRuntimeClassPath(javaProject);

		Set<URL> urlList = new HashSet<URL>();
		for (int i = 0; i < classPathEntries.length; i++) {
			String entry = classPathEntries[i];
			IPath path = new Path(entry);
			URL url = path.toFile().toURI().toURL();
			urlList.add(url);
		}

		// the parentClassLoader contains references to required ECView classes
		ClassLoader parentClassLoader = getClass().getClassLoader();
		URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
		classLoader = new URLClassLoader(urls, parentClassLoader);

		return classLoader;
	}

	@Override
	public void dispose() {
		synchronized (this) {
			// try {
			if (classLoader != null) {
				// classLoader.close();
				classLoader = null;
			}
			// } catch (IOException e) {
			// throw new IllegalStateException(e);
			// }
		}
	}
}
