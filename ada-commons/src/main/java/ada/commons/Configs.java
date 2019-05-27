package ada.commons;

import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public class Configs {

    /**
     * The name of the system variable which is used to store environment name
     */
    public static final String ENV_IRE_ENVIRONMENT = "app.environment";

    /**
     * The name of the system variable which is used to store the role of the ire instance
     */
    public static final String ENV_IRE_ROLE = "app.role";

    /**
     * The default environment name
     */
    public static final String LOCAL_ENVIRONMENT = "local";

    /**
     * The application configuration
     */
    public static final Config application;

    /**
     * Environment variables
     */
    public static final Config environment = ConfigFactory.systemEnvironment();

    /**
     * System variables/ JVM arguments
     */
    public static final Config system = ConfigFactory.systemProperties();

    private static final Logger LOG = LoggerFactory.getLogger(Configs.class);

    static {
        /*
         * Configurations are loaded in the order as provided below, the latter ones override the ones before.
         */
        application = ConfigBuilder
            .create()
            .withEnvironmentAwareness()
            .withRoleAwareness()
            .withEnvironmentRoleAwareness()
            .withSecureConfig()
            .withEnvironmentVariables()
            .withSystemProperties()
            .getConfig();
    }

    /**
     * A simple helper function to return a {@link Config} object as human readable map.
     *
     * @param config The config to be transformed
     * @return A map including all configuration key-value-pairs
     */
    public static Map<String, String> asMap(Config config) {
        Map<String, String> configs = Maps.newLinkedHashMap();
        config.entrySet().forEach(entry -> {
            try {
                configs.put(entry.getKey(), entry.getValue().unwrapped().toString());
            } catch (ConfigException.NotResolved e) {
                configs.put(entry.getKey(), "<Not Resolved>");
            }
        });
        return configs;
    }

    /**
     * Returns a config object in a human-readable string.
     *
     * @param config The config object to print
     * @return A human readable string including all config values.
     */
    public static String asString(Config config) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> configs = asMap(config);

        for (String key : configs.keySet().stream().sorted().collect(Collectors.toList())) {
            sb
                .append(key)
                .append(": ")
                .append(configs.get(key))
                .append("\n");
        }

        return sb.toString();
    }

    /**
     * Helper class to allow declarative definition of config file loading order.
     */
    private static class ConfigBuilder {

        private static final String environmentName = system.hasPath(ENV_IRE_ENVIRONMENT) ?
            system.getString(ENV_IRE_ENVIRONMENT) : LOCAL_ENVIRONMENT;

        private static final String roleName = system.hasPath(ENV_IRE_ROLE) ?
            system.getString(ENV_IRE_ROLE) : null;

        /**
         * The actual configuration which is built by the {@link ConfigBuilder} instance.
         */
        private final Config config;

        /**
         * Creates a new instance.
         *
         * @param config The actual configuration which is built by the {@link ConfigBuilder} instance
         */
        private ConfigBuilder(Config config) {
            this.config = config;
        }

        /**
         * Creates a new config builder; default configuration will be loaded first.
         * <p>
         * The environment name can be set in the system variable/ JVM argument {@link ConfigBuilder#ENV_IRE_ENVIRONMENT}.
         *
         * @return The {@link ConfigBuilder} instance
         */
        static ConfigBuilder create() {
            LOG.info("Loading configuration from default resources 'application.conf' and 'reference.conf'");
            return new ConfigBuilder(ConfigFactory.load());
        }

        /**
         * Finalizes the configuration object and returns it.
         *
         * @return The {@link Config} instance assembled by the builder.
         */
        public Config getConfig() {
            return config.resolve();
        }

        /**
         * Creates a new configuration which overrides existing values with environment specific config file.
         *
         * @return A new {@link ConfigBuilder} instance
         */
        public ConfigBuilder withEnvironmentAwareness() {
            final String envConfigFilePath = String.format("application.%s.conf", environmentName);
            return withResource(envConfigFilePath);
        }

        /**
         * Creates a new configuration which overrides existing values with configuration file from resources.
         *
         * @param resourceFileName The filename/ uri of the resource file
         * @return A new {@link ConfigBuilder} instance
         */
        public ConfigBuilder withResource(String resourceFileName) {
            if (this.getClass().getResource(String.format("/%s", resourceFileName)) != null) {
                LOG.info(String.format("Loading configuration from resource file '%s'", resourceFileName));
                return withOverwrites(ConfigFactory.parseResources(resourceFileName));
            } else {
                LOG.debug(String.format("Configuration resource with name '%s' does not exist - Will be skipped", resourceFileName));
                return this;
            }
        }

        /**
         * Creates a new configuration which overrides existing values with new provided {@link Config}.
         *
         * @param config The config file which overwrites existing values
         * @return A new {@link ConfigBuilder} instance
         */
        public ConfigBuilder withOverwrites(Config config) {
            return new ConfigBuilder(config.withFallback(this.config));
        }

        /**
         * Creates a new configuration which overrides existing values with environment specific config file.
         *
         * @return A new {@link ConfigBuilder} instance
         */
        public ConfigBuilder withEnvironmentRoleAwareness() {
            if (roleName != null) {
                final String envConfigFilePath = String.format("application.%s-%s.conf", environmentName, roleName);
                return withResource(envConfigFilePath);
            } else {
                return this;
            }
        }

        /**
         * Creates a new configuration extended by values found in environment variables.
         *
         * @return A new {@link ConfigBuilder} instance
         */
        public ConfigBuilder withEnvironmentVariables() {
            LOG.info("Loading configuration values from environment variables");
            return withOverwrites(ConfigFactory.systemEnvironment());
        }

        /**
         * Creates a new configuration which overrides existing values with role specific config file.
         * <p>
         * The role can be defined in the system variable/ JVM argument {@link ConfigBuilder#ENV_IRE_ROLE}.
         *
         * @return A new {@link ConfigBuilder} instance
         */
        public ConfigBuilder withRoleAwareness() {
            if (roleName != null) {
                final String roleConfigFilePath = String.format("application.%s.conf", roleName);
                return withResource(roleConfigFilePath);
            } else {
                return this;
            }
        }

        /**
         * Checks whether a file 'application.secure.conf' exists in the application's runtime directory, if yes
         * it uses this file as overwrites for the existing config.
         *
         * @return A new {@link ConfigBuilder} instance
         */
        public ConfigBuilder withSecureConfig() {
            final String executionDirectory = system.getString("user.dir");
            final String secureConfigFilePath = String.format("%s/application.secure.conf", executionDirectory);
            final File secureConfigFile = new File(secureConfigFilePath);

            if (secureConfigFile.exists()) {
                LOG.info(String.format("Loading configuration from secure configuration file '%s'", secureConfigFilePath));
                return withOverwrites(ConfigFactory.parseFile(secureConfigFile));
            } else {
                LOG.debug(String.format("Configuration file '%s' does not exist - Will be skipped", secureConfigFilePath));
                return this;
            }
        }

        /**
         * Overwrites existing values with properties passed as system properties.
         *
         * @return A new {@link ConfigBuilder} instance
         */
        public ConfigBuilder withSystemProperties() {
            LOG.info("Loading configuration values from system properties");
            return withOverwrites(ConfigFactory.systemProperties());
        }

    }

}

