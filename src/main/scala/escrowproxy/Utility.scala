package escrowproxy

import java.util.regex.{Matcher, Pattern}


class Utility extends Serializable {

  def parse(line: String): Entity = {

    var prs_id: String = null
    var label: String = null
    var event_type: String = null
    var command: String = null
    var user_agent: String = null
    var response: String = null
    var platform: String = null
    var os_value: String = null
    var errorCd: String = null
    var aggrCt: String = "0"
    var os_name: String = null
    var os_ver: String = null
    var os_major_version: String = null
    var os_minor_version: String = null
    var os_type: String = null
    var platform_name: String = null
    var platform_version: String = null
    var platform_type: String = null
    var isValid = false
    var device_valid = false
    var os_valid = false
    var DF_NAME_SUFFIX = "INVALID."

    val tokens = line.split("\\t")

    if (tokens.size < 10) {
      isValid = false
    } else {
      prs_id = tokens(0)
      label = tokens(1)
      event_type = tokens(3)
      command = tokens(5)
      user_agent = tokens(6)
      response = tokens(4)
      val parsedUserAgent = parseClientInfo(user_agent)
      platform = parsedUserAgent._1
      os_value = parsedUserAgent._2
      errorCd = tokens(8)

      if (tokens.length > 11 && tokens(11).matches("[0-9]+")) {
        aggrCt = tokens(11)
      }

      if (label != null && label.contains("com.apple.icdp.record") && response != null
        && !(response == "") && (command != null && command.contains("ENROLL")
        || command != null && command.contains("RECOVER"))) {

        isValid = true
      }

      if (label != null && label.contains("com.apple.icdp.record")
        && (command != null && command.contains("ENROLL")
        && response != null && !(response == "")
        && response.contains("200") || command != null
        && command.contains("RECOVER") && response != null
        && !(response == "") && response.contains("200"))) {

        os_valid = true
        device_valid = true
        DF_NAME_SUFFIX = if (command.equalsIgnoreCase("ENROLL")) "ENROLL." else "RECOVER."

        if (os_value != null && (os_value.contains(".") || os_value.contains(" "))) {
          os_value = os_value.replace(".", "_")
          os_value = os_value.replaceAll(" ", "_")
        }

        if (label != null && label.contains("com.apple.icdp.record")
          && (command != null && command.contains("ENROLL")
          && response != null && !(response == "")
          && response.contains("200") || command != null
          && command.contains("RECOVER") && response != null
          && !(response == "") && response.contains("200")) && !os_value.equalsIgnoreCase("UNKNOWN")) {

          os_valid = true

          val osSplits: Array[String] = os_value.split(";")
          os_name = osSplits(0)
          os_ver = osSplits(1)
          os_major_version = os_ver.substring(0, os_ver.indexOf("_"))
          os_minor_version = os_ver.substring(os_ver.indexOf("_"))
          os_type = if (os_name.contains("Mac")) "OSX"

          else if (os_name.contains("iPhone")) "IOS"
          else if (os_name.contains("Apple")) "TVOS" else (if (os_name.contains("Watch")) "WATCHOS" else "UNKONWN")
          if (os_value.contains("Mac")) {
            if (os_ver.split("_").length == 2) {
              os_ver = os_ver + "_0"
            }
            os_major_version = os_ver.substring(0, os_ver.indexOf("_", 3))
            os_minor_version = os_ver.substring(os_ver.indexOf("_", 3))
          }
          else if (os_ver.split("_").length == 1) {
            os_value = os_value + "_0"
          }
        }
        platform = platform.replaceAll(",", "_")
        if (platform != null && !platform.equalsIgnoreCase("UNKNOWN") && !platform.equalsIgnoreCase("UNKNOWNUA")) {
          platform = platform.toUpperCase
          val matcher: Matcher = Pattern.compile("\\d+").matcher(platform)
          if (matcher.find) {
            val i: Int = Integer.valueOf(matcher.group).intValue
            platform_name = platform.substring(0, platform.indexOf(i + ""))
            platform_version = platform.substring(platform.indexOf(i + ""))
          }
          else {
            platform_name = platform
          }
          device_valid = true
          if (platform.startsWith("IPAD")) {
            platform_type = "IPAD"
          }
          else if (platform.startsWith("IPOD")) {
            platform_type = "IPOD"
          }
          else if (platform.startsWith("IPHONE")) {
            platform_type = "IPHONE"
          }
          else if (!platform.startsWith("MAC") && !platform.startsWith("IMAC")) {
            platform_type = "UNKNOWN"
          }
          else {
            platform_type = "MAC"
          }
        }
      }
    }

    Entity(prs_id, label, event_type, command, response, platform, platform_type, platform_version,
      platform_name, device_valid, os_value, os_name, os_type, os_ver,
      os_major_version, os_minor_version, aggrCt, errorCd,
      os_valid, DF_NAME_SUFFIX, isValid)
  }

  def parseClientInfo(field: String) = {

    var platform = "UNKNOWNUA"
    var os_value = "UNKNOWNUA"

    val fields = field.split(">")

    if (fields.size > 1) {
      platform = fields(0).substring(1).trim
      os_value = fields(1).substring(2, fields(1).length).trim
    }
    (platform, os_value)
  }
}
