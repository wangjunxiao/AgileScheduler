package cn.dlut.core.cmd;

import org.kohsuke.args4j.Option;


/**
 * This class defines all command line settings, their default values, and their
 * getters.
 */
public class CmdLineSettings {
    /**
     * Default Coordinator host.
     */
    public static final String DEFAULT_OF_HOST = "0.0.0.0";
    /**
     * Default Coordinator port.
     */
    public static final Integer DEFAULT_OF_PORT = 6633;
    /**
     * Default value (in seconds) the switch statistics are queried.
     */
    public static final Integer DEFAULT_STATS_REFRESH = 30;
    /**
     * Default number of threads to handle switch connection events.
     */
    public static final Integer DEFAULT_SERVER_THREADS = 32;
    /**
     * Default number of threads to handle controller connection events.
     */
    public static final Integer DEFAULT_CLIENT_THREADS = 32;
    /**
     * Default value if BDDP is used for discovery.
     */
    public static final Boolean DEFAULT_USE_BDDP = false;

    @Option(name = "-p", aliases = "--of-port", metaVar = "INT", usage = "Coordinator OpenFlow listen port")
    private Integer ofPort = CmdLineSettings.DEFAULT_OF_PORT;

    @Option(name = "-h", aliases = "--of-host", metaVar = "String", usage = "Coordinator host")
    private String ofHost = CmdLineSettings.DEFAULT_OF_HOST;

    @Option(name = "--stats-refresh", usage = "Sets what interval to poll statistics with")
    private Integer statsRefresh = CmdLineSettings.DEFAULT_STATS_REFRESH;

    @Option(name = "--ct", aliases = "--client-threads", metaVar = "INT", usage = "Number of threads handles controller connections")
    private Integer clientThreads = CmdLineSettings.DEFAULT_CLIENT_THREADS;

    @Option(name = "--st", aliases = "--server-threads", metaVar = "INT", usage = "Number of threads handles switch connections")
    private Integer serverThreads = CmdLineSettings.DEFAULT_CLIENT_THREADS;

    @Option(name = "--ub", aliases = "--use-bddp", usage = "Use BDDP for network discovery; only use if you know what you are doing.")
    private Boolean useBDDP = CmdLineSettings.DEFAULT_USE_BDDP;

    /**
     * Gets the host Coordinator is running on.
     *
     * @return the OpenFlow host
     */
    public String getOFHost() {
        return this.ofHost;
    }

    /**
     * Gets the port Coordinator is running on.
     *
     * @return the OpenFlow port
     */
    public Integer getOFPort() {
        return this.ofPort;
    }
    
    /**
     * Gets the value (in seconds) the switch statistics are queried.
     *
     * @return the statistics refresh period
     */
    public Integer getStatsRefresh() {
        return this.statsRefresh;
    }

    /**
     * Gets the number of threads used to handle controller connection events.
     *
     * @return the number of client threads used
     */
    public Integer getClientThreads() {
        return this.clientThreads;
    }

    /**
     * Gets the number of threads used to handle switch connection events.
     *
     * @return the number of server threads used
     */
    public Integer getServerThreads() {
        return this.serverThreads;
    }

    /**
     * Checks if BDDP is enabled.
     *
     * @return true if BDDP is enabled, false otherwise
     */
    public Boolean getUseBDDP() {
        return this.useBDDP;
    }

}
