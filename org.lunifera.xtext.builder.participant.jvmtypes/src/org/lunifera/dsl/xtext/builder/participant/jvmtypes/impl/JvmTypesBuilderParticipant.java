package org.lunifera.dsl.xtext.builder.participant.jvmtypes.impl;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.lunifera.dsl.xtext.builder.participant.jvmtypes.IJvmTypeMetadataService;
import org.lunifera.xtext.builder.metadata.services.AbstractBuilderParticipant;
import org.lunifera.xtext.builder.metadata.services.IBuilderParticipant;
import org.lunifera.xtext.builder.metadata.services.IMetadataBuilderService;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.google.inject.Inject;

@Component(service = { IBuilderParticipant.class })
public class JvmTypesBuilderParticipant extends AbstractBuilderParticipant {

	@Inject
	private XtextResourceSet resourceSet;

	@Inject
	private IMetadataBuilderService metadataBuilderService;

	private ComponentContext context;
	private ServiceRegistration<IJvmTypeMetadataService> serviceRegister;

	@Activate
	protected void activate(ComponentContext context) {
		this.context = context;
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		metadataBuilderService.removeFromBundleSpace(context.getBundleContext()
				.getBundle());

		this.context = null;
	}

	@Override
	public List<URL> getModels(Bundle suspect) {
		return Collections.emptyList();
	}

	@Override
	public void notifyLifecyle(LifecycleEvent event) {
		if (event.getState() == IBuilderParticipant.LifecycleEvent.INITIALIZE) {
			initialize();
		} else if (event.getState() == IBuilderParticipant.LifecycleEvent.ACTIVATED) {
			JvmTypeService jvmTypeService = new JvmTypeService();
			serviceRegister = context.getBundleContext().registerService(
					IJvmTypeMetadataService.class, jvmTypeService, null);
		} else if (event.getState() == IBuilderParticipant.LifecycleEvent.DEACTIVATED) {
			if (serviceRegister != null) {
				serviceRegister.unregister();
				serviceRegister = null;
			}

			if (metadataBuilderService != null) {
				metadataBuilderService.removeFromBundleSpace(context
						.getBundleContext().getBundle());
			}
		}
	}

	private void initialize() {
		if (!Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
				.containsKey("ecore")) {
			Resource.Factory.Registry.INSTANCE
					.getExtensionToFactoryMap()
					.put("ecore",
							new org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl());
		}

		if (!Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
				.containsKey("xmi")) {
			Resource.Factory.Registry.INSTANCE
					.getExtensionToFactoryMap()
					.put("xmi",
							new org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl());
		}

		if (!EPackage.Registry.INSTANCE.containsKey(TypesPackage.eNS_URI)) {
			EPackage.Registry.INSTANCE.put(TypesPackage.eNS_URI,
					TypesPackage.eINSTANCE);
		}

		metadataBuilderService.addToBundleSpace(context.getBundleContext()
				.getBundle());
	}

	/**
	 * Provided as an OSGi service to return {@link JvmType JvmTypes} for the
	 * given class name.
	 */
	private class JvmTypeService implements IJvmTypeMetadataService {

		@Override
		public JvmType getJvmType(Class<?> clazz) {
			return getJvmType(clazz.getCanonicalName());
		}

		@Override
		public JvmType getJvmType(String qualifiedName) {
			Resource resource = resourceSet.getResource(URI.createURI(String
					.format("java://%s/Objects/%s", qualifiedName,
							qualifiedName)), true);
			return (JvmType) (resource.getContents().size() > 0 ? resource
					.getContents().get(0) : null);
		}

	}

}
