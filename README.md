Xtext Runtime Builder
=============================

A Xtext runtime builder for OSGi environments.

The main issue for that project is to provide OSGi services for different grammars and their semantic models.
The exposed services should return the semantic model elements for fully qualified names.

## Some examples

For instance org.eclipse.xtext.common.types.JvmType:
	
	interface IJvmTypeMetadataService {

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
The service will use the given qualified name to return the proper JvmType for it.

Another example is the Xbase grammar:

	public interface IXbaseMetadataService {
	
		/**
		 * Returns the {@link Grammar} qualified name.
		 * 
		 * @param qualifedName
		 * @return
		 */
		Grammar getGrammar(String qualifedName);
	
	}
	
Using this service the grammar semantic model of any known Xtext grammar can be accessed.

	IXbaseMetadataService service = getService();
	Grammar grammar = service.getGrammar("org.eclipse.xtext.Xtext")
	

## How it works
The project implements a builder core. This core can become extended by so called "builder participants" and 
by the OSGi extender pattern.

### BundleSpace
Xtext grammars that are based on Xbase and JvmTypes need to load the referenced classes. To load classes a
proper class loader is required. For now Xtext implements an JdtTypeProvider for the IDE and a ClasspathTypeProvider
for standalone issues.

Since we are working in an OSGi environment, the ClasspathType provider is not the perfect match. So that repo
provides a so called BundleSpaceTypeProvider. Every class loading issue is delegated to the BundleSpace, which will
try to load the class by delegating to all registered bundles.

#### Register bundles at BundleSpace
To register bundles at the BundleSpace, two different ways have been implemented. The IMetadataBuilderService API provides
a method #addToBundleSpace(Bundle bundle).
The second way is using the OSGi extender pattern. Extender pattern means, that a bundle exposes an additional Manifest header.
All bundles are being scanned by the MetadataBuilder and if they expose the defined header, they will be automatically
added to the BundleSpace.

	Manifest-Version: 1.0
	Bundle-ManifestVersion: 2
	Bundle-Name: org.lunifera.xtext.builder.participant.jvmtypes.extender.tests
	Bundle-Vendor: Lunifera.org
	Bundle-Version: 0.0.1.qualifier
	Bundle-SymbolicName: org.lunifera.xtext.builder.participant.jvmtypes.extender.tests
	...
	Lun-RuntimeBuilder-BundleSpace: 

In this example you can see the manifest header "Lun-RuntimeBuilder-BundleSpace". If this header is available in 
a manifest file, then the bundle becomes part of the BundleSpace.

#### Changes for XtextStandaloneSetup
To add your Xtext grammar to the runtime builder, you need to create a new standalone setup that uses the
BundleSpaceTypeProvider. You can find examples in the org.lunifera.xtext.builder.participant.xbase folder.
Under java package org.lunifera.dsl.xtext.builder.participant.xbase.impl you will find two classes called 
XbaseBundleSpaceRuntimeModule and XbaseBundleSpaceStandaloneSetup.

The important part is the XbaseBundleSpaceRuntimeModule:

	public class XbaseBundleSpaceRuntimeModule extends XbaseRuntimeModule {
		// contributed by org.eclipse.xtext.generator.types.TypesGeneratorFragment
		public Class<? extends org.eclipse.xtext.common.types.access.IJvmTypeProvider.Factory> bindIJvmTypeProvider$Factory() {
			return BundleSpaceTypeProviderFactory.class;
		}
		// contributed by org.eclipse.xtext.generator.types.TypesGeneratorFragment
		public Class<? extends org.eclipse.xtext.common.types.xtext.AbstractTypeScopeProvider> bindAbstractTypeScopeProvider() {
			return BundleSpaceTypeScopeProvider.class;
		}
	}

It registers the BundleSpace specific things at guice. The standalone setup just points the guice bindings to the implementations.

How to implement the StandaloneSetup just follow XbaseBundleSpaceStandaloneSetup. But mention, that every grammar needs to use
the BundleSpaceTypeProvider. Also the Xbase grammars. If your grammar extends Xbase, then you may use 
XbaseWithAnnotationsBundleSpaceRuntimeModule an XbaseWithAnnotationsBundleSpaceStandaloneSetup as a templat for your implementations.


### Builder participants
Builder participants are responsible to setup grammars, collect model files and to expose services. XbaseBuilderParticipant
may be a good template for you own implementations.

The builder participant does following steps:
 1) setup Xtext grammars
 2) load all models for the given bundle
 3) expose an OSGi service for proper access
 

