package com.api.playpal.architecture;

import com.api.playpal.user.domain.User;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.repository.Repository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

@AnalyzeClasses(packages = "com.api.playpal", importOptions = ImportOption.DoNotIncludeTests.class)
class AuthorizationArchitectureTest {

    private static final DescribedPredicate<JavaClass> repositories =
            assignableTo(Repository.class)
                    .or(simpleNameEndingWith("Repository"))
                    .or(simpleNameEndingWith("RepositoryImp"))
                    .as("repositorios de persistencia");

    @ArchTest
    static final ArchRule controllersDoNotAccessRepositoriesDirectly =
            noClasses().that().areAnnotatedWith(RestController.class)
                    .should().dependOnClassesThat(repositories)
                    .because("los controladores deben delegar la persistencia en los servicios de aplicacion");

    @ArchTest
    static final ArchRule controllersDoNotEvaluateRoles =
            noClasses().that().areAnnotatedWith(RestController.class)
                    .should().callMethod(User.class, "getRole")
                    .because("las decisiones basadas en rol pertenecen a la capa de aplicacion o a SecurityConfig");

    @ArchTest
    static final ArchRule controllersDoNotDeclareMethodLevelAuthorization =
            noMethods().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                    .should().beAnnotatedWith(PreAuthorize.class)
                    .orShould().beAnnotatedWith(Secured.class)
                    .orShould().beAnnotatedWith(RolesAllowed.class)
                    .because("la autorizacion de endpoints debe declararse solo en authorizeHttpRequests()");

    @ArchTest
    static final ArchRule httpAuthorizationIsCentralizedInSecurityConfig =
            noClasses().that().doNotHaveSimpleName("SecurityConfig")
                    .should().dependOnClassesThat().belongToAnyOf(HttpSecurity.class, SecurityFilterChain.class)
                    .because("SecurityConfig es el unico punto de decision de autorizacion HTTP");
}
