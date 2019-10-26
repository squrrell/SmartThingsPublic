/**
 *  Pushbutton Virtual Dimmer
 *
 *  Copyright 2017 James Bishop
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Pushbutton Virtual Dimmer", namespace: "squrrell", author: "James Bishop", oauth: true) {
		capability "Button"
		capability "Light"
		capability "Switch"
        capability "Switch Level"
	//	capability "Polling"
	//	capability "Refresh"
        
		command "low"
		command "med"
		command "high"

		attribute "currentState", "string"
	}

	tiles (scale:2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.currentState", key: "PRIMARY_CONTROL") {
				attributeState "default", label:'unknown state', action:"refresh.refresh", icon:"st.Lighting.light24", backgroundColor:"#b9bcbf", nextState: "turningOff"
				attributeState "HIGH", label:'HIGH', action:"switch.off", icon:"st.Lighting.light24", backgroundColor:"#486e13", nextState: "turningOff"
				attributeState "MED", label:'MED', action:"switch.off", icon:"st.Lighting.light24", backgroundColor:"#60931a", nextState: "turningOff"
				attributeState "LOW", label:'LOW', action:"switch.off", icon:"st.Lighting.light24", backgroundColor:"#79b821", nextState: "turningOff"
				attributeState "OFF", label:'OFF', action:"switch.on", icon:"st.Lighting.light24", backgroundColor:"#ffffff", nextState: "turningOn"
                attributeState "ON", label:'ON', action:"switch.off", icon:"st.Lighting.light24", backgroundColor:"#ffffff", nextState: "turningOff"
				attributeState "turningOn", action:"switch.on", label:'TURNINGON', icon:"st.Lighting.light24", backgroundColor:"#2179b8", nextState: "turningOn"
				attributeState "turningOff", action:"switch.off", label:'TURNINGOFF', icon:"st.Lighting.light24", backgroundColor:"#2179b8", nextState: "turningOff"
			}
			tileAttribute ("device.level", key: "SECONDARY_CONTROL") {
				attributeState "level", label:'${currentValue}%'
			}
		}
		standardTile("low", "device.currentState", inactiveLabel: false, width: 2, height: 2) {
        		state "default", label: 'LOW', action: "low", icon:"st.Home.home30", backgroundColor: "#ffffff"
			state "LOW", label:'LOW', action: "lowSpeed", icon:"st.Home.home30", backgroundColor: "#79b821"
			state "ADJUSTING.LOW", label:'LOW', action: "low", icon:"st.Home.home30", backgroundColor: "#2179b8"
  		}
		standardTile("med", "device.currentState", inactiveLabel: false, width: 2, height: 2) {
			state "default", label: 'MED', action: "med", icon:"st.Home.home30", backgroundColor: "#ffffff"
			state "MED", label: 'MED', action: "med", icon:"st.Home.home30", backgroundColor: "#79b821"
            		state "ADJUSTING.MED", label:'MED', action: "med", icon:"st.Home.home30", backgroundColor: "#2179b8"
		}
		standardTile("high", "device.currentState", inactiveLabel: false, width: 2, height: 2) {
			state "default", label: 'HIGH', action: "high", icon:"st.Home.home30", backgroundColor: "#ffffff"
			state "HIGH", label: 'HIGH', action: "high", icon:"st.Home.home30", backgroundColor: "#79b821"
            		state "ADJUSTING.HIGH", label:'HIGH', action: "high", icon:"st.Home.home30", backgroundColor: "#2179b8"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main(["switch"])
		details(["switch", "low", "med", "high", "refresh"])
	}
	preferences {
		section("Light Thresholds") {
			input "lowThreshold", "number", title: "Low Threshold", range: "1..99"
			input "medThreshold", "number", title: "Medium Threshold", range: "1..99"
			input "highThreshold", "number", title: "High Threshold", range: "1..99"
		}
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}

// handle commands
def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}
