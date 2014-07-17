package org.lunifera.xtext.builder.ui.access.jdt;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;

public interface IJdtTypeLoaderProvider {

	IJdtTypeLoader get(ResourceSet resourceSet);

	IJdtTypeLoader get(IJavaProject project);
}
