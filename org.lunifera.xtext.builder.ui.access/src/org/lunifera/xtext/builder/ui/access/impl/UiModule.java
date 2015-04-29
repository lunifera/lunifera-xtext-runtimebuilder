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
package org.lunifera.xtext.builder.ui.access.impl;

import org.eclipse.xtext.ui.shared.SharedStateModule;
import org.lunifera.xtext.builder.types.loader.api.ITypeLoaderFactory;
import org.lunifera.xtext.builder.types.loader.api.ITypeLoaderProvider;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoaderFactory;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoaderProvider;
import org.lunifera.xtext.builder.ui.access.jdt.impl.JdtTypeLoaderFactory;
import org.lunifera.xtext.builder.ui.access.jdt.impl.JdtTypeLoaderProvider;

import com.google.inject.Binder;
import com.google.inject.Singleton;

@SuppressWarnings("restriction")
public class UiModule extends SharedStateModule {

	public void configureITypeLoaderFactory(Binder binder) {
		binder.bind(ITypeLoaderFactory.class).to(JdtTypeLoaderFactory.class)
				.in(Singleton.class);
		binder.bind(IJdtTypeLoaderFactory.class).to(JdtTypeLoaderFactory.class)
				.in(Singleton.class);
	}

	public void configureITypeLoaderProvider(Binder binder) {
		binder.bind(ITypeLoaderProvider.class).to(JdtTypeLoaderProvider.class)
				.in(Singleton.class);
		binder.bind(IJdtTypeLoaderProvider.class)
				.to(JdtTypeLoaderProvider.class).in(Singleton.class);
	}

}
