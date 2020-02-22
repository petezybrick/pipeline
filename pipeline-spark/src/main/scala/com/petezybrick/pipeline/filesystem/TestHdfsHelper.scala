package com.petezybrick.pipeline.filesystem

object TestHdfsHelper {

  def main(args: Array[String]): Unit = {
    try {
      val hdfsHelper : HdfsHelper = new HdfsHelper(hdfsNameNode = "pipeline-hive-namenode", hadoopUser = "pipeline" )

      println("rm folder")
      hdfsHelper.rmDir("folder_1")

      println("mkdir")
      hdfsHelper.mkDir("folder_1")
      println("write 1")
      hdfsHelper.writeStringToFile("folder_1/file1", "first")
      println("write 2")
      hdfsHelper.writeStringToFile("folder_1/file2", "second")
      println("write 3")
      hdfsHelper.writeStringToFile("folder_1/file3", "third")
      println("rm f2")
      hdfsHelper.rmFile("folder_1/file2")
      println("rm folder")
      hdfsHelper.rmDir("folder_1")

    } catch {
      case ex: Exception => {
        println("Exception " + ex)
      }
    }
  }
}
