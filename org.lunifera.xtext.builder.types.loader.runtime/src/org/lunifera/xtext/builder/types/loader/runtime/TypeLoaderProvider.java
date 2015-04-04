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
package org.lunifera.xtext.builder.types.loader.runtime;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.lunifera.dsl.xtext.types.bundles.BundleSpace;
import org.lunifera.xtext.builder.types.loader.api.ITypeLoader;
import org.lunifera.xtext.builder.types.loader.api.ITypeLoaderFactory;
import org.lunifera.xtext.builder.types.loader.api.ITypeLoaderProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("restriction")
public class TypeLoaderProvider implements ITypeLoaderProvider {

	@Inject
	private ITypeLoaderFactory factory;

	private Map<WeakReference<ResourceSet>, ITypeLoader> loaders = new HashMap<WeakReference<ResourceSet>, ITypeLoader>(
			3);

	private ReferenceQueue<ResourceSet> referenceQueue = new ReferenceQueue<ResourceSet>();

	@Override
	public ITypeLoader get(ResourceSet resourceSet) {
		ITypeLoader loader = null;
		synchronized (loaders) {
			cleanUp();

			if (resourceSet == null)
				throw new IllegalArgumentException(
						"resourceSet may not be null.");

			XtextResourceSet xtextRS = (XtextResourceSet) resourceSet;
			Object context = xtextRS.getClasspathURIContext();
			if (!(context instanceof BundleSpace)) {
				throw new IllegalArgumentException(
						"No bundlespace available to load classes.");
			}

			for (Map.Entry<WeakReference<ResourceSet>, ITypeLoader> entry : loaders
					.entrySet()) {
				if (entry.getKey().get() == xtextRS) {
					loader = entry.getValue();
					break;
				}
			}

			if (loader == null) {
				loader = factory.createTypeLoader(xtextRS);
				loaders.put(new WeakReference<ResourceSet>(xtextRS,
						referenceQueue), loader);
			}
		}
		return loader;
	}

	private void cleanUp() {
		while (true) {
			Reference<? extends ResourceSet> ref = referenceQueue.poll();
			if (ref == null) {
				break;
			}
			ITypeLoader toDispose = loaders.get(ref);
			toDispose.dispose();
			loaders.remove(toDispose);
		}
	}

}
