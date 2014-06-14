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
package org.lunifera.xtext.builder.metadata.services.impl;

import static com.google.common.collect.Iterables.addAll;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.common.types.access.impl.IndexedJvmTypeAccess;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.lunifera.dsl.xtext.types.bundles.BundleSpace;
import org.lunifera.dsl.xtext.types.bundles.BundleSpaceTypeProvider;
import org.lunifera.xtext.builder.metadata.services.IBuilderParticipant;
import org.lunifera.xtext.builder.metadata.services.IBuilderParticipant.LifecycleEvent;
import org.lunifera.xtext.builder.metadata.services.IMetadataBuilderService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;

@SuppressWarnings("restriction")
@Component(immediate = true)
public class MetadataBuilder implements BundleListener, IMetadataBuilderService {

	private static final Logger logger = org.slf4j.LoggerFactory
			.getLogger(MetadataBuilder.class);

	// Is used to sync calls to the bundle space
	private ComponentContext context;
	private XtextResourceSet resourceSet;
	private ResourceDescriptionsProvider resourceDescriptionsProvider;
	private IQualifiedNameConverter converter;
	private IndexedJvmTypeAccess jvmTypeAccess;
	private BundleSpace bundleSpace;

	private Set<Bundle> modelProviders = Collections
			.synchronizedSet(new HashSet<Bundle>());
	private List<IBuilderParticipant> participants = Collections
			.synchronizedList(new ArrayList<IBuilderParticipant>());
	private Set<IBuilderParticipant> injectedParticipants = Collections
			.synchronizedSet(new HashSet<IBuilderParticipant>());

	private Injector injector;

	private AtomicBoolean waitingForFrameworkStartedEvent = new AtomicBoolean(
			true);

	@Activate
	public void activate(ComponentContext context) {
		this.context = context;
		new ServiceActivatedTask().run();
	}

	private boolean isActive() {
		return context != null;
	}

	@Deactivate
	public void deactivate() {
		new ServiceDeactivatedTask().run();
		;
	}

	/**
	 * Returns the metadata model element for the given parameters. Or
	 * <code>null</code> if no model could be found.
	 * 
	 * @param qualifiedName
	 * @param type
	 * @return
	 */
	public synchronized EObject getMetadata(String qualifiedName, EClass type) {
		return getEObjectForFQN(converter.toQualifiedName(qualifiedName), type);
	}

	public EObject getEObjectForFQN(QualifiedName fqn, EClass type) {
		EObject result = null;
		IResourceDescriptions resourceDescriptions = resourceDescriptionsProvider
				.getResourceDescriptions(resourceSet);
		Iterable<IEObjectDescription> descriptions = resourceDescriptions
				.getExportedObjects(type, fqn, false);
		for (IEObjectDescription desc : descriptions) {
			result = desc.getEObjectOrProxy();
		}

		return result;
	}

	/**
	 * Returns true, if the bundle contains the header.
	 * 
	 * @param bundle
	 * @param header
	 * @return
	 */
	private boolean containsHeader(Bundle bundle, String header) {
		Dictionary<String, String> headers = bundle.getHeaders();
		Enumeration<String> keys = headers.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key.equals(header)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Activates the given participant.
	 * 
	 * @param participant
	 */
	private void doActivateParticipant(IBuilderParticipant participant) {
		if (isActive()) {
			// Tell the participant to initialize its state
			participant.notifyLifecyle(new IBuilderParticipant.LifecycleEvent(
					LifecycleEvent.INITIALIZE));
			// Tell the participant to activate its state
			participant.notifyLifecyle(new IBuilderParticipant.LifecycleEvent(
					LifecycleEvent.ACTIVATED));
		}
	}

	/**
	 * Unresolves the models contained in the given bundle.
	 * 
	 * @param bundle
	 */
	private synchronized void unresolveModels(Bundle bundle) {
		List<URL> urls = doFindModels(bundle);
		if (urls.isEmpty()) {
			return;
		}

		for (URL url : urls) {
			logger.info("Unregistered " + url.toString());
			Resource rs = resourceSet.getResource(
					URI.createURI(url.toString()), true);
			rs.unload();
			resourceSet.getResources().remove(rs);
		}

		EcoreUtil.resolveAll(resourceSet);

		List<Issue> validationResults = validate(resourceSet);
		for (Issue issue : validationResults) {
			System.out.println(issue.getMessage());
		}
	}

	protected List<Issue> validate(ResourceSet resourceSet) {
		List<Issue> issues = Lists.newArrayList();
		List<Resource> resources = Lists.newArrayList(resourceSet
				.getResources());
		for (Resource resource : resources) {
			IResourceServiceProvider resourceServiceProvider = IResourceServiceProvider.Registry.INSTANCE
					.getResourceServiceProvider(resource.getURI());
			if (resourceServiceProvider != null) {
				IResourceValidator resourceValidator = resourceServiceProvider
						.getResourceValidator();
				List<Issue> result = resourceValidator.validate(resource,
						CheckMode.ALL, null);
				addAll(issues, result);
			}
		}
		return issues;
	}

	/**
	 * Looks up for all models available.
	 * 
	 * @param bundleContext
	 * @return
	 */
	private List<URL> doFindModels(Bundle suspect) {

		List<URL> result = new ArrayList<URL>();
		// iterate all participants
		synchronized (participants) {
			for (IBuilderParticipant participant : participants) {
				result.addAll(participant.getModels(suspect));
			}
		}

		if (result.size() > 0) {
			modelProviders.add(suspect);
		}

		return result;
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		if (event.getType() == BundleEvent.STARTED) {
			new BundleAddedTask(event.getBundle()).run();
		} else if (event.getType() == BundleEvent.STOPPED) {
			new BundleRemovedTask(event.getBundle()).run();
		}
	}

	/**
	 * Called by OSGi-DS.
	 * 
	 * @param participant
	 */
	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, unbind = "removeParticipant")
	public void addParticipant(IBuilderParticipant participant) {
		new ParticipantAddedTask(participant).run();
	}

