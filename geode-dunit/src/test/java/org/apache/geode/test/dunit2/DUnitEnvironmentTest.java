package org.apache.geode.test.dunit2;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DUnitEnvironmentTest {

  @Test
  public void getVMGetsRequestedVM() {
    DUnitEnvironment environment = new DUnitEnvironment();
    assertThat(environment.getVM(1)).isNotNull();
    assertThat(environment.getVM(1).getId()).isEqualTo(1);
  }

  @Test
  public void launchVMRunsJavaProcess() {

  }

}