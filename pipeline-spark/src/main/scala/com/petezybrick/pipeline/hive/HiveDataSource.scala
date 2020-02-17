package com.petezybrick.pipeline.hive

import java.sql.{Connection, DriverManager}

object HiveDataSource extends Serializable {
  var jdbcUrl : String = ""
  var jdbcUser : String = ""
  var jdbcPassword : String = ""
  var jdbcClassName : String = "org.apache.hive.jdbc.HiveDriver"

  def setJdbcParms( _jdbcUrl : String, _jdbcUser : String, _jdbcPassword : String ) {
    jdbcUrl = _jdbcUrl
  }

  def getConnection() : Connection = {
    Class.forName(jdbcClassName)
    DriverManager.getConnection( jdbcUrl, jdbcUser, jdbcPassword)
  }

}
