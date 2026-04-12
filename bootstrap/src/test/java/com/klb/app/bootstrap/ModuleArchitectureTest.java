package com.klb.app.bootstrap;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
		packages = "com.klb.app",
		importOptions = ImportOption.DoNotIncludeTests.class
)
class ModuleArchitectureTest {

	@ArchTest
	static final ArchRule webMustNotDependOnBatch = noClasses()
			.that().resideInAPackage("..web..")
			.should().dependOnClassesThat().resideInAPackage("..batch..");

	@ArchTest
	static final ArchRule securityMustNotDependOnPersistence = noClasses()
			.that().resideInAPackage("..security..")
			.should().dependOnClassesThat().resideInAPackage("..persistence..");

	@ArchTest
	static final ArchRule applicationMustNotDependOnWeb = noClasses()
			.that().resideInAPackage("..application..")
			.should().dependOnClassesThat().resideInAPackage("..web..");

	@ArchTest
	static final ArchRule webMustNotDependOnApplicationServiceImpl = noClasses()
			.that().resideInAPackage("..web..")
			.should().dependOnClassesThat().resideInAPackage("..application.service.impl..");

	@ArchTest
	static final ArchRule batchMustNotDependOnWeb = noClasses()
			.that().resideInAPackage("..batch..")
			.should().dependOnClassesThat().resideInAPackage("..web..");

	@ArchTest
	static final ArchRule domainMustNotDependOnPersistence = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat().resideInAPackage("..persistence..");
}
