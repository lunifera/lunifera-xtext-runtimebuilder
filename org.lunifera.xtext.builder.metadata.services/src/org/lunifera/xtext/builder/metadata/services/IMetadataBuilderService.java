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
package org.lunifera.xtext.builder.metadata.services;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.lunifera.dsl.xtext.types.bundles.BundleSpace;
import org.lunifera.runtime.common.types.IBundleSpace;
import org.osgi.framework.Bundle;

/**
 * A service that handles the runtime build of Xtext models or Ecore models. It
 * uses delegates to extend the types of models that may become handled.
 * {@link IBuilderParticipant} have to become registered as an OSGi service.
 * Guice injection is enabled. After a participant was added to the service,
 * injections will be done.
 * 
 * <p>
 * <b>Attention:</b> This interface should not be implemented by clients!
 */
@SuppressWarnings("restriction")
public interface IMetadataBuilderService {

	/**
	 * Bundles that add this header to their MANIFEST are added to the
	 * BundleSpace of the builder. So class loading issues are forwareded to
	 * this bundle.
	 */
	static final String LUN_RUNTIME_BUILDER_BUNDLE_SPACE = "Lun-RuntimeBuilder-BundleSpace";

	/**
	 * Returns the resolved model or <code>null</code> if the model could not be
	 * found.
	 * 
	 * @param fqn
	 * @param type
	 * @return
	 */
	EObject getMetadata(String qualifiedName, EClass type);

	/**
	 * Returns all {@link IEObjectDescription}s found for the given type.
	 * 
	 * @param type
	 * @return
	 */
	Iterable<IEObjectDescription> getAllDescriptions(EClass type);

	/**
	 * Adds the given bundle to the {@link BundleSpace}, so it may be used to
	 * resolved classes. Note, that also
	 * {@link #LUN_RUNTIME_BUILDER_BUNDLE_SPACE} MANIFEST header may be used, to
	 * add a bundle to the {@link BundleSpace}. This method allows
	 * {@link IBuilderParticipant} to add additional bundles that do not provide
	 * the header.
	 * 
	 * @param bundle
	 */
	void addToBundleSpace(Bundle bundle);

	/**
	 * Removes the given bundle from the {@link BundleSpace}.
	 * 
	 * @param bundle
	 */
	void removeFromBundleSpace(Bundle bundle);
	
	/**
	 * Returns the bundle space.
	 * @return
	 */
	IBundleSpace getBundleSpace();

}
