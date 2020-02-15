import java.net.URI

import com.petezybrick.pipeline.spark.basic.HdfsHelper
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FSDataOutputStream, FileSystem, Path}

object TestHdfsHelper {

  def main(args: Array[String]): Unit = {
    try {
      val hdfsHelper : HdfsHelper = new HdfsHelper(hdfsNameNode = "pipeline-hive-namenode", hadoopUser = "pipeline")

      hdfsHelper.mkDir("folder_1")
      hdfsHelper.writeStringToFile("folder_1/file1", "first")
      hdfsHelper.writeStringToFile("folder_1/file2", "second")
      hdfsHelper.writeStringToFile("folder_1/file3", "third")
      hdfsHelper.rmFile("folder_1/file2")
      hdfsHelper.rmDir("folder_1")

    } catch {
      case ex: Exception => {
        println("Exception " + ex)
      }
    }
  }
}
