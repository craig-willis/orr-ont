package org.mmisw.orr.ont

import com.typesafe.scalalogging.slf4j.Logging
import com.typesafe.config.{ConfigFactory, Config}
import com.mongodb.casbah.Imports._
import com.mongodb.ServerAddress


/**
 *
 */
class Db(mongoConfig: Config) extends AnyRef with Logging {

  private[this] var mcOpt: Option[MongoClient] = None

  logger.info(s"mongoConfig = ${ConfigFactory.parseString(
    "pw=\"*\"\npw_special=\"*\"").withFallback(mongoConfig)}")

  val host = mongoConfig.getString("host")
  val port = mongoConfig.getInt(   "port")
  val db   = mongoConfig.getString("db")

  val serverAddress = new ServerAddress(host, port)

  private[this] val mongoClient: MongoClient = if (mongoConfig.hasPath("user")) {
    val user = mongoConfig.getString("user")
    val pw   = mongoConfig.getString("pw")
    logger.info(s"connecting to $host:$port/$db using credentials ...")
    val credential = MongoCredential(user, db, pw.toCharArray)
    MongoClient(serverAddress, List(credential))
  }
  else {
    logger.info(s"connecting to $host:$port/$db with no credentials ...")
    MongoClient(serverAddress)
  }

  private[this] val mongoClientDb = mongoClient(db)

  val ontologiesColl  = mongoClientDb(mongoConfig.getString("ontologies"))

  mcOpt = Some(mongoClient)

  def destroy() {
    logger.info("Closing MongoClient ...")
    mcOpt foreach { _.close() }
  }
}
