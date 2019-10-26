import static java.util.UUID.randomUUID 
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import java.security.SignatureException

metadata {
	definition (name: "Wifi LED Devices (MagicHome, Flux, Hera, Novaldo, Luxor, Spectrum, LEDeNet)", namespace: "smartthings", author: "SmartThings") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "setColor"
        command "setAdjustedColor"
        command "setUFOWWLevel"
	}
    
    preferences {       
       input(name:"ip", type:"string", title: "IP Address:",
       		  description: "The IP address of this Bulb or Strip", defaultValue: "${ip}",
              required: true, displayDuringSetup: true)
       
       input(name:"deviceType", type:"string", title: "Bulb, or UFO?", 
       			description: "Type 'bulb' or 'ufo'", defaultValue: "${deviceType}", 
                required: true, displayDuringSetup: true)
	}
    
    tiles(scale: 2) {
    	multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.illuminance.illuminance.bright", backgroundColor:"#ffbf28", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.illuminance.illuminance.dark", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', icon:"st.illuminance.illuminance.bright", backgroundColor:"#ffe5a9"
				attributeState "turningOff", label:'${name}', icon:"st.illuminance.illuminance.dark", backgroundColor:"#ffe5a9"
            }
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
		}
        //Comment this section out if using a bulb
        valueTile("UFOWWSliderValue", "device.UFOWWLevel", height: 2, width: 2) {
    		state "UFOWWLevel", label:"Warm White:", defaultState: true
		}
        controlTile("UFOWWSliderControl", "device.UFOWWLevel", "slider", height: 2,
             width: 4, inactiveLabel: false, range:"(0..99)") {
    		state "device.aUFOWWLevel", action:"setUFOWWLevel"
		}
        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
    }
   
    
	main(["switch"])
	details(["switch", 
    		"rgbSelector", 
            "levelSliderControl", 
            "UFOWWSliderValue", 
            "UFOWWSliderControl",
            "refresh"])
}

def poll() {
	parent.poll(this)
}

// parse events into attributes
def parse(resp) {	    
	parseResponse(resp)    
}

private parseResponse(resp){
resp.headers.each {
    	log.info "-----------------${it.name} >> ${it.value}--------------"
        
         if(it.name.equalsIgnoreCase("powerState") && it.value != null){
         	if(it.value.toString() != device.currentValue("power")){
            	log.info "Changing power state from ${device.currentValue("power")} to ${it.value}"
    			sendEvent(name: "power", value: it.value.toString())
            }
            else{
           	 	log.info "Power State Sustained"
            }
         }
         if(it.name.equalsIgnoreCase("level") && it.value != null) {
         	if(Math.ceil(it.value.toFloat()).toInteger() != device.currentValue("level").toInteger()){
            	log.info "Changing level from ${device.currentValue("level").toInteger()} to ${Math.ceil(it.value.toFloat()).toInteger()}"
             	if(Math.ceil(it.value.toFloat()).toInteger() == 100.0){
                	sendEvent(name: "level", value: 99) //100% brightness breaks the level bar (and I'm not sure why)
                }
                else{
                	sendEvent(name: "level", value: Math.ceil(it.value.toFloat()).toInteger())
                }
                
            }
            else{
            	log.info "Level sustained"
            }
         }
         if(it.name.equalsIgnoreCase("hex") && it.value != null){
         	if(device.currentValue("color") != it.value){
         		log.info "Changing color from ${device.currentValue("color")} to ${it.value}"
        		sendEvent(name: "color", value: it.value)
                }
                else{
               	 log.info "Color Sustained"
                }
        }
        if(it.name.equalsIgnoreCase("UFOWWLevel") && it.value != null){
        	if(Math.ceil(it.value.toFloat()).toInteger() != device.currentValue("UFOWWLevel").toInteger()){
            	log.info "UFOWWLevel changing from ${device.currentValue("UFOWWLevel").toInteger()} to ${Math.ceil(it.value.toFloat()).toInteger()}"
        		sendEvent(name: "UFOWWLevel", value: "${Math.ceil(it.value.toFloat()).toInteger()}")
    		}
            else{
            	log.info "WW Level Sustained"
            }
        }
    }
}



// Handle commands
// Turn the bulb on
def on() {
	sendEvent(name: "switch", value: "on")
    sendUpdateCommand([status: "on"])
}

