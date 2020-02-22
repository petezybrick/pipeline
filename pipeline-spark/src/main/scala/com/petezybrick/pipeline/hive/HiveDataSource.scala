package com.petezybrick.pipeline.hive

import java.sql.{Connection, DriverManager}

object HiveDataSource extends Serializable {
  var jdbcUrl : String = ""
  var jdbcUser : String = ""
  var jdbcPassword : String = ""
  var jdbcClassName : String = "org.apache.hive.jdbc.HiveDriver"

  def setJdbcParms( _jdbcUrl : String, _jdbcUser : String, _jdbcPassword : String ) {
    jdbcUrl = _jdbcUrl
    jdbcUser = _jdbcUser
    jdbcPassword = _jdbcPassword
  }

  def getConnection() : Connection = {
    //Class.forName(jdbcClassName)
    println("+++ jdbcUrl " + jdbcUrl)
    DriverManager.getConnection( jdbcUrl, jdbcUser, jdbcPassword)
  }

}
