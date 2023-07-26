import groovy.xml.XmlSlurper

def fsmDir = new File(basedir, 'target/fsm-root')
assert fsmDir.exists() : "The FSM root directory was not created"

def moduleXmlFile = new File(fsmDir, 'META-INF/module.xml')
assert moduleXmlFile.exists() : "The module.xml descriptor was not created"
def moduleIsolatedXmlFile = new File(fsmDir, 'META-INF/module-isolated.xml')
assert moduleIsolatedXmlFile.exists() : "The module-isolated.xml descriptor was not created"

def doc = new XmlSlurper().parse(moduleXmlFile)
assert doc.version == 11 : "The module.xml version was not filtered"

// verify commons-math3 is under <components> -> <web-app> -> <web-resources> and has the correct version
def resource = doc.components.'web-app'.'web-resources'.childNodes().find { it.attributes().name = "org.apache.commons:commons-math3" }
assert resource != null : "commons-math3 resource must exist in module.xml"
def resourceVersion = resource.attributes().version
assert resourceVersion == "3.6.1" : "commons-math3 version must be 3.6.1"

// verify dependency has been copied to <fsm-root>/lib/
def libDir = new File(fsmDir, 'lib');
assert libDir.exists() : "The lib directory was not created"
def resourceLib = new File(libDir, 'commons-math3-3.6.1.jar')
assert resourceLib.exists() : "The resource lib 'commons-math3-3.6.1.jar' was not copied to the lib/ directory"
