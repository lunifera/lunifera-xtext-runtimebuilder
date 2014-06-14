package org.lunifera.dsl.xtext.builder.participant.jvmtypes.tests;

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
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.XtextPackage;
import org.junit.Before;
import org.junit.Test;
import org.lunifera.dsl.xtext.builder.participant.xbase.IXbaseMetadataService;
import org.lunifera.xtext.builder.metadata.services.IMetadataBuilderService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class XbaseBuilderParticipantTest {

	private Bundle extenderBundle;
	private Bundle builderBundle;
	private Bundle participantBundle;

	@Before
	public void setup() throws BundleException {

		setDefaultBundleContext(Activator.context);

		participantBundle = findBundle(Activator.context,
				"org.lunifera.xtext.builder.participant.xbase");
		participantBundle.start();
		extenderBundle = findBundle(Activator.context,
				"org.lunifera.xtext.builder.participant.xbase.extender.tests");
		extenderBundle.start();
		
		// restart the metadata service
		builderBundle = findBundle(Activator.context,
				"org.lunifera.xtext.builder.metadata.services");
		builderBundle.stop();
		builderBundle.start();
		getService(Activator.context, IMetadataBuilderService.class, 2000);
	}

	@Test
	public void test_deactivateService() throws Exception {
		IXbaseMetadataService service = getService(Activator.context,
				IXbaseMetadataService.class, 1000);
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, 1000);
		assertNotNull(service);
		assertNotNull(builderService);

		Grammar grammar = service.getGrammar("org.eclipse.xtext.Xtext");
		assertEquals("org.eclipse.xtext.Xtext", grammar.getName());

		builderBundle.stop();
		Thread.sleep(500);

		assertServiceUnavailable(IXbaseMetadataService.class);
		assertServiceUnavailable(IMetadataBuilderService.class);

		builderBundle.start();
		Thread.sleep(500);

		service = getService(Activator.context, IXbaseMetadataService.class,
				1000);
		builderService = getService(Activator.context,
				IMetadataBuilderService.class, 1000);
		assertNotNull(service);
		assertNotNull(builderService);

		Grammar grammar2 = service.getGrammar("org.eclipse.xtext.Xtext");
		assertEquals("org.eclipse.xtext.Xtext", grammar.getName());

		assertNotSame(grammar, grammar2);

		// since the resource was unloaded, the grammar loaded with old service
		// is
		// an EProxy now
		InternalEObject iGrammar = (InternalEObject) grammar;
		assertFalse(iGrammar.eIsProxy());
		assertNull(iGrammar.eResource().getResourceSet());
		assertNotNull(grammar2.eResource().getResourceSet());
		
		assertTrue(EcoreUtil.equals(grammar, grammar2));
	}

	@Test
	public void test_cache() throws Exception {
		IXbaseMetadataService service = getService(Activator.context,
				IXbaseMetadataService.class, 1000);
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, 1000);
		assertNotNull(service);
		assertNotNull(builderService);

		Grammar grammar = service.getGrammar("org.eclipse.xtext.Xtext");
		assertEquals("org.eclipse.xtext.Xtext", grammar.getName());

		Grammar grammar2 = service.getGrammar("org.eclipse.xtext.Xtext");
		assertEquals("org.eclipse.xtext.Xtext", grammar.getName());

		InternalEObject iGrammar = (InternalEObject) grammar;
		assertFalse(iGrammar.eIsProxy());
		assertNotNull(iGrammar.eResource().getResourceSet());
		assertNotNull(grammar2.eResource().getResourceSet());
		
		assertTrue(EcoreUtil.equals(grammar, grammar2));
		assertSame(grammar, grammar2);
	}

	@Test
	public void test_extendedGrammar() throws Exception {
		IXbaseMetadataService service = getService(Activator.context,
				IXbaseMetadataService.class, 1000);
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, 1000);
		assertNotNull(service);
		assertNotNull(builderService);

		Grammar grammar = service.getGrammar("xbase.extender.tests.Grammar");
		assertEquals("xbase.extender.tests.Grammar", grammar.getName());

		Grammar grammar2 = service.getGrammar("xbase.extender.tests.Grammar");
		assertEquals("xbase.extender.tests.Grammar", grammar.getName());

		InternalEObject iGrammar = (InternalEObject) grammar;
		assertFalse(iGrammar.eIsProxy());
		assertNotNull(grammar.eResource().getResourceSet());
		assertNotNull(grammar2.eResource().getResourceSet());
		
		assertTrue(EcoreUtil.equals(grammar, grammar2));
		assertSame(grammar, grammar2);
	}

	@Test
	public void test_stopExtenderBundle() throws Exception {
		IXbaseMetadataService service = getService(Activator.context,
				IXbaseMetadataService.class, 1000);
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, 1000);
		assertNotNull(service);
		assertNotNull(builderService);

		extenderBundle.stop();
		Thread.sleep(500);

		Grammar grammar = service.getGrammar("xbase.extender.tests.Grammar");
		assertNull(grammar);

		extenderBundle.start();
		Thread.sleep(500);

		grammar = service.getGrammar("xbase.extender.tests.Grammar");
		assertEquals("xbase.extender.tests.Grammar", grammar.getName());

		// stop again
		extenderBundle.stop();
		Thread.sleep(500);

		Grammar grammar2 = service.getGrammar("xbase.extender.tests.Grammar");
		assertNull(grammar2);

		// start again
		extenderBundle.start();
		Thread.sleep(500);

		Grammar grammar3 = service.getGrammar("xbase.extender.tests.Grammar");
		assertNotNull(grammar3);
		assertEquals("xbase.extender.tests.Grammar", grammar3.getName());

		// assert that the grammar loaded before stopping the service has
		// changed to an eProxy
		InternalEObject iGrammar = (InternalEObject) grammar;
		assertFalse(iGrammar.eIsProxy());
		assertNull(grammar.eResource().getResourceSet());
		assertNotNull(grammar3.eResource().getResourceSet());
		
		assertTrue(EcoreUtil.equals(grammar, grammar3));
		assertNotSame(grammar, grammar3);
	}

	@Test
	public void test_stopParticipantBundle() throws Exception {
		IXbaseMetadataService service = getService(Activator.context,
				IXbaseMetadataService.class, 1000);
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, 1000);
		assertNotNull(service);
		assertNotNull(builderService);

		participantBundle.stop();
		Thread.sleep(2500);

		service = getService(Activator.context, IXbaseMetadataService.class,
				1000);
		builderService = getService(Activator.context,
				IMetadataBuilderService.class, 1000);
		assertNotNull(builderService);
		assertNull(service);

		// access the grammar by using the builder service directly
		Grammar grammar = (Grammar) builderService.getMetadata(
				"xbase.extender.tests.Grammar", XtextPackage.Literals.GRAMMAR);
		assertNull(grammar);

		participantBundle.start();
		Thread.sleep(500);

		service = getService(Activator.context, IXbaseMetadataService.class,
				1000);
		grammar = service.getGrammar("xbase.extender.tests.Grammar");
		assertEquals("xbase.extender.tests.Grammar", grammar.getName());

		// stop and start again
		participantBundle.stop();
		Thread.sleep(500);
		participantBundle.start();
		Thread.sleep(500);

		Grammar grammar3 = service.getGrammar("xbase.extender.tests.Grammar");
		assertNotNull(grammar3);
		assertEquals("xbase.extender.tests.Grammar", grammar3.getName());

		// assert that the grammar loaded before stopping the service has
		// changed to an eProxy
		InternalEObject iGrammar = (InternalEObject) grammar;
		assertFalse(iGrammar.eIsProxy());
		assertNull(grammar.eResource().getResourceSet());
		assertNotNull(grammar3.eResource().getResourceSet());
		
		assertTrue(EcoreUtil.equals(grammar, grammar3));
		assertNotSame(grammar, grammar3);
	}

}
