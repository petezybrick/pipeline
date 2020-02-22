package com.petezybrick.pipeline.filesystem

import java.net.URI

import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FSDataOutputStream, FileSystem, Path}


class HdfsHelper(val hdfsNameNode: String, val hadoopUser: String) {
  val hdfsUri = "hdfs://" + hdfsNameNode + ":9000"
  val pathBase = "/user/" + hadoopUser + "/"

  // ====== Init HDFS File System Object
  val conf: Configuration = new Configuration
  // Set FileSystem URI
  conf.set("fs.defaultFS", hdfsUri)
  // Because of Maven
  // "Nothing"
  conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem")
  conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem")
  // Set HADOOP user
  System.setProperty("HADOOP_USER_NAME", hadoopUser)
  System.setProperty("hadoop.home.dir", "/")
  // Get the filesystem - HDFS
  val fs: FileSystem = FileSystem.get(URI.create(hdfsUri), conf)


  def mkDir(folder: String): Boolean = {
    val newFolderPath: Path = new Path(pathBase + folder)
    if (!fs.exists(newFolderPath)) { // Create new Directory
      fs.mkdirs(newFolderPath)
    } else {
      true
    }
  }

  def rmDir(folder: String): Boolean = {
    val rmFolderPath: Path = new Path(pathBase + folder)
    fs.delete(rmFolderPath, true)
  }

  def rmFile(nameExt: String): Boolean = {
    val rmNameExtPath: Path = new Path(pathBase + nameExt)
    fs.delete(rmNameExtPath, false)
  }

  def writeByteArrayToFile(folderNameExt: String, fileContent: Array[Byte]) = {
    val hdfswritepath: Path = new Path(pathBase + folderNameExt)
    val outputStream: FSDataOutputStream = null
    try {
      fs.create(hdfswritepath)
      outputStream.write(fileContent)
    } finally {
      if (outputStream != null) outputStream.close()
    }
  }

  def writeStringToFile(folderNameExt: String, fileContent: String) = {
    val hdfswritepath: Path = new Path(pathBase + folderNameExt)
    var outputStream: FSDataOutputStream = null
    try {
      outputStream = fs.create(hdfswritepath)
      outputStream.writeBytes(fileContent)
      //fs.close()
    } finally {
      if (outputStream != null) {
       // outputStream.flush()
        outputStream.close()
      }
    }
  }

  def readFileToByteArray(folderNameExt: String): Array[Byte] = {
    val hdfsreadpath: Path = new Path(pathBase + folderNameExt)
    val inputStream: FSDataInputStream = fs.open(hdfsreadpath)
    val result = IOUtils.toByteArray(inputStream)
    inputStream.close
    fs.close
    result
  }

  def readFileToString(folderNameExt: String): String = {
    val hdfsreadpath: Path = new Path(pathBase + folderNameExt)
    // Init input stream
    val inputStream: FSDataInputStream = fs.open(hdfsreadpath)
    // Classical input stream usage
    val result = IOUtils.toString(inputStream, "UTF-8")
    inputStream.close
    fs.close
    result
  }

}
