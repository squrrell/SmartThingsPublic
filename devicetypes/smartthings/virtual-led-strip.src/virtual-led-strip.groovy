import static java.util.UUID.randomUUID 
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import java.security.SignatureException

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Virtual LED Strip", namespace: "smartthings", author: "SmartThings") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "setAdjustedColor"
	}
    
	standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
		state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
		state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
		state "turningOn", label:'${name}', icon:"st.switches.switch.on", backgroundColor:"#79b821"
		state "turningOff", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ffffff"
	}
	standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
	}
	controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
		state "color", action:"setAdjustedColor"
	}
	controlTile("saturationSliderControl", "device.saturation", "slider", height: 1, width: 2, inactiveLabel: false) {
		state "saturation", action:"color control.setSaturation"
	}
	valueTile("saturation", "device.saturation", inactiveLabel: false, decoration: "flat") {
		state "saturation", label: 'Sat ${currentValue}    '
	}
	controlTile("hueSliderControl", "device.hue", "slider", height: 1, width: 2, inactiveLabel: false) {
		state "hue", action:"color control.setHue"
	}
	valueTile("hue", "device.hue", inactiveLabel: false, decoration: "flat") {
		state "hue", label: 'Hue ${currentValue}   '
	}
    controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
		state "level", action:"switch level.setLevel"
	}
	valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
		state "level", label: 'Level ${currentValue}%'
	}

	main(["switch"])
	details(["switch", "rgbSelector", "levelSliderControl"])

}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Bulb stringToMap - ${map}"
		map = stringToMap(description)
	}

	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}

	results

}

// handle commands
def on() {
	sendEvent(name: "switch", value: "on")
    sendUpdateCommand([status: "on"])
}

def off() {
	sendEvent(name: "switch", value: "off")
    sendUpdateCommand([status: "off"])
}

def poll() {
	parent.poll(this)
}

def setSaturation(percent) {
	sendEvent(name: "saturation", value: percent)
}

def setHue(percent) {
	sendEvent(name: "hue", value: percent)
}

def setLevel(percent) {
	sendUpdateCommand([level: percent])
	sendEvent(name: "level", value: percent)
}

def setColor(value) {
	log.debug "setColor: ${value}"
	sendEvent(name: "hue", value: value.hue)
	sendEvent(name: "saturation", value: value.saturation)
}

def setAdjustedColor(value) {
	log.debug "setAdjustedColor: ${value}"
    sendUpdateCommand([r: value.red, g: value.green, b: value.blue])
	def adjusted = value + [:]
	adjusted.hue = adjustOutgoingHue(value.hue)
	adjusted.level = null // needed because color picker always sends 100
	setColor(adjusted)
}

def save() {
	log.debug "Executing 'save'"
}

def refresh() {
	log.debug "Executing 'refresh'"
}

def adjustOutgoingHue(percent) {
	def adjusted = percent
	if (percent > 31) {
		if (percent < 63.0) {
			adjusted = percent + (7 * (percent -30 ) / 32)
		}
		else if (percent < 73.0) {
			adjusted = 69 + (5 * (percent - 62) / 10)
		}
		else {
			adjusted = percent + (2 * (100 - percent) / 28)
		}
	}
	log.info "percent: $percent, adjusted: $adjusted"
	adjusted
}

def hmac(String data, String key) throws SignatureException {
  final Mac hmacSha1;
  try {
     hmacSha1 = Mac.getInstance("HmacSHA1");
  } catch (Exception nsae) {
      hmacSha1 = Mac.getInstance("HMAC-SHA-1");         
  }
  
  final SecretKeySpec macKey = new SecretKeySpec(key.getBytes(), "RAW");
  hmacSha1.init(macKey);
  
  final byte[] signature =  hmacSha1.doFinal(data.getBytes());
  
  return signature.encodeHex()
}

def sendUpdateCommand(params) {
	log.info "sending update command. params: $params"
    
	final def payload = randomUUID() as String
    long time = new Date().getTime() 
    time /= 1000L
    
    final String signature = hmac(payload + time, 'e143ade4-40b6-4f0c-9c7c-bbd3158e0a5e')

    httpPost(
    	[
        	uri: ('192.168.1.28:8000'),
			body: params,
            headers: [
            	'X-Signature-Timestamp': time,
                'X-Signature-Payload': payload,
                'X-Signature': signature
            ]
        ]
    )
}