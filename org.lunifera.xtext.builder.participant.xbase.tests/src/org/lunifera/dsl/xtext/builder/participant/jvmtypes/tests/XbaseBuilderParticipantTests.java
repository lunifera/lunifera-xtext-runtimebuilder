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

public class XbaseBuilderParticipantTests {

	private static final String XTEXT_GRAMMAR = "org.eclipse.xtext.Xtext";
	private static final String TESTS_GRAMMAR = "xbase.extender.tests.Grammar";
	private static final int TIME_500 = 500;
	private static final int TIME_1000 = 1000;
	private static final int TIME_2000 = 2000;
	private static final int TIME_2500 = 2500;
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
		getService(Activator.context, IMetadataBuilderService.class, TIME_2000);
	}

	@Test
	public void testDeactivateService() throws Exception {
		IXbaseMetadataService service = getService(Activator.context,
				IXbaseMetadataService.class, TIME_1000);
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(service);
		assertNotNull(builderService);

		Grammar grammar = service.getGrammar(XTEXT_GRAMMAR);
		assertEquals(XTEXT_GRAMMAR, grammar.getName());

		builderBundle.stop();
		Thread.sleep(TIME_500);

		assertServiceUnavailable(IXbaseMetadataService.class);
		assertServiceUnavailable(IMetadataBuilderService.class);

		builderBundle.start();
		Thread.sleep(TIME_500);

		service = getService(Activator.context, IXbaseMetadataService.class,
				TIME_1000);
		builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(service);
		assertNotNull(builderService);

		Grammar grammar2 = service.getGrammar(XTEXT_GRAMMAR);
		assertEquals(XTEXT_GRAMMAR, grammar.getName());

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
	public void testCache() throws Exception {
		IXbaseMetadataService service = getService(Activator.context,
				IXbaseMetadataService.class, TIME_1000);
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(service);
		assertNotNull(builderService);

		Grammar grammar = service.getGrammar(XTEXT_GRAMMAR);
		assertEquals(XTEXT_GRAMMAR, grammar.getName());

		Grammar grammar2 = service.getGrammar(XTEXT_GRAMMAR);
		assertEquals(XTEXT_GRAMMAR, grammar.getName());

		InternalEObject iGrammar = (InternalEObject) grammar;
		assertFalse(iGrammar.eIsProxy());
		assertNotNull(iGrammar.eResource().getResourceSet());
		assertNotNull(grammar2.eResource().getResourceSet());

		assertTrue(EcoreUtil.equals(grammar, grammar2));
		assertSame(grammar, grammar2);
	}

	@Test
	public void testExtendedGrammar() throws Exception {
		IXbaseMetadataService service = getService(Activator.context,
				IXbaseMetadataService.class, TIME_1000);
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(service);
		assertNotNull(builderService);

		Grammar grammar = service.getGrammar(TESTS_GRAMMAR);
		assertEquals(TESTS_GRAMMAR, grammar.getName());

		Grammar grammar2 = service.getGrammar(TESTS_GRAMMAR);
		assertEquals(TESTS_GRAMMAR, grammar.getName());

		InternalEObject iGrammar = (InternalEObject) grammar;
		assertFalse(iGrammar.eIsProxy());
		assertNotNull(grammar.eResource().getResourceSet());
		assertNotNull(grammar2.eResource().getResourceSet());

		assertTrue(EcoreUtil.equals(grammar, grammar2));
		assertSame(grammar, grammar2);
	}

	@Test
	public void testStopExtenderBundle() throws Exception {
		IXbaseMetadataService service = getService(Activator.context,
				IXbaseMetadataService.class, TIME_1000);
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(service);
		assertNotNull(builderService);

		extenderBundle.stop();
		Thread.sleep(TIME_500);

		Grammar grammar = service.getGrammar(TESTS_GRAMMAR);
		assertNull(grammar);

		extenderBundle.start();
		Thread.sleep(TIME_500);

		grammar = service.getGrammar(TESTS_GRAMMAR);
		assertEquals(TESTS_GRAMMAR, grammar.getName());

		// stop again
		extenderBundle.stop();
		Thread.sleep(TIME_500);

		Grammar grammar2 = service.getGrammar(TESTS_GRAMMAR);
		assertNull(grammar2);

		// start again
		extenderBundle.start();
		Thread.sleep(TIME_500);

		Grammar grammar3 = service.getGrammar(TESTS_GRAMMAR);
		assertNotNull(grammar3);
		assertEquals(TESTS_GRAMMAR, grammar3.getName());

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
	public void testStopParticipantBundle() throws Exception {
		IXbaseMetadataService service = getService(Activator.context,
				IXbaseMetadataService.class, TIME_1000);
		IMetadataBuilderService builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(service);
		assertNotNull(builderService);

		participantBundle.stop();
		Thread.sleep(TIME_2500);

		service = getService(Activator.context, IXbaseMetadataService.class,
				TIME_1000);
		builderService = getService(Activator.context,
				IMetadataBuilderService.class, TIME_1000);
		assertNotNull(builderService);
		assertNull(service);

		// access the grammar by using the builder service directly
		Grammar grammar = (Grammar) builderService.getMetadata(TESTS_GRAMMAR,
				XtextPackage.Literals.GRAMMAR);
		assertNull(grammar);

		participantBundle.start();
		Thread.sleep(TIME_500);

		service = getService(Activator.context, IXbaseMetadataService.class,
				TIME_1000);
		grammar = service.getGrammar(TESTS_GRAMMAR);
		assertEquals(TESTS_GRAMMAR, grammar.getName());

		// stop and start again
		participantBundle.stop();
		Thread.sleep(TIME_500);
		participantBundle.start();
		Thread.sleep(TIME_500);

		Grammar grammar3 = service.getGrammar(TESTS_GRAMMAR);
		assertNotNull(grammar3);
		assertEquals(TESTS_GRAMMAR, grammar3.getName());

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
