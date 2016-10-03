package escrowproxy

case class Entity(prs_id: String, label: String, event_type: String,
                  command: String, response: String, platform: String,
                  platform_type: String, platform_version: String, platform_name: String,
                  device_valid: Boolean, os_value: String, os_name: String,
                  os_type: String, os_ver: String, os_major_version: String,
                  os_minor_version: String, aggrCt: String, errorCd: String,
                  os_valid: Boolean, DF_NAME_SUFFIX: String, isValid: Boolean)
