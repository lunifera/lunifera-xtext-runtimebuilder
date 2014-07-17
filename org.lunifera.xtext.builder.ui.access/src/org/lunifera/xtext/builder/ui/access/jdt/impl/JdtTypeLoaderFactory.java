package org.lunifera.xtext.builder.ui.access.jdt.impl;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.xtext.common.types.access.jdt.IJavaProjectProvider;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoader;
import org.lunifera.xtext.builder.ui.access.jdt.IJdtTypeLoaderFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class JdtTypeLoaderFactory implements IJdtTypeLoaderFactory {

	@Inject
	private IJavaProjectProvider javaProjectProvider;

	public IJdtTypeLoader createJdtTypeLoader(ResourceSet resourceSet) {
		if (resourceSet == null)
			throw new IllegalArgumentException("resourceSet may not be null.");
		IJavaProject javaProject = javaProjectProvider
				.getJavaProject(resourceSet);
		IJdtTypeLoader result = createJdtTypeLoader(javaProject);
		return result;
	}

	public IJdtTypeLoader createJdtTypeLoader(IJavaProject javaProject) {
		return new JdtTypeLoader(javaProject);
	}

}
