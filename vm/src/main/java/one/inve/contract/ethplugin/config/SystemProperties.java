/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package one.inve.contract.ethplugin.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import one.inve.contract.ethplugin.config.blockchain.OlympicConfig;
import one.inve.contract.ethplugin.config.net.*;
import one.inve.contract.ethplugin.core.Genesis;
import one.inve.contract.ethplugin.core.genesis.GenesisConfig;
import one.inve.contract.ethplugin.core.genesis.GenesisJson;
import one.inve.contract.ethplugin.core.genesis.GenesisLoader;
import one.inve.contract.ethplugin.crypto.ECKey;
import one.inve.contract.ethplugin.util.ByteUtil;
import one.inve.contract.ethplugin.util.Utils;
import one.inve.contract.ethplugin.validator.BlockCustomHashRule;
import one.inve.contract.ethplugin.validator.BlockHeaderValidator;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

/**
 * Utility class to retrieve property values from the ethereumj.conf files
 *
 * The properties are taken from different sources and merged in the following order
 * (the config option from the next source overrides option from previous):
 * - resource ethereumj.conf : normally used as a reference config with default values
 *          and shouldn't be changed
 * - system property : each config entry might be altered via -D VM option
 * - [user dir]/config/ethereumj.conf
 * - config specified with the -Dethereumj.conf.file=[file.conf] VM option
 * - CLI options
 *
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
public class SystemProperties {
    private static Logger logger = LoggerFactory.getLogger("general");

    private static SystemProperties CONFIG;
    private static boolean useOnlySpringConfig = false;
    private String generatedNodePrivateKey;

    /**
     * Returns the static config instance. If the config is passed
     * as a Spring bean by the application this instance shouldn't
     * be used
     * This method is mainly used for testing purposes
     * (Autowired fields are initialized with this static instance
     * but when running within Spring context they replaced with the
     * bean config instance)
     */
    public static SystemProperties getDefault() {
        return useOnlySpringConfig ? null : getSpringDefault();
    }

    static SystemProperties getSpringDefault() {
        if (CONFIG == null) {
            CONFIG = new SystemProperties();
        }
        return CONFIG;
    }

    static boolean isUseOnlySpringConfig() {
        return useOnlySpringConfig;
    }

    /**
     * Marks config accessor methods which need to be called (for value validation)
     * upon config creation or modification
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ValidateMe {};


    private Config config;

    // mutable options for tests
    private String databaseDir = null;
    private String projectVersion = null;
    protected Integer databaseVersion = null;

    private String genesisInfo = null;

    private GenesisJson genesisJson;
    private BlockchainNetConfig blockchainConfig;
    private Genesis genesis;
    private Boolean vmTrace;
    private Boolean recordInternalTransactionsData;

    private final ClassLoader classLoader;

    private GenerateNodeIdStrategy generateNodeIdStrategy = null;

    public SystemProperties() {
        this(ConfigFactory.empty());
    }

    public SystemProperties(File configFile) {
        this(ConfigFactory.parseFile(configFile));
    }

    public SystemProperties(String configResource) {
        this(ConfigFactory.parseResources(configResource));
    }

    public SystemProperties(Config apiConfig) {
        this(apiConfig, SystemProperties.class.getClassLoader());
    }

    public SystemProperties(Config apiConfig, ClassLoader classLoader) {
        try {
            this.classLoader = classLoader;

            Config javaSystemProperties = ConfigFactory.load("no-such-resource-only-system-props");
            Config referenceConfig = ConfigFactory.parseResources("ethereumj.conf");
           // logger.info("Config (" + (referenceConfig.entrySet().size() > 0 ? " yes " : " no  ") + "): default properties from resource 'ethereumj.conf'");
            String res = System.getProperty("ethereumj.conf.res");
            Config cmdLineConfigRes = mergeConfigs(res, ConfigFactory::parseResources);
           // logger.info("Config (" + (cmdLineConfigRes.entrySet().size() > 0 ? " yes " : " no  ") + "): user properties from -Dethereumj.conf.res resource(s) '" + res + "'");
            Config userConfig = ConfigFactory.parseResources("user.conf");
          //  logger.info("Config (" + (userConfig.entrySet().size() > 0 ? " yes " : " no  ") + "): user properties from resource 'user.conf'");
            File userDirFile = new File(System.getProperty("user.dir"), "/config/ethereumj.conf");
            Config userDirConfig = ConfigFactory.parseFile(userDirFile);
          //  logger.info("Config (" + (userDirConfig.entrySet().size() > 0 ? " yes " : " no  ") + "): user properties from file '" + userDirFile + "'");
            Config testConfig = ConfigFactory.parseResources("test-ethereumj.conf");
          //  logger.info("Config (" + (testConfig.entrySet().size() > 0 ? " yes " : " no  ") + "): test properties from resource 'test-ethereumj.conf'");
            Config testUserConfig = ConfigFactory.parseResources("test-user.conf");
          //  logger.info("Config (" + (testUserConfig.entrySet().size() > 0 ? " yes " : " no  ") + "): test properties from resource 'test-user.conf'");
            String file = System.getProperty("ethereumj.conf.file");
            Config cmdLineConfigFile = mergeConfigs(file, s -> ConfigFactory.parseFile(new File(s)));
          //  logger.info("Config (" + (cmdLineConfigFile.entrySet().size() > 0 ? " yes " : " no  ") + "): user properties from -Dethereumj.conf.file file(s) '" + file + "'");
          //  logger.info("Config (" + (apiConfig.entrySet().size() > 0 ? " yes " : " no  ") + "): config passed via constructor");
            config = apiConfig
                    .withFallback(cmdLineConfigFile)
                    .withFallback(testUserConfig)
                    .withFallback(testConfig)
                    .withFallback(userDirConfig)
                    .withFallback(userConfig)
                    .withFallback(cmdLineConfigRes)
                    .withFallback(referenceConfig);

          //  logger.debug("Config trace: " + config.root().render(ConfigRenderOptions.defaults().setComments(false).setJson(false)));

            config = javaSystemProperties.withFallback(config)
                    .resolve();     // substitute variables in config if any
            validateConfig();

            // There could be several files with the same name from other packages,
            // "version.properties" is a very common name
            List<InputStream> iStreams = loadResources("version.properties", this.getClass().getClassLoader());
          for (InputStream is : iStreams) {
            Properties props = new Properties();
            props.load(is);
            if (props.getProperty("versionNumber") == null || props.getProperty("databaseVersion") == null) {
              continue;
            }
            this.projectVersion = props.getProperty("versionNumber");
            this.projectVersion = this.projectVersion.replaceAll("'", "");

            if (this.projectVersion == null) this.projectVersion = "-.-.-";

            this.databaseVersion = Integer.valueOf(props.getProperty("databaseVersion"));

            this.generateNodeIdStrategy = new GetNodeIdFromPropsFile(databaseDir())
                .withFallback(new GenerateNodeIdRandomly(databaseDir()));
            break;
            }
        } catch (Exception e) {
            logger.error("Can't read config.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads resources using given ClassLoader assuming, there could be several resources
     * with the same name
     */
    public static List<InputStream> loadResources(
            final String name, final ClassLoader classLoader) throws IOException {
        final List<InputStream> list = new ArrayList<InputStream>();
        final Enumeration<URL> systemResources =
                (classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader)
                        .getResources(name);
        while (systemResources.hasMoreElements()) {
            list.add(systemResources.nextElement().openStream());
        }
        return list;
    }

    public Config getConfig() {
        return config;
    }

    private void validateConfig() {
        for (Method method : getClass().getMethods()) {
            try {
                if (method.isAnnotationPresent(ValidateMe.class)) {
                    method.invoke(this);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error validating config method: " + method, e);
            }
        }
    }

    /**
     * Builds config from the list of config references in string doing following actions:
     * 1) Splits input by "," to several strings
     * 2) Uses parserFunc to create config from each string reference
     * 3) Merges configs, applying them in the same order as in input, so last overrides first
     * @param input         String with list of config references separated by ",", null or one reference works fine
     * @param parserFunc    Function to apply to each reference, produces config from it
     * @return Merged config
     */
    protected Config mergeConfigs(String input, Function<String, Config> parserFunc) {
        Config config = ConfigFactory.empty();
        if (input != null && !input.isEmpty()) {
            String[] list = input.split(",");
            for (int i = list.length - 1; i >= 0; --i) {
                config = config.withFallback(parserFunc.apply(list[i]));
            }
        }

        return config;
    }

    public <T> T getProperty(String propName, T defaultValue) {
        if (!config.hasPath(propName)) return defaultValue;
        String string = config.getString(propName);
        if (string.trim().isEmpty()) return defaultValue;
        return (T) config.getAnyRef(propName);
    }

    public BlockchainNetConfig getBlockchainConfig() {
        if (blockchainConfig == null) {
            GenesisJson genesisJson = getGenesisJson();
            if (genesisJson.getConfig() != null && genesisJson.getConfig().isCustomConfig()) {
                blockchainConfig = new JsonNetConfig(genesisJson.getConfig());
            } else {
                if (config.hasPath("blockchain.config.name") && config.hasPath("blockchain.config.class")) {
                    throw new RuntimeException("Only one of two options should be defined: 'blockchain.config.name' and 'blockchain.config.class'");
                }
                if (config.hasPath("blockchain.config.name")) {
                    switch (config.getString("blockchain.config.name")) {
                        case "main":
                            blockchainConfig = new MainNetConfig();
                            break;
                        case "olympic":
                            blockchainConfig = new OlympicConfig();
                            break;
                        case "morden":
                            blockchainConfig = new MordenNetConfig();
                            break;
                        case "ropsten":
                            blockchainConfig = new RopstenNetConfig();
                            break;
                        case "testnet":
                            blockchainConfig = new TestNetConfig();
                            break;
                        default:
                            throw new RuntimeException("Unknown value for 'blockchain.config.name': '" + config.getString("blockchain.config.name") + "'");
                    }
                } else {
                    String className = config.getString("blockchain.config.class");
                    try {
                        Class<? extends BlockchainNetConfig> aClass = (Class<? extends BlockchainNetConfig>) classLoader.loadClass(className);
                        blockchainConfig = aClass.newInstance();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("The class specified via blockchain.config.class '" + className + "' not found", e);
                    } catch (ClassCastException e) {
                        throw new RuntimeException("The class specified via blockchain.config.class '" + className + "' is not instance of one.inve.contract.ethplugin.config.BlockchainForkConfig", e);
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException("The class specified via blockchain.config.class '" + className + "' couldn't be instantiated (check for default constructor and its accessibility)", e);
                    }
                }
            }

            if (genesisJson.getConfig() != null && genesisJson.getConfig().headerValidators != null) {
                for (GenesisConfig.HashValidator validator : genesisJson.getConfig().headerValidators) {
                    BlockHeaderValidator headerValidator = new BlockHeaderValidator(new BlockCustomHashRule(ByteUtil.hexStringToBytes(validator.hash)));
                    blockchainConfig.getConfigForBlock(validator.number).headerValidators().add(
                            Pair.of(validator.number, headerValidator));
                }
            }
        }
        return blockchainConfig;
    }

    @ValidateMe
    public boolean databaseFromBackup() {
        return config.getBoolean("database.fromBackup");
    }

    @ValidateMe
    public int databasePruneDepth() {
        return config.getBoolean("database.prune.enabled") ? config.getInt("database.prune.maxDepth") : -1;
    }

    @ValidateMe
    public Integer traceStartBlock() {
        return config.getInt("trace.startblock");
    }

    @ValidateMe
    public String dumpStyle() {
        return config.getString("dump.style");
    }

    @ValidateMe
    public int dumpBlock() {
        return config.getInt("dump.block");
    }

    @ValidateMe
    public String databaseDir() {
        return databaseDir == null ? config.getString("database.dir") : databaseDir;
    }

    @ValidateMe
    public boolean vmTrace() {
        return vmTrace == null ? (vmTrace = config.getBoolean("vm.structured.trace")) : vmTrace;
    }

    @ValidateMe
    public String vmTraceDir() {
        return config.getString("vm.structured.dir");
    }

    public String customSolcPath() {
        return config.hasPath("solc.path") ? config.getString("solc.path"): null;
    }

    public String privateKey() {
        if (config.hasPath("peer.privateKey")) {
            String key = config.getString("peer.privateKey");
            if (key.length() != 64 || !Utils.isHexEncoded(key)) {
                throw new RuntimeException("The peer.privateKey needs to be Hex encoded and 32 byte length");
            }
            return key;
        } else {
            return getGeneratedNodePrivateKey();
        }
    }

    private String getGeneratedNodePrivateKey() {
        if (generatedNodePrivateKey == null) {
            generatedNodePrivateKey = generateNodeIdStrategy.getNodePrivateKey();
        }
        return generatedNodePrivateKey;
    }

    public ECKey getMyKey() {
        return ECKey.fromPrivate(Hex.decode(privateKey()));
    }

    /**
     *  Home NodeID calculated from 'peer.privateKey' property
     */
    public byte[] nodeId() {
        return getMyKey().getNodeId();
    }

    @ValidateMe
    public String getKeyValueDataSource() {
        return config.getString("keyvalue.datasource");
    }

    @ValidateMe
    public String genesisInfo() {
        return genesisInfo == null ? config.getString("genesis") : genesisInfo;
    }

    @ValidateMe
    public String getCryptoProviderName() {
        return config.getString("crypto.providerName");
    }

    @ValidateMe
    public boolean recordInternalTransactionsData() {
        if (recordInternalTransactionsData == null) {
            recordInternalTransactionsData = config.getBoolean("record.internal.transactions.data");
        }
        return recordInternalTransactionsData;
    }

    @ValidateMe
    public String getHash256AlgName() {
        return config.getString("crypto.hash.alg256");
    }

    @ValidateMe
    public String getHash512AlgName() {
        return config.getString("crypto.hash.alg512");
    }

    private GenesisJson getGenesisJson() {
        if (genesisJson == null) {
            genesisJson = GenesisLoader.loadGenesisJson(this, classLoader);
        }
        return genesisJson;
    }

    public Genesis getGenesis() {
        if (genesis == null) {
            genesis = GenesisLoader.parseGenesis(getBlockchainConfig(), getGenesisJson());
        }
        return genesis;
    }
}
