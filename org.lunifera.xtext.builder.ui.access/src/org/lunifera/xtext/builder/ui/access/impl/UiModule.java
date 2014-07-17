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
package org.lunifera.xtext.builder.ui.access.impl;

import org.eclipse.xtext.ui.shared.SharedStateModule;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoaderFactory;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoaderProvider;
import org.lunifera.xtext.builder.ui.access.jdt.impl.JdtTypeLoaderFactory;
import org.lunifera.xtext.builder.ui.access.jdt.impl.JdtTypeLoaderProvider;

public class UiModule extends SharedStateModule {

	public Class<? extends IJdtTypeLoaderFactory> bindIJdtTypeLoaderFactory() {
		return JdtTypeLoaderFactory.class;
	}

	public Class<? extends IJdtTypeLoaderProvider> bindIJdtTypeLoaderProvider() {
		return JdtTypeLoaderProvider.class;
	}
	
}
