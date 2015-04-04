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
package org.eclipse.xtext.example.domainmodel.formatting;

import java.util.List;

import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.example.domainmodel.services.DomainmodelGrammarAccess;
import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter;
import org.eclipse.xtext.formatting.impl.FormattingConfig;
import org.eclipse.xtext.util.Pair;

/**
 * This class contains custom formatting description.
 * 
 * see : http://www.eclipse.org/Xtext/documentation/latest/xtext.html#formatting
 * on how and when to use it 
 * 
 * Also see {@link org.eclipse.xtext.xtext.XtextFormattingTokenSerializer} as an example
 */
public class DomainmodelFormatter extends AbstractDeclarativeFormatter {
	
	@Override
	protected void configureFormatting(FormattingConfig c) {
		DomainmodelGrammarAccess f = (DomainmodelGrammarAccess) getGrammarAccess();

		c.setAutoLinewrap(120);
		
		c.setLinewrap(1, 2, 3).around(f.getAbstractElementRule());
		c.setLinewrap(1, 2, 3).around(f.getPackageDeclarationRule());
		c.setLinewrap(1, 1, 2).around(f.getFeatureRule());
		
		List<Pair<Keyword,Keyword>> pairs = f.findKeywordPairs("{", "}");
		for (Pair<Keyword, Keyword> pair : pairs) {
			c.setIndentation(pair.getFirst(), pair.getSecond());
		}
		
		c.setLinewrap(0, 1, 2).before(f.getSL_COMMENTRule());
		c.setLinewrap(0, 1, 2).before(f.getML_COMMENTRule());
		c.setLinewrap(0, 1, 1).after(f.getML_COMMENTRule());
		
	}
}
