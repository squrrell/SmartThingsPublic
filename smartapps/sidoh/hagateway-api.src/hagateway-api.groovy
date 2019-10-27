/**
 *  Control a Switch with an API call
 *
 *  Copyright 2015 SmartThings
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
definition(
    name: "HaGateway API",
    namespace: "sidoh",
    author: "sidoh",
    description: "Expose devices to HaGateway",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)

preferences {
        section("Select Devices to Authorize") {
            input "theSwitches", "capability.switch", title: "Switches:", required: false, multiple: true
        }
}

mappings {
  path("/switches") {
    action: [
      GET: "getSwitches",
      PUT: "updateSwitches"
    ]
  }
  
  path("/switches/:id") {
    action: [
        GET: "getSwitch",
        PUT: "updateSwitch"
    ]
  }
    
  path("/routines/:name") {
  	action: [
    	GET: "runRoutine"
    ]
  }
  
  path("/routines") {
  	action: [
    	GET: "getRoutines"
    ]
  }
}

def runRoutine() {
    def allRoutines = location.helloHome?.getPhrases()*.label
    def routine = allRoutines.find {
    	it.toLowerCase().replaceAll(" ", "_").replaceAll(/[^a-zA-Z0-9_]/, "") == params.name
    }
    
    log.trace "Running: ${routine}"
    
	location.helloHome?.execute(routine)
    
    return true
}

def getRoutines() {
	location.helloHome?.getPhrases()*.label
}

def getSwitches() {
    def status = [:]
    theSwitches.each {theSwitch ->
        log.trace "will populate status map"
        log.trace "theSwitch id: ${theSwitch.id}"
        status.put(theSwitch.id, [name:theSwitch.displayName,status:theSwitch.currentSwitch])
    }
    
    log.debug "listSwitches returning: $status"
    return status
}

def getSwitch() {
    def theSwitch = theSwitches.find{it.id == params.id}
    [theSwitch.displayName, theSwitch.currentSwitch]
}

def updateSwitches() {
    theSwitches.each {
        doCommand(it, params.command)
    }
}

def updateSwitch() {
    def theSwitch = theSwitches.find{it.id == params.id}
    doCommand(theSwitch, params.command)
}

def doCommand(theSwitch, command) {
    if (command == "toggle") {
        if (theSwitch.currentSwitch == "on") {
            log.debug "will try and turn switch ${theSwitch.displayName} on"
            theSwitch.off()
        } else {
            log.debug "will try and turn switch ${theSwitch.displayName} off"
            theSwitch.on()
        }
    } else if (command == "on" || command == "off") {
        theSwitch."$command"()
    } else {
        httpError(400, "Unsupported command - only 'toggle', 'off', and 'on' supported")
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
}