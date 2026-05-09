package org.dubini.frontend_api.config;

import org.springframework.aot.hint.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints({
        JwtNativeConfig.JwtHintsRegistrar.class,
        NativeHintsConfig.AppHintsRegistrar.class
})
public class NativeHintsConfig {

    static class AppHintsRegistrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

            // ============ DTOs ============
            registerDto(hints, "org.dubini.frontend_api.dto.EditorJSBlock");
            registerDto(hints, "org.dubini.frontend_api.dto.EditorJSContentDTO");
            registerDto(hints, "org.dubini.frontend_api.dto.PublicationDTO");
            registerDto(hints, "org.dubini.frontend_api.dto.HttpResponse");
            registerDto(hints, "org.dubini.frontend_api.dto.UpdateStatusResponse");
            registerDto(hints, "org.dubini.frontend_api.dto.MuseoVisitanteRegistroRequest");
            registerDto(hints, "org.dubini.frontend_api.dto.MuseoVisitanteRegistroResponse");
            registerDto(hints, "org.dubini.frontend_api.dto.ContactRequest");
            registerDto(hints, "org.dubini.frontend_api.dto.InscripcionRequest");

            // ============ EXCEPCIONES ============
            registerException(hints, "org.dubini.frontend_api.exception.BackofficeException");
            registerException(hints, "org.dubini.frontend_api.exception.CacheException");
            registerException(hints, "org.dubini.frontend_api.exception.ResourceNotFoundException");
            registerException(hints, "org.dubini.frontend_api.exception.MuseoRegistroException");
            registerException(hints, "org.dubini.frontend_api.exception.GlobalExceptionHandler");

            // ============ SPRING SECURITY ============
            registerSecurityClasses(hints);

            // ============ CONTROLLERS ============
            registerController(hints, "org.dubini.frontend_api.controller.WebController");
            registerController(hints, "org.dubini.frontend_api.controller.MuseoController");
            registerController(hints, "org.dubini.frontend_api.controller.rest.CacheController");
            registerController(hints, "org.dubini.frontend_api.controller.rest.NewsRestController");
            registerController(hints, "org.dubini.frontend_api.controller.rest.ContactController");
            registerController(hints, "org.dubini.frontend_api.controller.rest.InscripcionController");

            // ============ SERVICIOS ============
            registerService(hints, "org.dubini.frontend_api.service.NewsService");
            registerService(hints, "org.dubini.frontend_api.service.CacheEtagService");
            registerService(hints, "org.dubini.frontend_api.service.SupabaseStorageService");
            registerService(hints, "org.dubini.frontend_api.service.ResendEmailService");
            registerService(hints, "org.dubini.frontend_api.service.MuseoRegistroService");
            registerService(hints, "org.dubini.frontend_api.service.RateLimiterService");

            // ============ CLIENTS ============
            registerService(hints, "org.dubini.frontend_api.client.NewsClient");

            // ============ CACHE ============
            registerCacheClasses(hints);

            // ============ CONFIGURATION ============
            registerConfig(hints, "org.dubini.frontend_api.config.BackofficeApiUrlProperties");
            registerConfig(hints, "org.dubini.frontend_api.config.CorsConfig");
            registerConfig(hints, "org.dubini.frontend_api.config.JwtProperties");
            registerConfig(hints, "org.dubini.frontend_api.config.SecurityConfig");
            registerConfig(hints, "org.dubini.frontend_api.config.WebClientConfig");
            registerConfig(hints, "org.dubini.frontend_api.config.SupabaseStorageProperties");
            registerConfig(hints, "org.dubini.frontend_api.config.ResendProperties");

