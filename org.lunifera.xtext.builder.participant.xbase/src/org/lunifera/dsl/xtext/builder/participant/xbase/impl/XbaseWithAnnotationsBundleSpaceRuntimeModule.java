package org.lunifera.dsl.xtext.builder.participant.xbase.impl;

import org.eclipse.xtext.xbase.annotations.XbaseWithAnnotationsRuntimeModule;
import org.lunifera.dsl.xtext.types.bundles.BundleSpaceTypeProviderFactory;
import org.lunifera.dsl.xtext.types.bundles.BundleSpaceTypeScopeProvider;

@SuppressWarnings("restriction")
public class XbaseWithAnnotationsBundleSpaceRuntimeModule extends
		XbaseWithAnnotationsRuntimeModule {

	// contributed by org.eclipse.xtext.generator.types.TypesGeneratorFragment
	public Class<? extends org.eclipse.xtext.common.types.access.IJvmTypeProvider.Factory> bindIJvmTypeProvider$Factory() {
		return BundleSpaceTypeProviderFactory.class;
	}

	// contributed by org.eclipse.xtext.generator.types.TypesGeneratorFragment
	public Class<? extends org.eclipse.xtext.common.types.xtext.AbstractTypeScopeProvider> bindAbstractTypeScopeProvider() {
		return BundleSpaceTypeScopeProvider.class;
	}

}
