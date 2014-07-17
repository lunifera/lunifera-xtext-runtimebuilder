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
package org.lunifera.xtext.builder.ui.access.jdt.impl;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.xtext.common.types.access.jdt.IJavaProjectProvider;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoader;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoaderFactory;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoaderProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class JdtTypeLoaderProvider implements IJdtTypeLoaderProvider {

	@Inject
	private IJavaProjectProvider javaProjectProvider;

	@Inject
	private IJdtTypeLoaderFactory factory;

	private Map<WeakReference<IProject>, IJdtTypeLoader> loaders = new HashMap<WeakReference<IProject>, IJdtTypeLoader>(
			3);

	private ReferenceQueue<IProject> referenceQueue = new ReferenceQueue<IProject>();

	@Override
	public IJdtTypeLoader get(ResourceSet resourceSet) {
		IJavaProject javaProject = javaProjectProvider
				.getJavaProject(resourceSet);
		if (javaProject == null) {
			return null;
		}

		IJdtTypeLoader loader = get(javaProject);

		return loader;
	}

	@Override
	public IJdtTypeLoader get(IJavaProject javaProject) {
		IJdtTypeLoader loader = null;
		synchronized (loaders) {
			cleanUp();

			IProject project = javaProject.getProject();
			for (Map.Entry<WeakReference<IProject>, IJdtTypeLoader> entry : loaders
					.entrySet()) {
				if (entry.getKey().get() == project) {
					loader = entry.getValue();
					break;
				}
			}

			if (loader == null) {
				loader = factory.createJdtTypeLoader(javaProject);
				loaders.put(
						new WeakReference<IProject>(project, referenceQueue),
						loader);
			}
		}
		return loader;
	}

	private void cleanUp() {
		while (true) {
			Reference<? extends IProject> ref = referenceQueue.poll();
			if (ref == null) {
				break;
			}
			IJdtTypeLoader toDispose = loaders.get(ref);
			toDispose.dispose();
			loaders.remove(toDispose);
		}
	}

}
