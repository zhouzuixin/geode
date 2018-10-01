package org.apache.geode.test.dunit2;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class VMTest {

  @Rule
  public SystemOutRule systemOutRule = new SystemOutRule();
  @Test
  public void testLaunchVM() throws IOException {
    VM newVM = new VM();
    VM launchedVM = new VM().launch();

    assertThat(launchedVM.isAlive()).isTrue();
    assertThat(newVM.isAlive()).isFalse();
  }

  @Test
  public void testStopVMStopsIt() throws IOException {
    VM runningVM = new VM().launch();
    runningVM.stop();
    assertThat(runningVM.isAlive()).isFalse();
  }

  @Test
  public void  testInvokeReturnsValueFromCallable() throws IOException {
    final String message = "Hello! World";

    VM vm = new VM().launch();
    String result = vm.invoke(() -> message);

    assertThat(result).isEqualTo(message);
  }

}