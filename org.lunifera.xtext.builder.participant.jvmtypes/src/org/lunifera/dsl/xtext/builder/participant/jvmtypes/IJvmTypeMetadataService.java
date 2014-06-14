package org.lunifera.dsl.xtext.builder.participant.jvmtypes;

import org.eclipse.xtext.common.types.JvmType;

public interface IJvmTypeMetadataService {

	/**
	 * Returns the {@link JvmType} for the given class.
	 * 
	 * @param clazz
	 * @return
	 */
	JvmType getJvmType(Class<?> clazz);

	/**
	 * Returns the {@link JvmType} for the given qualified name.
	 * 
	 * @param qualifiedName
	 * @return
	 */
	JvmType getJvmType(String qualifiedName);

}