	private void doInitializeParticipant(IBuilderParticipant participant) {
		participant
				.notifyLifecyle(new LifecycleEvent(LifecycleEvent.INITIALIZE));
	}

	private void doInjectParticipant(IBuilderParticipant participant) {
		if (isActive() && !injectedParticipants.contains(participant)) {
			injector.injectMembers(participant);
			injectedParticipants.add(participant);
		}
	}

	protected void handleTaskFinish(ITask task) {
		if (task.getClass() == ServiceActivatedTask.class) {
			new WaitForFrameworkTask().run();
		} else if (task.getClass() == WaitForFrameworkTask.class) {
			new InitialResolveTask().run();
		} else if (task.getClass() == InitialResolveTask.class) {
			// nothing to do for now. All listeners are installed properly
		}
	}

	/**
	 * Called by OSGi-DS.
	 * 
	 * @param participant
	 */
	public void removeParticipant(IBuilderParticipant participant) {
		new ParticipantRemovedTask(participant).run();
	}

	private void doDeactivateParticipant(IBuilderParticipant participant) {
		participant.notifyLifecyle(new LifecycleEvent(
				LifecycleEvent.DEACTIVATED));

		injectedParticipants.remove(participant);
	}

	/**
	 * Adds the bundle to the bundle space if the header is available.
	 * 
	 * @param bundle
	 */
	private void doAddToBundleSpace(Bundle bundle) {
		synchronized (bundleSpace) {
			bundleSpace.add(bundle);
		}
	}

	/**
	 * Removes the given bundle from the bundle space.
	 * 
	 * @param bundle
	 */
	private void doRemoveFromBundleSpace(Bundle bundle) {
		synchronized (bundleSpace) {
			bundleSpace.remove(bundle);
		}
	}

	@Override
	public void addToBundleSpace(Bundle bundle) {
		new AddBundleToBundleSpaceTask(bundle).run();
	}

	@Override
	public void removeFromBundleSpace(Bundle bundle) {
		new RemoveBundleFromBundleSpaceTask(bundle).run();
	}

	/**
	 * An internal task that processes different kinds of issues.
	 */
	static interface ITask {
		void run();
	}

	/**
	 * This task handles waiting for the framework start. Only if the framework
	 * became started, the models may become resolved.
	 */
	private class WaitForFrameworkTask implements ITask, FrameworkListener {
		@Override
		public void run() {

			// get the state of the framework
			Bundle framework = context.getBundleContext().getBundle(0);
			if (framework.getState() == Bundle.ACTIVE) {
				waitingForFrameworkStartedEvent.set(false);
			}

			if (waitingForFrameworkStartedEvent.get()) {
				context.getBundleContext().addFrameworkListener(this);
			} else {
				notifyFrameworkStarted();
			}
		}

		@Override
		public void frameworkEvent(FrameworkEvent event) {
			if (event.getType() == FrameworkEvent.STARTED) {
				context.getBundleContext().removeFrameworkListener(this);

				notifyFrameworkStarted();
			}
		}

		private void notifyFrameworkStarted() {
			waitingForFrameworkStartedEvent.set(false);

			handleTaskFinish(this);
		}
	}

	/**
	 * This task will be called, if the service service was activated
	 */
	private class ServiceActivatedTask implements ITask {

		@Override
		public void run() {
			doSetupService();
			doInjectParticipants();
			doInitializeParticipants();
			doActivateParticipants();

			handleTaskFinish(this);
		}

		/**
		 * Does the setup.
		 */
		protected void doSetupService() {
			converter = new IQualifiedNameConverter.DefaultImpl();
			injector = Guice.createInjector(new MetadataBuilderModule(
					MetadataBuilder.this));
			resourceSet = injector.getInstance(XtextResourceSet.class);
			resourceDescriptionsProvider = injector
					.getInstance(ResourceDescriptionsProvider.class);
			jvmTypeAccess = injector.getInstance(IndexedJvmTypeAccess.class);
			bundleSpace = injector.getInstance(BundleSpace.class);
			bundleSpace.add(context.getBundleContext().getBundle());

			// Create the bundle space for class loading issues
			new BundleSpaceTypeProvider(bundleSpace, resourceSet, jvmTypeAccess);
			// new ClasspathTypeProvider(classLoader, resourceSet, null);
			resourceSet.setClasspathURIContext(bundleSpace);
		}

