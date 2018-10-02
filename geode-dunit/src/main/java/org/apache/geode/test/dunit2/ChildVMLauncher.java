package org.apache.geode.test.dunit2;

import static org.apache.geode.distributed.ConfigurationProperties.ENABLE_NETWORK_PARTITION_DETECTION;
import static org.apache.geode.distributed.ConfigurationProperties.LOG_LEVEL;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.distributed.internal.InternalLocator;
import org.apache.geode.test.dunit.VM;
import org.apache.geode.test.dunit.standalone.ChildVM;
import org.apache.geode.test.dunit.standalone.DUnitLauncher;
import org.apache.geode.test.dunit.standalone.VersionManager;

public class ChildVMLauncher {

  private VersionManager versionManager;

  private int debugPort = Integer.getInteger("dunit.debug.basePort", 0);
  private int suspendVM = Integer.getInteger("dunit.debug.suspendVM", -100);

  public ChildVMLauncher(VersionManager versionManager) {
    this.versionManager = versionManager;
  }

  public Process launch(int vmId, String workingDirPath, String geodeVersion, String registryHost, int registryPort)
    throws IOException {
    File workingDir = new File(workingDirPath);

    if (!workingDir.exists()) {
      workingDir.mkdirs();
    }

    String[] cmd = buildJavaCommand(vmId, geodeVersion, registryHost, registryPort);
    System.out.println("Executing " + Arrays.toString(cmd));

    // TODO - delete directory contents, preferably with commons io FileUtils
    Process process = Runtime.getRuntime().exec(cmd, null, workingDir);
    linkStreams(geodeVersion, vmId, process, process.getErrorStream(), System.err);
    linkStreams(geodeVersion, vmId, process, process.getInputStream(), System.out);
    return process;
  }

  private String[] buildJavaCommand(int vmId, String geodeVersion, String registryHost, int registryPort) {

    String cmd = Paths.get(System.getProperty("java.home"), "bin", "java").toString();

    String dunitClasspath = System.getProperty("java.class.path");
    String separator = File.separator;
    String classPath;
    if (VersionManager.isCurrentVersion(geodeVersion)) {
      classPath = dunitClasspath;
    } else {
      // remove current-version product classes and resources from the classpath
      String buildDir = separator + "geode-core" + separator + "build" + separator;

      String mainClasses = buildDir + "classes" + separator + "main";
      dunitClasspath = removeFromPath(dunitClasspath, mainClasses);

      dunitClasspath = removeFromPath(dunitClasspath, "geode-core/out/production");

      String mainResources = buildDir + "resources" + separator + "main";
      dunitClasspath = removeFromPath(dunitClasspath, mainResources);

      String generatedResources = buildDir + "generated-resources" + separator + "main";
      dunitClasspath = removeFromPath(dunitClasspath, generatedResources);

      buildDir = separator + "geode-common" + separator + "build" + separator + "classes"
          + separator + "main";
      dunitClasspath = removeFromPath(dunitClasspath, buildDir);

      buildDir = separator + "geode-json" + separator + "build" + separator + "classes" + separator
          + "main";
      dunitClasspath = removeFromPath(dunitClasspath, buildDir);

      classPath = versionManager.getClasspath(geodeVersion) + File.pathSeparator + dunitClasspath;
    }

    String jdkDebug = "";
    if (debugPort > 0) {
      jdkDebug += ",address=" + debugPort;
      debugPort++;
    }

    String jdkSuspend = vmId == suspendVM ? "y" : "n"; // ignore version
    ArrayList<String> cmds = new ArrayList<String>();
    cmds.add(cmd);
    cmds.add("-classpath");
    String jreLib = separator + "jre" + separator + "lib" + separator;
    classPath = removeFromPath(classPath, jreLib);
    cmds.add(classPath);
    cmds.add("-D" + DUnitLauncher.RMI_PORT_PARAM + "=" + registryPort);
    cmds.add("-D" + DUnitLauncher.VM_NUM_PARAM + "=" + vmId);
    cmds.add("-D" + DUnitLauncher.VM_VERSION_PARAM + "=" + geodeVersion);
    cmds.add("-D" + DUnitLauncher.WORKSPACE_DIR_PARAM + "=" + new File(".").getAbsolutePath());
    if (vmId >= 0) { // let the locator print a banner
      if (geodeVersion.equals(VersionManager.CURRENT_VERSION)) { // enable the banner for older versions
        cmds.add("-D" + InternalLocator.INHIBIT_DM_BANNER + "=true");
      }
    } else {
      // most distributed unit tests were written under the assumption that network partition
      // detection is disabled, so we turn it off in the locator. Tests for network partition
      // detection should create a separate locator that has it enabled
      cmds.add(
          "-D" + DistributionConfig.GEMFIRE_PREFIX + ENABLE_NETWORK_PARTITION_DETECTION + "=false");
      cmds.add(
          "-D" + DistributionConfig.GEMFIRE_PREFIX + "allow_old_members_to_join_for_testing=true");
    }
    cmds.add("-D" + LOG_LEVEL + "=" + DUnitLauncher.logLevel);
    if (DUnitLauncher.LOG4J != null) {
      cmds.add("-Dlog4j.configurationFile=" + DUnitLauncher.LOG4J);
    }
    cmds.add("-Djava.library.path=" + System.getProperty("java.library.path"));
    cmds.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=" + jdkSuspend + jdkDebug);
    cmds.add("-XX:+HeapDumpOnOutOfMemoryError");
    cmds.add("-Xmx512m");
    cmds.add("-D" + DistributionConfig.GEMFIRE_PREFIX + "DEFAULT_MAX_OPLOG_SIZE=10");
    cmds.add("-D" + DistributionConfig.GEMFIRE_PREFIX + "disallowMcastDefaults=true");
    cmds.add("-D" + DistributionConfig.RESTRICT_MEMBERSHIP_PORT_RANGE + "=true");
    cmds.add("-D" + DistributionConfig.GEMFIRE_PREFIX
        + ConfigurationProperties.VALIDATE_SERIALIZABLE_OBJECTS + "=true");
    cmds.add("-ea");
    cmds.add("-XX:MetaspaceSize=512m");
    cmds.add("-XX:SoftRefLRUPolicyMSPerMB=1");
    cmds.add(ChildVM.class.getName());
    String[] rst = new String[cmds.size()];
    cmds.toArray(rst);

    return rst;
  }

  private String removeFromPath(String classpath, String partialPath) {
    String[] jars = classpath.split(File.pathSeparator);
    StringBuilder sb = new StringBuilder(classpath.length());
    Boolean firstjar = true;
    for (String jar : jars) {
      if (!jar.contains(partialPath)) {
        if (!firstjar) {
          sb.append(File.pathSeparator);
        }
        sb.append(jar);
        firstjar = false;
      }
    }
    return sb.toString();
  }

  private void linkStreams(final String version, final int vmId, final Process process,
                           final InputStream in, final PrintStream out) {
    final String vmName = "[" + VM.getVMName(version, vmId) + "] ";
    System.out.println("linking IO streams for " + vmName);
    Thread ioTransport = new Thread() {
      public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
          String line = reader.readLine();
          while (line != null) {
            if (line.length() == 0) {
              out.println();
            } else {
              out.print(vmName);
              out.println(line);
            }
            line = reader.readLine();
          }
        } catch (Exception e) {
          if (!process.isAlive()) {
            out.println("Error transporting IO from child process");
            e.printStackTrace(out);
          }
        }
      }
    };

    ioTransport.setDaemon(true);
    ioTransport.start();
  }
}
