
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