		private void doInitializeParticipants() {
			for (IBuilderParticipant participant : participants
					.toArray(new IBuilderParticipant[participants.size()])) {
				doInitializeParticipant(participant);
			}
		}

		private void doInjectParticipants() {
			for (IBuilderParticipant participant : participants) {
				doInjectParticipant(participant);
			}
		}

		/**
		 * Activate all participants.
		 */
		private void doActivateParticipants() {
			for (IBuilderParticipant participant : participants
					.toArray(new IBuilderParticipant[participants.size()])) {
				doActivateParticipant(participant);
			}
		}
	}

	private class InitialResolveTask implements ITask {

		@Override
		public void run() {
			doScanAllBundles();
			startBundleTracking();

			handleTaskFinish(this);
		}

		/**
		 * Resolves all models for all proper model bundles.
		 */
		private synchronized void doScanAllBundles() {
			for (Bundle bundle : context.getBundleContext().getBundles()) {
				for (URL url : doFindModels(bundle)) {
					logger.info("Adding model " + url.toString()
							+ " to model cache.");
					resourceSet
							.getResource(URI.createURI(url.toString()), true);

					// adds the bundle to the bundleSpace if header is available
					doAddToBundleSpace(bundle);
				}
			}

			// resolve all models
			EcoreUtil.resolveAll(resourceSet);

			List<Issue> validationResults = validate(resourceSet);
			for (Issue issue : validationResults) {
				logger.warn(issue.toString());
			}

			logger.info("Models resolved. In case of error, see messages before.");
		}

		/**
		 * Starts the tracking of bundles.
		 */
		private synchronized void startBundleTracking() {
			context.getBundleContext().addBundleListener(MetadataBuilder.this);
		}

	}

	private class AddBundleToBundleSpaceTask implements ITask {

		private final Bundle bundle;

		public AddBundleToBundleSpaceTask(Bundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public void run() {
			doAddToBundleSpace(bundle);
		}
	}

	private class RemoveBundleFromBundleSpaceTask implements ITask {

		private final Bundle bundle;

		public RemoveBundleFromBundleSpaceTask(Bundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public void run() {
			doRemoveFromBundleSpace(bundle);
		}

	}

	private class BundleAddedTask implements ITask {

		private final Bundle bundle;

		public BundleAddedTask(Bundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public void run() {
			// started bundles need to expose the MANIFEST header
			if (containsHeader(bundle, LUN_RUNTIME_BUILDER_BUNDLE_SPACE)) {
				doAddToBundleSpace(bundle);
			}

			// if the bundle was not scanned yet
			if (!modelProviders.contains(bundle)) {
				doResolveAddedBundle(bundle);
			}
		}

		/**
		 * Resolves the models contained in the given bundle.
		 * 
		 * @param bundle
		 */
		private synchronized void doResolveAddedBundle(Bundle bundle) {
			List<URL> urls = doFindModels(bundle);
			if (urls.isEmpty()) {
				return;
			}
			for (URL url : urls) {
				logger.info("Added " + url.toString() + " to metadata cache.");
				resourceSet.getResource(URI.createURI(url.toString()), true);
			}

			EcoreUtil.resolveAll(resourceSet);

			List<Issue> validationResults = validate(resourceSet);
			for (Issue issue : validationResults) {
				logger.warn(issue.toString());
			}
		}
	}

	private class BundleRemovedTask implements ITask {

		private final Bundle bundle;

		public BundleRemovedTask(Bundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public void run() {
			// remove the bundle from the bundle space
			removeFromBundleSpace(bundle);

			// the bundle was already scanned
			if (!modelProviders.contains(bundle)) {
				return;
			}

			unresolveModels(bundle);

			modelProviders.remove(bundle);
		}

	}

	private class ServiceDeactivatedTask implements ITask {

		@Override
		public void run() {
			context.getBundleContext().removeBundleListener(
					MetadataBuilder.this);

			modelProviders.clear();

			for (Resource rs : new ArrayList<Resource>(
					resourceSet.getResources())) {
				rs.unload();
			}
			resourceSet = null;
			resourceDescriptionsProvider = null;
		}
	}

	private class ParticipantAddedTask implements ITask {

		private final IBuilderParticipant participant;

		public ParticipantAddedTask(IBuilderParticipant participant) {
			this.participant = participant;
		}

		@Override
		public void run() {
			if (!participants.contains(participant)) {
				doInjectParticipant(participant);

				participants.add(participant);

				if (isActive()) {
					doInitializeParticipant(participant);
					doActivateParticipant(participant);
				}
			}
		}

	}

	private class ParticipantRemovedTask implements ITask {

		private final IBuilderParticipant participant;

		public ParticipantRemovedTask(IBuilderParticipant participant) {
			this.participant = participant;
		}

		@Override
		public void run() {
			participants.remove(participant);
			doDeactivateParticipant(participant);
		}

	}

}