            // ============ RECURSOS ESTÁTICOS ============
            hints.resources().registerPattern("templates/**");
            hints.resources().registerPattern("templates/*.html");
            hints.resources().registerPattern("templates/fragments/*.html");
            hints.resources().registerPattern("static/**");
            hints.resources().registerPattern("static/assets/**");
            hints.resources().registerPattern("static/assets/documents/**");
            hints.resources().registerPattern("static/scripts/**");
            hints.resources().registerPattern("static/scripts/api/**");
            hints.resources().registerPattern("static/scripts/components/**");
            hints.resources().registerPattern("static/scripts/pages/**");
            hints.resources().registerPattern("static/styles/**");
            hints.resources().registerPattern("static/styles/components/**");
            hints.resources().registerPattern("static/styles/layout/**");
            hints.resources().registerPattern("static/*.js");
            hints.resources().registerPattern("static/*.ico");
            hints.resources().registerPattern("application*.properties");
            hints.resources().registerPattern("resend.properties");
            hints.resources().registerPattern("application*.yml");
            hints.resources().registerPattern("META-INF/**");
            hints.resources().registerPattern("META-INF/additional-spring-configuration-metadata.json");

            // ============ THYMELEAF ============
            registerThymeleafClasses(hints);

            // ============ JACKSON (JSON serialization) ============
            registerJacksonClasses(hints);

            // ============ WEBCLIENT / WEBFLUX ============
            registerWebClientClasses(hints);

            // ============ RESILIENCE4J ============
            registerResilience4jClasses(hints);

            // ============ CAFFEINE CACHE ============
            registerCaffeineClasses(hints);

            // ============ SUPABASE STORAGE ============
            registerSupabaseClasses(hints);

