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
 * Based on Xtext org.eclipse.xtext.common.types.access.impl.ClassFinder
 * 
 */
package org.lunifera.dsl.xtext.types.bundles;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.common.types.access.impl.ClassNameUtil;
import org.eclipse.xtext.common.types.access.impl.Primitives;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.lunifera.runtime.common.types.IBundleSpace;
import org.osgi.framework.Bundle;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class BundleSpace implements IBundleSpace {

	private static final Class<?> NULL_CLASS;
	@Inject
	private XtextResourceSet resourceSet;

	static {
		class Null {
		}
		NULL_CLASS = Null.class;
	}

	private final Set<Bundle> bundles = Collections
			.synchronizedSet(new HashSet<Bundle>());
	private final ClassNameUtil classNameUtil;
	private final Class<?> nullValue;
	private final Cache cache;

	public BundleSpace() {
		this.nullValue = NULL_CLASS;
		this.cache = new Cache();
		this.classNameUtil = new ClassNameUtil();
	}

	/**
	 * Tries to find a class for the given class name using the bundles
	 * registered in the bundle space.
	 * 
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class<?> forName(String name) throws ClassNotFoundException {
		Class<?> result = null;
		synchronized (cache) {
			result = cache.get(name);
			if (result != null) {
				if (result == nullValue) {
					throw new ClassNotFoundException();
				}
				return result;
			}

			synchronized (bundles) {
				for (Bundle bundle : bundles) {
					try {
						result = bundle.loadClass(classNameUtil
								.normalizeClassName(name));
						if (result != null) {
							break;
						}
					} catch (ClassNotFoundException e) {
						// nothing to do
					}
				}
			}

			if (result != null) {
				cache.put(name, result);
			} else {
				cache.put(name, NULL_CLASS);
				throw new ClassNotFoundException();
			}
		}
		return result;
	}

	/**
	 * Adds a new bundle to the bundle space. The bundle will be used to find
	 * classes.
	 * 
	 * @param bundle
	 */
	public void add(Bundle bundle) {
		bundles.add(bundle);

		synchronized (cache) {
			cache.resetNullValue();
		}
	}

	/**
	 * Removes the bundle from the bundle space.
	 * 
	 * @param bundle
	 */
	public void remove(Bundle bundle) {
		bundles.remove(bundle);
	}

	@SuppressWarnings("serial")
	private class Cache extends HashMap<String, Class<?>> {
		private static final int INITIAL_SIZE = 500;

		public Cache() {
			super(INITIAL_SIZE);
			for (Class<?> primitiveType : Primitives.ALL_PRIMITIVE_TYPES) {
				put(primitiveType.getName(), primitiveType);
			}
		}

		public void resetNullValue() {
			for (Iterator<Map.Entry<String, Class<?>>> iterator = entrySet()
					.iterator(); iterator.hasNext();) {
				Map.Entry<String, Class<?>> entry = iterator.next();
				if (entry.getValue() == NULL_CLASS) {
					unloadClassResource(entry);
					iterator.remove();
				}
			}
		}

		/**
		 * Unloads the class resource.
		 * 
		 * @param entry
		 */
		private void unloadClassResource(Map.Entry<String, Class<?>> entry) {
			Resource resource = resourceSet.getResource(
					URI.createURI(String.format("java://%s/Objects/%s",
							entry.getKey(), entry.getKey())), false);
			if (resource != null) {
				resource.unload();
				resourceSet.getResources().remove(resource);
			}
		}
	}
}
