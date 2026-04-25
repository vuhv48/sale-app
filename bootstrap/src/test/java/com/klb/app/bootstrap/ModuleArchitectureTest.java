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
			.that().resideInAPackage("com.klb.app.web..")
			.should().dependOnClassesThat().resideInAPackage("com.klb.app.batch..");

	@ArchTest
	static final ArchRule securityMustNotDependOnPersistence = noClasses()
			.that().resideInAPackage("com.klb.app.security..")
			.should().dependOnClassesThat().resideInAPackage("com.klb.app.persistence..");

	@ArchTest
	static final ArchRule applicationMustNotDependOnWeb = noClasses()
			.that().resideInAPackage("com.klb.app.application..")
			.should().dependOnClassesThat().resideInAPackage("com.klb.app.web..");

	@ArchTest
	static final ArchRule webMustNotDependOnApplicationServiceImpl = noClasses()
			.that().resideInAPackage("com.klb.app.web..")
			.should().dependOnClassesThat().resideInAPackage("com.klb.app.application.service.impl..");

	@ArchTest
	static final ArchRule batchMustNotDependOnWeb = noClasses()
			.that().resideInAPackage("com.klb.app.batch..")
			.should().dependOnClassesThat().resideInAPackage("com.klb.app.web..");

	@ArchTest
	static final ArchRule redisMustStayInfrastructureOnly = noClasses()
			.that().resideInAPackage("com.klb.app.redis..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"com.klb.app.web..",
					"com.klb.app.persistence..",
					"com.klb.app.application..",
					"com.klb.app.batch..");

	@ArchTest
	static final ArchRule kafkaMustStayInfrastructureOnly = noClasses()
			.that().resideInAPackage("com.klb.app.kafka..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"com.klb.app.web..",
					"com.klb.app.persistence..",
					"com.klb.app.application..",
					"com.klb.app.batch..");

	@ArchTest
	static final ArchRule mongodbMustStayInfrastructureOnly = noClasses()
			.that().resideInAPackage("com.klb.app.mongodb..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"com.klb.app.web..",
					"com.klb.app.persistence..",
					"com.klb.app.application..",
					"com.klb.app.batch..");
}
