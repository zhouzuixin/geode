package org.apache.geode.test.dunit2;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import hydra.MethExecutorResult;

import org.apache.geode.test.dunit.NamedCallable;
import org.apache.geode.test.dunit.NamedRunnable;
import org.apache.geode.test.dunit.RMIException;
import org.apache.geode.test.dunit.SerializableCallable;
import org.apache.geode.test.dunit.SerializableCallableIF;
import org.apache.geode.test.dunit.SerializableRunnable;
import org.apache.geode.test.dunit.SerializableRunnableIF;
import org.apache.geode.test.dunit.standalone.RemoteDUnitVMIF;

public class VM {
  private ChildVMLauncher launcher;
  private int id = 1;
  private boolean alive = false;

  private Process process;
  private RemoteDUnitVMIF stub;

  public VM() {

  }
  public VM(DUnitEnvConfig config, ChildVMLauncher launcher) {
    this.launcher = launcher;
  }

  public int getId() {
    return id;
  }

  public VM launch() throws IOException {
    alive = true;
    process = launcher.launch(1, "0", "0", "", 0);
    return this;
  }

  public void stop() {
    alive = false;
  }

  public boolean isAlive() {
    return alive;
  }

  /**
   * Invokes the {@code run} method of a {@link Runnable} in this {@code VM}. Recall that
   * {@code run} takes no arguments and has no return value.
   *
   * @param runnable The {@code Runnable} to be run
   * @param name The name of the {@code Runnable}, which will be logged in DUnit output
   *
   * @see SerializableRunnable
   */
  public void invoke(final String name, final SerializableRunnableIF runnable) {
    invoke(new NamedRunnable(name, runnable), "run");
  }

  /**
   * Invokes the {@code run} method of a {@link Runnable} in this {@code VM}. Recall that
   * {@code run} takes no arguments and has no return value.
   *
   * @param runnable The {@code Runnable} to be run
   *
   * @see SerializableRunnable
   */
  public void invoke(final SerializableRunnableIF runnable) {
    invoke(runnable, "run");
  }

  /**
   * Invokes the {@code call} method of a {@link Callable} in this {@code VM}.
   *
   * @param callable The {@code Callable} to be run
   * @param name The name of the {@code Callable}, which will be logged in DUnit output
   *
   * @see SerializableCallable
   */
  public <V> V invoke(final String name, final SerializableCallableIF<V> callable) {
    return invoke(new NamedCallable<>(name, callable), "call");
  }

  /**
   * Invokes the {@code call} method of a {@link Callable} in this {@code VM}.
   *
   * @param callable The {@code Callable} to be run
   *
   * @see SerializableCallable
   */
  public <V> V invoke(final SerializableCallableIF<V> callable) {
    return invoke(callable, "call");
  }

  private <V> V invoke(final Object targetObject, final String methodName) {
    return invoke(targetObject, methodName, new Object[0]);
  }

  private <V> V invoke(final Object targetObject, final String methodName, final Object[] args) {
    if (!alive) {
      throw new RMIException(this, targetObject.getClass().getName(), methodName,
          new IllegalStateException("VM not available: " + this));
    }

    try {
      MethExecutorResult result;
      if (args == null) {
        result = stub.executeMethodOnObject(targetObject, methodName);
      } else {
        result = stub.executeMethodOnObject(targetObject, methodName, args);
      }
      if (!result.exceptionOccurred()) {
        return (V) result.getResult();

      } else {
        throw new RMIException(this, targetObject.getClass().getName(), methodName,
            result.getException(), result.getStackTrace());
      }
    } catch (RemoteException exception) {
      throw new RMIException(this, targetObject.getClass().getName(), methodName, exception);
    }
  }
}
