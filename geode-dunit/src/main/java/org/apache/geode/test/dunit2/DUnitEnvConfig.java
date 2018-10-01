package org.apache.geode.test.dunit2;

public class DUnitEnvConfig {
  private String registryHost;
  private int registryPort;

  public DUnitEnvConfig(String registryHost, int registryPort) {
    this.registryHost = registryHost;
    this.registryPort = registryPort;
  }

  public String getRegistryHost() {
    return registryHost;
  }

  public void setRegistryHost(String registryHost) {
    this.registryHost = registryHost;
  }

  public int getRegistryPort() {
    return registryPort;
  }

  public void setRegistryPort(int registryPort) {
    this.registryPort = registryPort;
  }
}
