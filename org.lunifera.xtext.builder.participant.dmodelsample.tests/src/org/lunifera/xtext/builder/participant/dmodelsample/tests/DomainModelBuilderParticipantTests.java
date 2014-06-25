package org.lunifera.xtext.builder.participant.dmodelsample.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.knowhowlab.osgi.testing.assertions.OSGiAssert.setDefaultBundleContext;
import static org.knowhowlab.osgi.testing.assertions.ServiceAssert.assertServiceUnavailable;
import static org.knowhowlab.osgi.testing.utils.BundleUtils.findBundle;
import static org.knowhowlab.osgi.testing.utils.ServiceUtils.getService;

import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.XtextPackage;
import org.eclipse.xtext.example.domainmodel.domainmodel.DomainmodelPackage;
import org.eclipse.xtext.example.domainmodel.domainmodel.Entity;
import org.junit.Before;
import org.junit.Test;
import org.lunifera.xtext.builder.metadata.services.IMetadataBuilderService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class DomainModelBuilderParticipantTests {

	private static final String ENTITY_NAME = "Simple";
	private static final String ENTITY = "org.my.tests.Simple";
	private static final int TIME_500 = 500;
	private static final int TIME_1000 = 1000;
	private static final int TIME_2000 = 2000;
	private static final int TIME_2500 = 2500;
	private Bundle builderBundle;
	private Bundle participantBundle;

	@Before
	public void setup() throws BundleException {

		setDefaultBundleContext(Activator.context);

		participantBundle = findBundle(Activator.context,
				"org.lunifera.xtext.builder.participant.dmodelsample");
		participantBundle.stop();

		// restart the metadata service
		builderBundle = findBundle(Activator.context,
				"org.lunifera.xtext.builder.metadata.services");
		builderBundle.stop();
		builderBundle.start();
		getService(Activator.context, IMetadataBuilderService.class, TIME_2000);
	}

	@Test
	public void testDeactivateService() throws Exception {
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(builderService);

		participantBundle.start();
		Thread.sleep(TIME_2000);
		
		Entity entity = findEntity(builderService);
		assertEquals(ENTITY_NAME, entity.getName());

		builderBundle.stop();
		Thread.sleep(TIME_500);

		assertServiceUnavailable(IMetadataBuilderService.class);

		builderBundle.start();
		Thread.sleep(TIME_500);

		builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(builderService);

		Entity entity2 = findEntity(builderService);
		assertEquals(ENTITY_NAME, entity2.getName());

		assertNotSame(entity, entity2);

		// since the resource was unloaded, the entity loaded with old service
		// is
		// an EProxy now
		InternalEObject iEntity = (InternalEObject) entity;
		assertFalse(iEntity.eIsProxy());
		assertNull(iEntity.eResource().getResourceSet());
		assertNotNull(entity2.eResource().getResourceSet());

		assertTrue(EcoreUtil.equals(entity, entity2));
	}

	private Entity findEntity(IMetadataBuilderService builderService) {
		return (Entity) builderService.getMetadata(ENTITY,
				DomainmodelPackage.Literals.ENTITY);
	}

	@Test
	public void testCache() throws Exception {
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(builderService);
		
		participantBundle.start();
		Thread.sleep(TIME_2000);
		
		Entity entity = findEntity(builderService);
		assertEquals(ENTITY_NAME, entity.getName());

		Entity entity2 = findEntity(builderService);
		assertEquals(ENTITY_NAME, entity.getName());

		InternalEObject iEntity = (InternalEObject) entity;
		assertFalse(iEntity.eIsProxy());
		assertNotNull(iEntity.eResource().getResourceSet());
		assertNotNull(entity2.eResource().getResourceSet());

		assertTrue(EcoreUtil.equals(entity, entity2));
		assertSame(entity, entity2);
	}

	@Test
	public void testStopParticipantBundle() throws Exception {
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(builderService);

		participantBundle.stop();
		Thread.sleep(TIME_2500);

		builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(builderService);

		// access the entity by using the builder service directly
		Entity entity = (Entity) builderService.getMetadata(ENTITY,
				XtextPackage.Literals.GRAMMAR);
		assertNull(entity);

		participantBundle.start();
		Thread.sleep(TIME_500);

		entity = findEntity(builderService);
		assertEquals(ENTITY_NAME, entity.getName());

		// stop and start again
		participantBundle.stop();
		Thread.sleep(TIME_500);
		participantBundle.start();
		Thread.sleep(TIME_500);

		Entity entity3 = findEntity(builderService);
		assertNotNull(entity3);
		assertEquals(ENTITY_NAME, entity3.getName());

		// assert that the entity loaded before stopping the service has
		// changed to an eProxy
		InternalEObject iEntity = (InternalEObject) entity;
		assertFalse(iEntity.eIsProxy());
		assertNull(entity.eResource().getResourceSet());
		assertNotNull(entity3.eResource().getResourceSet());

		assertTrue(EcoreUtil.equals(entity, entity3));
		assertNotSame(entity, entity3);
	}

}
