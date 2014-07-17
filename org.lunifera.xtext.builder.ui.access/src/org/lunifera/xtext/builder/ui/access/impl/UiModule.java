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
