package grover.utils

import com.typesafe.config.ConfigFactory
import grover.GroverI
import grover.impl.flink.Grover
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkContext, SparkConf}


object Config {

  Logger.getLogger("org").setLevel(Level.ERROR)
  Logger.getLogger("akka").setLevel(Level.ERROR)


  private val config =  ConfigFactory.load()

  @transient val sparkConf = new SparkConf()
    .setMaster(config.getString("application.spark.master-uri"))
    .setAppName(config.getString("application.spark.app-name"))

  val sparkContext = new SparkContext(sparkConf)

  //val flinkEnvironment = ExecutionEnvironment.createLocalEnvironment()
  val flinkEnvironment = ExecutionEnvironment.getExecutionEnvironment


  object app {

    val appConf = config.getConfig("app")

    val systemName = appConf.getString("systemName")
    val interface = appConf.getString("interface")
    val port = appConf.getInt("port")
    val groverServiceName = appConf.getString("groverServiceName")

    val groverImplType = appConf.getString("impl")

  }

  val sparkImpl = grover.impl.spark.Grover
  val flinkImpl = grover.impl.flink.Grover
  val aggregateMessagesImpl = grover.impl.aggregateMessages.Grover

  def getImpl():GroverI = {

    println("Config.app.groverImplType=" + Config.app.groverImplType)

    Config.app.groverImplType match {
      case "spark" => sparkImpl
      case "flink" => flinkImpl
      case "aggregateMessages" => aggregateMessagesImpl
//      case _ => throw new Exception("IMPL_TYPE not set")
      case _ => sparkImpl

    }

  }

}
