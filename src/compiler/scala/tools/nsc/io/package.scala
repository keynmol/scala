/* NSC -- new Scala compiler
 * Copyright 2005-2010 LAMP/EPFL
 * @author Paul Phillips
 */

package scala.tools.nsc

import java.util.concurrent.{ Future, Callable, Executors, ThreadFactory }
import java.util.{ Timer, TimerTask }

package object io {
  def runnable(body: => Unit): Runnable       = new Runnable { override def run() = body }
  def callable[T](body: => T): Callable[T]    = new Callable[T] { override def call() = body }
  def spawn[T](body: => T): Future[T]         = Executors.newSingleThreadExecutor() submit callable[T](body)
  def submit(runnable: Runnable)              = Executors.newSingleThreadExecutor() submit runnable
  def runnableFn(f: () => Unit): Runnable     = runnable(f())
  def callableFn[T](f: () => T): Callable[T]  = callable(f())
  def spawnFn[T](f: () => T): Future[T]       = spawn(f())

  def newConfiguredExecutor(f: Thread => Unit) = {
    Executors.newCachedThreadPool(new ThreadFactory {
      def newThread(r: Runnable) = {
        val t = Executors.defaultThreadFactory().newThread(r)
        f(t)
        t
      }
    })
  }
  def newDaemonThreadExecutor() = newConfiguredExecutor(_ setDaemon true)

  // Create, start, and return a background thread
  // If isDaemon is true, it is marked as daemon (and will not interfere with JVM shutdown)
  def daemonize(isDaemon: Boolean)(body: => Unit): Thread = {
    val thread = new Thread(runnable(body))
    thread setDaemon isDaemon
    thread.start
    thread
  }

  // Set a timer to execute the given code.
  def timer(seconds: Int)(body: => Unit): Timer = {
    val alarm = new Timer(true) // daemon
    val tt    = new TimerTask { def run() = body }

    alarm.schedule(tt, seconds * 1000)
    alarm
  }
}