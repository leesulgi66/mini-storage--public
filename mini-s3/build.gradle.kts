plugins {
	id("org.springframework.boot")
}

description = "mini S3 - Object Storage Core"

dependencies {
	implementation(project(":common"))

	// Spring
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// MyBatis
	implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.5")

	// DB
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	runtimeOnly("org.postgresql:postgresql")

	// Flyway
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")

	// Dev
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// Test
	testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.5")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}