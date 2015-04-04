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
package org.lunifera.xtext.builder.metadata.services;

import java.net.URL;
import java.util.List;

import org.lunifera.dsl.xtext.types.bundles.BundleSpaceTypeProvider;
import org.osgi.framework.Bundle;

/**
 * Builder participants can participate to the model builder.<br>
 * Attention: The runtime builder needs to use the
 * {@link BundleSpaceTypeProvider}. Attached {@link BuilderParticipant} or
 * {@link StandaloneGrammarsSetup} for implementation details.
 * 
 * <b>Attention:</b> This interface should not be implemented by clients. Use
 * {@link AbstractBuilderParticipant} instead.
 */
@SuppressWarnings("restriction")
public interface IBuilderParticipant {

	/**
	 * Returns a list with all models that should be loaded into the builder. To
	 * become part of model providers, the {@link IMetadataBuilderService} Guice
	 * Inject may be used.
	 * 
	 * @param suspect
	 *            The bundle which might contain model files.
	 * 
	 * @return urls - never <code>null</code>
	 */
	List<URL> getModels(Bundle suspect);

	/**
	 * Registers the participant about the lifecyle.<br>
	 * The participant needs to do different issues. On ACTIVATE OSGi-services
	 * should be registered. On DEACTIVATE they should be unregistered.
	 * 
	 * @param event
	 */
	void notifyLifecyle(LifecycleEvent event);

	/**
	 * The lifecycle event for the participant.
	 */
	class LifecycleEvent {
		public static final int INITIALIZE = 10;
		public static final int ACTIVATED = 20;
		public static final int BUNDLES_SCANNED = 30;
		public static final int DEACTIVATED = 40;

		private int state;

		public LifecycleEvent(int state) {
			super();
			this.state = state;
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}

	}

}
