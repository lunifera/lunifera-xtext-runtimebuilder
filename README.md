lunifera-xtext-runtimebuilder
=============================

A Xtext runtime builder for OSGi environments.

The main issue for that project is to provide OSGi services for different grammars. These services should return 
the semantic model elements for fully qualified names.

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

It will use the given qualified name to return the proper JvmType for it.

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
	
	

	 




