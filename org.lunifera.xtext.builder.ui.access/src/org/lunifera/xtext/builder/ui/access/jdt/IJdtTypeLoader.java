package org.lunifera.xtext.builder.ui.access.jdt;

public interface IJdtTypeLoader {

	Class<?> findTypeByName(String fullyQualifiedName);

	void dispose();

}