#### OSGi service
To expose a BuilderParticipant OSGi service, you may use OSGi declarative services. All OSGi exposed with the interface 
IBuilderParticipant will become hooked up by the MetadataBuilder and will become treaded as a participant.  

### Services come and services go
If participants are extender bundles are stopped, their associated Xtext models will be removed from the XtextResourceSet.
But they won't become unloaded. Unloading a resource would mean, that it turns to an EProxy which may not be resolved.
To change that issue, two different properties are implemented in the MetadataBuilder.

	/**
	 * If true, then affected resources will become unloaded if an extender
	 * bundle is stopped or a builder participant service removed
	 */
	private boolean unloadResources = false;

	/**
	 * If true, then affected resources will be removed from the resource set if
	 * an extender bundle is stopped or a builder participant service removed
	 */
	private boolean removeFromResourceset = true;

For now they can not become configured by Config Admin. But if anybody has a request for it, i can change that issue and
make these properties configureable.

### Build project
To build that project just call ```mvn clean verify -Pbuild.features,build.p2,stage.p2 -Dlunifera.build.eclipse.platform.name=kepler -Dlunifera.deploy.to.local.federated.p2 -Dlunifera.build.uses.remote.federated.p2```


### Get artifacts
The artifacts are available by maven or P2 repository.

	P2 --> http://lun.lunifera.org/downloads/p2/lunifera/kepler/latest/
	Maven --> http://maven.lunifera.org:8086/nexus/content/repositories/snapshots/org/lunifera/xtext/builder/
 

TypeLoader
=============================
As explained before, classes may be loaded from different locations. It makes a big difference whether you are working
in the IDE or code runs in the Rumtime without an Eclipse UI.

This projects added two different ways to load classes

### IJdtTypeLoader - in UI environments
The IJdtTypeLoader is located in the bundle ```org.lunifera.xtext.builder.ui.access``` and it allows you to load classes
from your java projects. It is perfect for preview issues. Imagine there is a Xtext grammar that infers JavaCode and puts
it somewhere in a java project at the workspace. The running eclipse instance may then load the class from java project
and directly use.

#### IXtextUtilService
The easiest way to consume classes from the workspace is the use of IXtextUtilService. It provides a lot of convenience methods
to access classes.
It also provides you access to a workspace project for a given fully qualified name of a java class.

IXtextUtilService is an OSGi service component and will automatically startup.

#### Directly use IJdtTypeLoader
To use the jdtTypeLoader take a look at UiModule class in the ui.access project. It will configure Guice to inject it into your
Xtext resources.


### ITypeLoader - in NON-UI environments with Lunifera Builder
The IJdtTypeLoader is located in the bundle ```org.lunifera.xtext.builder.types.loader.runtime``` and it allows you to load classes
from your BundleSpace (see description above).

To use the bundle space type loader, you need to register it in the Guice-module. And it highly requires a BundleSpace setup.
If BundleSpace is not configured to be used, the typeLoader will not be usable. But runtime environments using the Lunifera
Xtext Builder will always use the BundleSpace.
To see an example how to setup a runtime environment follow:

```
https://github.com/lunifera/lunifera-ecview-addons/tree/master/org.lunifera.ecview.dsl/src/org/lunifera/ecview/dsl
classes: UIGrammarBundleSpaceRuntimeModule and UIGrammarBundleSpaceRuntimeSetup

```


 