// Turn the bulb off
def off() {
	sendEvent(name: "switch", value: "off")
    sendUpdateCommand([status: "off"])
}


def setSaturation(saturation) {
     // Update the device saturation
	sendEvent(name: "saturation", value: saturation)

    // Send info to the device log
	log.info "setSaturation(Saturation: " + device.currentValue("saturation") + ")"
    
    // Send the command to the device
	sendUpdateCommand([saturation: saturation])
}

def setHue(hue) {
     // Update the device hue
	sendEvent(name: "hue", value: hue)

    // Send info to the device log
	log.info "setHue(Hue: " + device.currentValue("hue") + ")"
    
    // Send the command to the device
	sendUpdateCommand([hue: hue])
}

def setLevel(level) {
    // Update the device level
	sendEvent(name: "level", value: level)

    // Send info to the device log
	log.info "setLevel(Level: " + device.currentValue("level") + ")"
    
    // Send the command to the device
	sendUpdateCommand([level: level])
}

def setColor(value) {
    
    // Send HSL
    sendEvent(name: "hue", value: value.hue)
	sendEvent(name: "saturation", value: value.saturation)
    
    // If no level is assigned yet, set it to 100%
	if(device.currentValue("level") == null){
    	sendEvent(name: "level", value: 100)
    }
    //Change the assigned color if we have one	
    if (value.hex) {
		sendEvent(name: "color", value: value.hex)
	}
    // Get the device's current level to send
	value.level = device.currentValue("level")
	
    // Send info to the device log 
    log.info "setColor (Hue: ${value.hue}, Saturation: ${value.saturation}, Level: ${value.level})"
	
    //Send the command to the device
    sendUpdateCommand(value)
    
}

def setAdjustedColor(value){
    // Pass this through to the color since we adjust it on the server
	setColor(value)
}

def setUFOWWLevel(UFOWWLevel){
	 // Update the device level
	sendEvent(name: "UFOWWLevel", value: UFOWWLevel)

    // Send info to the device log
	log.info "setUFOWWLevel(UFOWWLevel: " + device.currentValue("UFOWWLevel") + ")"
    
    // Send the command to the device
	sendUpdateCommand([UFOWWLevel: UFOWWLevel])
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

	// Add the device IP to the parameters
	params += [ip: settings.ip]
    // Add the device type to the parameters (changes the way we adjust colors)
    params += [deviceType: settings.deviceType]
    //params += [refresh: 'false']
    
    // We need to transmit a UUID, Date, and Time for the REST server to accept our commands
	final def payload = randomUUID() as String
    long time = new Date().getTime() 
    time /= 1000L
     
    log.debug "Sending... " + params
    // Put your generated hash in the 3rd parameter slot 
    final String signature = hmac(payload + time, 'INSERT-TOKEN-HERE')

    httpPost(
    	[
        	// Put your public address here (including your port number and "/leds" at the end of the address
        	uri: ('http://24.211.254.22:5223/leds'),
            body:  params,
            headers: [
            	'X-Signature-Timestamp': time,
                'X-Signature-Payload': payload,
                'X-Signature': signature
            ]
        ]
    ) {resp ->
            // Do nothing. Only parse a response if we're refreshing the device
        }
}

def refresh(params) {
	if(params == null){
    	params = [ip: settings.ip]
    }
    else{
    	params += [ip: settings.ip]
    }
    params += [deviceType: settings.deviceType]
    //params += [refresh: 'false']
    
    // We need to transmit a UUID, Date, and Time for the REST server to accept our commands
	final def payload = randomUUID() as String
    long time = new Date().getTime() 
    time /= 1000L
     
    log.debug "Sending... " + params
    // Put your generated hash in the 3rd parameter slot 
    final String signature = hmac(payload + time, 'INSERT-TOKEN-HERE')


    try {
        httpPost(
        [
        	// Put your public address here (including your port number and "/leds" at the end of the address
        	uri: ('http://24.211.254.22:5223/leds'),
            body:  params,
            headers: [
            	'X-Signature-Timestamp': time,
                'X-Signature-Payload': payload,
                'X-Signature': signature
            ]
         ]
        ) {resp ->
            parse(resp)
        }
    } catch (e) {
        log.error "error in response: $e"
    }
}