            // ============ BEAN VALIDATION / JSR 380 ============
            registerValidationClasses(hints);
        }

        @SuppressWarnings("unchecked")
        private void registerDto(RuntimeHints hints, String className) {
            try {
                Class<?> clazz = Class.forName(className);
                hints.reflection().registerType(
                        clazz,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.DECLARED_FIELDS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.INVOKE_DECLARED_METHODS);
                if (java.io.Serializable.class.isAssignableFrom(clazz)) {
                    hints.serialization().registerType((Class<? extends java.io.Serializable>) clazz);
                }
            } catch (ClassNotFoundException e) {
                System.err.println("DTO no encontrado: " + className);
            }
        }

        private void registerException(RuntimeHints hints, String className) {
            try {
                hints.reflection().registerType(
                        Class.forName(className),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.DECLARED_FIELDS);
            } catch (ClassNotFoundException e) {
                System.err.println("Exception no encontrada: " + className);
            }
        }

        private void registerController(RuntimeHints hints, String className) {
            try {
                hints.reflection().registerType(
                        Class.forName(className),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS);
            } catch (ClassNotFoundException e) {
                System.err.println("Controller no encontrado: " + className);
            }
        }

        private void registerService(RuntimeHints hints, String className) {
            try {
                hints.reflection().registerType(
                        Class.forName(className),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS);
            } catch (ClassNotFoundException e) {
                System.err.println("Service no encontrado: " + className);
            }
        }

        private void registerConfig(RuntimeHints hints, String className) {
            try {
                hints.reflection().registerType(
                        Class.forName(className),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.DECLARED_FIELDS);
            } catch (ClassNotFoundException e) {
                System.err.println("Config no encontrado: " + className);
            }
        }

        private void registerCacheClasses(RuntimeHints hints) {
            try {
                hints.reflection().registerType(
                        Class.forName("org.dubini.frontend_api.cache.CacheInitializer"),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS);

                hints.reflection().registerType(
                        Class.forName("org.dubini.frontend_api.cache.CacheWarmable"),
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS);

                hints.reflection().registerType(
                        Class.forName("org.dubini.frontend_api.cache.PersistentCaffeineCacheManager"),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS);
            } catch (ClassNotFoundException e) {
                System.err.println("Cache class no encontrada: " + e.getMessage());
            }
        }

        private void registerSecurityClasses(RuntimeHints hints) {
            try {
                // Spring Security User
                hints.reflection().registerType(
                        org.springframework.security.core.userdetails.User.class,
                        MemberCategory.values());

                // Spring Security Authority
                hints.reflection().registerType(
                        org.springframework.security.core.authority.SimpleGrantedAuthority.class,
                        MemberCategory.values());

                // JWT Filter y Provider
                hints.reflection().registerType(
                        Class.forName("org.dubini.frontend_api.security.JwtFilter"),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS);

                hints.reflection().registerType(
                        Class.forName("org.dubini.frontend_api.security.JwtProvider"),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS);

            } catch (ClassNotFoundException e) {
                System.err.println("Security class no encontrada: " + e.getMessage());
            }
        }

        private void registerThymeleafClasses(RuntimeHints hints) {
            try {
                // Thymeleaf core classes
                hints.reflection().registerType(
                        Class.forName("org.thymeleaf.spring6.view.ThymeleafViewResolver"),
                        MemberCategory.values());
                hints.reflection().registerType(
                        Class.forName("org.thymeleaf.spring6.SpringTemplateEngine"),
                        MemberCategory.values());
            } catch (ClassNotFoundException e) {
                System.err.println("Thymeleaf class no encontrada: " + e.getMessage());
            }
        }

        private void registerJacksonClasses(RuntimeHints hints) {
            try {
                // Jackson ObjectMapper y clases core
                hints.reflection().registerType(
                        com.fasterxml.jackson.databind.ObjectMapper.class,
                        MemberCategory.values());
                hints.reflection().registerType(
                        com.fasterxml.jackson.databind.JsonNode.class,
                        MemberCategory.values());
                hints.reflection().registerType(
                        com.fasterxml.jackson.databind.node.ObjectNode.class,
                        MemberCategory.values());
                hints.reflection().registerType(
                        com.fasterxml.jackson.databind.node.ArrayNode.class,
                        MemberCategory.values());

                // TypeReference para deserialización genérica
                hints.reflection().registerType(
                        com.fasterxml.jackson.core.type.TypeReference.class,
                        MemberCategory.values());
            } catch (Exception e) {
                System.err.println("Jackson class issue: " + e.getMessage());
            }
        }

        private void registerWebClientClasses(RuntimeHints hints) {
            try {
                // WebClient core
                hints.reflection().registerType(
                        org.springframework.web.reactive.function.client.WebClient.class,
                        MemberCategory.values());

                // WebClient Exceptions
                registerClassIfExists(hints, "org.springframework.web.reactive.function.client.WebClientException");
                registerClassIfExists(hints,
                        "org.springframework.web.reactive.function.client.WebClientRequestException");
                registerClassIfExists(hints,
                        "org.springframework.web.reactive.function.client.WebClientResponseException");
                registerClassIfExists(hints,
                        "org.springframework.web.reactive.function.client.WebClientResponseException$BadRequest");
                registerClassIfExists(hints,
                        "org.springframework.web.reactive.function.client.WebClientResponseException$Unauthorized");
                registerClassIfExists(hints,
                        "org.springframework.web.reactive.function.client.WebClientResponseException$Forbidden");
                registerClassIfExists(hints,
                        "org.springframework.web.reactive.function.client.WebClientResponseException$NotFound");
                registerClassIfExists(hints,
                        "org.springframework.web.reactive.function.client.WebClientResponseException$InternalServerError");
                registerClassIfExists(hints,
                        "org.springframework.web.reactive.function.client.WebClientResponseException$ServiceUnavailable");

                // Reactor core types
                registerClassIfExists(hints, "reactor.core.publisher.Mono");
                registerClassIfExists(hints, "reactor.core.publisher.Flux");

                // Netty classes
                registerClassIfExists(hints, "io.netty.channel.Channel");
                registerClassIfExists(hints, "io.netty.channel.EventLoopGroup");
                registerClassIfExists(hints, "io.netty.channel.nio.NioEventLoopGroup");
                registerClassIfExists(hints, "io.netty.channel.socket.nio.NioSocketChannel");

                // Reactor Netty
                registerClassIfExists(hints, "reactor.netty.http.client.HttpClient");
                registerClassIfExists(hints, "reactor.netty.resources.ConnectionProvider");

            } catch (Exception e) {
                System.err.println("WebClient class issue: " + e.getMessage());
            }
        }

        private void registerResilience4jClasses(RuntimeHints hints) {
            try {
                // Circuit Breaker
                registerClassIfExists(hints, "io.github.resilience4j.circuitbreaker.CircuitBreaker");
                registerClassIfExists(hints, "io.github.resilience4j.circuitbreaker.CircuitBreakerConfig");
                registerClassIfExists(hints, "io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry");

                // Retry
                registerClassIfExists(hints, "io.github.resilience4j.retry.Retry");
                registerClassIfExists(hints, "io.github.resilience4j.retry.RetryConfig");
                registerClassIfExists(hints, "io.github.resilience4j.retry.RetryRegistry");

                // Time Limiter
                registerClassIfExists(hints, "io.github.resilience4j.timelimiter.TimeLimiter");
                registerClassIfExists(hints, "io.github.resilience4j.timelimiter.TimeLimiterConfig");

                // Bulkhead
                registerClassIfExists(hints, "io.github.resilience4j.bulkhead.Bulkhead");
                registerClassIfExists(hints, "io.github.resilience4j.bulkhead.BulkheadConfig");

                // Rate Limiter
                registerClassIfExists(hints, "io.github.resilience4j.ratelimiter.RateLimiter");
                registerClassIfExists(hints, "io.github.resilience4j.ratelimiter.RateLimiterConfig");

                // Spring Boot integration
                registerClassIfExists(hints,
                        "io.github.resilience4j.spring6.circuitbreaker.configure.CircuitBreakerConfiguration");
                registerClassIfExists(hints, "io.github.resilience4j.spring6.retry.configure.RetryConfiguration");

                // Configuration Properties - CRÍTICO
                registerClassIfExists(hints,
                        "io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerProperties");
                registerClassIfExists(hints, "io.github.resilience4j.springboot3.retry.autoconfigure.RetryProperties");
                registerClassIfExists(hints,
                        "io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterProperties");
                registerClassIfExists(hints,
                        "io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadProperties");
                registerClassIfExists(hints,
                        "io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterProperties");

                // Instance Configuration
                registerClassIfExists(hints,
                        "io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigurationProperties");
                registerClassIfExists(hints,
                        "io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigurationProperties$InstanceProperties");
                registerClassIfExists(hints,
                        "io.github.resilience4j.common.retry.configuration.RetryConfigurationProperties");
                registerClassIfExists(hints,
                        "io.github.resilience4j.common.retry.configuration.RetryConfigurationProperties$InstanceProperties");

            } catch (Exception e) {
                System.err.println("Resilience4j class issue: " + e.getMessage());
            }
        }

        private void registerCaffeineClasses(RuntimeHints hints) {
            try {
                // Caffeine core
                hints.reflection().registerType(
                        com.github.benmanes.caffeine.cache.Caffeine.class,
                        MemberCategory.values());

                registerClassIfExists(hints, "com.github.benmanes.caffeine.cache.Cache");
                registerClassIfExists(hints, "com.github.benmanes.caffeine.cache.LoadingCache");
                registerClassIfExists(hints, "com.github.benmanes.caffeine.cache.AsyncCache");
                registerClassIfExists(hints, "com.github.benmanes.caffeine.cache.AsyncLoadingCache");
                registerClassIfExists(hints, "com.github.benmanes.caffeine.cache.CacheLoader");
                registerClassIfExists(hints, "com.github.benmanes.caffeine.cache.RemovalListener");
                registerClassIfExists(hints, "com.github.benmanes.caffeine.cache.Weigher");
                registerClassIfExists(hints, "com.github.benmanes.caffeine.cache.Expiry");

                // Spring Cache integration
                registerClassIfExists(hints, "org.springframework.cache.caffeine.CaffeineCacheManager");
                registerClassIfExists(hints, "org.springframework.cache.caffeine.CaffeineCache");

            } catch (Exception e) {
                System.err.println("Caffeine class issue: " + e.getMessage());
            }
        }

        private void registerSupabaseClasses(RuntimeHints hints) {
            try {
                // Java HTTP Client (usado por SupabaseStorageService)
                hints.reflection().registerType(
                        java.net.http.HttpClient.class,
                        MemberCategory.values());

                registerClassIfExists(hints, "java.net.http.HttpRequest");
                registerClassIfExists(hints, "java.net.http.HttpRequest$Builder");
                registerClassIfExists(hints, "java.net.http.HttpRequest$BodyPublisher");
                registerClassIfExists(hints, "java.net.http.HttpRequest$BodyPublishers");
                registerClassIfExists(hints, "java.net.http.HttpResponse");
                registerClassIfExists(hints, "java.net.http.HttpResponse$BodyHandler");
                registerClassIfExists(hints, "java.net.http.HttpResponse$BodyHandlers");

                // Map types usados en cache persistence
                hints.reflection().registerType(
                        java.util.Map.class,
                        MemberCategory.values());
                hints.reflection().registerType(
                        java.util.HashMap.class,
                        MemberCategory.values());
                hints.reflection().registerType(
                        java.util.concurrent.ConcurrentHashMap.class,
                        MemberCategory.values());

                // Serialization hints para Map<Object, Object>
                hints.serialization().registerType(java.util.HashMap.class);
                hints.serialization().registerType(java.util.concurrent.ConcurrentHashMap.class);

            } catch (Exception e) {
                System.err.println("Supabase class issue: " + e.getMessage());
            }
        }

        private void registerValidationClasses(RuntimeHints hints) {
            try {
                // Hibernate Validator constraint validators - JSR 380 - Solo Java 8+ time
                // NOTE: Omitiendo validadores de Joda Time ya que no están en classpath
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForLocalDate");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForLocalDateTime");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForLocalTime");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForZonedDateTime");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForLocalDate");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.assertion.AssertFalseValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.assertion.AssertTrueValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.number.bound.DecimalMinValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.number.bound.DecimalMaxValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidator$CollectionSizeValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.email.EmailValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.pattern.PatternValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.notblank.NotBlankValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.notnull.NotNullValidator");
                registerClassIfExists(hints,
                        "org.hibernate.validator.internal.constraintvalidators.bv.isnull.IsNullValidator");

                // Registrar las anotaciones de validación (constraints)
                registerClassIfExists(hints, "jakarta.validation.constraints.Future");
                registerClassIfExists(hints, "jakarta.validation.constraints.FutureOrPresent");
                registerClassIfExists(hints, "jakarta.validation.constraints.Email");
                registerClassIfExists(hints, "jakarta.validation.constraints.NotBlank");
                registerClassIfExists(hints, "jakarta.validation.constraints.NotEmpty");
                registerClassIfExists(hints, "jakarta.validation.constraints.NotNull");
                registerClassIfExists(hints, "jakarta.validation.constraints.Pattern");
                registerClassIfExists(hints, "jakarta.validation.constraints.Min");
                registerClassIfExists(hints, "jakarta.validation.constraints.Max");
                registerClassIfExists(hints, "jakarta.validation.constraints.Size");
                registerClassIfExists(hints, "jakarta.validation.constraints.DecimalMin");
                registerClassIfExists(hints, "jakarta.validation.constraints.DecimalMax");
                registerClassIfExists(hints, "jakarta.validation.constraints.AssertTrue");
                registerClassIfExists(hints, "jakarta.validation.constraints.AssertFalse");

                // Hibernate Validator configuración
                registerClassIfExists(hints, "org.hibernate.validator.HibernateValidator");
                registerClassIfExists(hints, "org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator");
                registerClassIfExists(hints, "org.springframework.validation.beanvalidation.LocalValidatorFactoryBean");

            } catch (Exception e) {
                System.err.println("Validation class issue: " + e.getMessage());
            }
        }

        private void registerClassIfExists(RuntimeHints hints, String className) {
            try {
                Class<?> clazz = Class.forName(className);
                hints.reflection().registerType(
                        clazz,
                        MemberCategory.values());
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                // Ignorar si no existe o sus dependencias no están disponibles
            } catch (Exception e) {
                // Ignorar otras excepciones de carga de clases
            }
        }
    }
}