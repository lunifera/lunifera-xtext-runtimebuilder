package org.lunifera.dsl.xtext.builder.participant.xbase;

import org.eclipse.xtext.Grammar;

public interface IXbaseMetadataService {

	/**
	 * Returns the {@link Grammar} qualified name.
	 * 
	 * @param qualifedName
	 * @return
	 */
	Grammar getGrammar(String qualifedName);

}
