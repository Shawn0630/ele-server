package com.ele.server.config;

import com.github.racc.tscg.TypesafeConfig;
import com.google.inject.Inject;

public class SystemConfig {

    private final int serverHttpPort;
    private final int serverInstances;
    private final String apiRoot;
    private final boolean allowCore;
    private final String clusterName;
    private final String uploadsDir;

    @Inject
    public SystemConfig (
            @TypesafeConfig("ele.server.http-port") int serverHttpPort,
            @TypesafeConfig("ele.server.serverInstances") int serverInstances,
            @TypesafeConfig("ele.api.root") String apiRoot,
            @TypesafeConfig("ele.api.allow-core") boolean allowCore,
            @TypesafeConfig("ele.server.cluster.name") String clusterName,
            @TypesafeConfig("ele.server.cluster.uploadsDir") String uploadsDir
    ) {

        this.serverHttpPort = serverHttpPort;
        this.serverInstances = serverInstances;
        this.apiRoot = apiRoot;
        this.allowCore = allowCore;
        this.clusterName = clusterName;
        this.uploadsDir = uploadsDir;
    }

    public String getApiRoot() {
        return this.apiRoot;
    }

    public int getServerHttpPort() {
        return this.serverHttpPort;
    }

    public int getServerInstances() { return this.serverInstances; }

    public boolean getAllowCore() {
        return this.allowCore;
    }

    public String getClusterName() {
        return this.clusterName;
    }

    public String getUploadsDir() {
        return this.uploadsDir;
    }

}
