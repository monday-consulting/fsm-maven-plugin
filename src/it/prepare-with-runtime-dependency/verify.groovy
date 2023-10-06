import groovy.xml.XmlSlurper

def fsmDir = new File(basedir, 'target/fsm-root')
assert fsmDir.exists() : "The FSM root directory was not created"

def moduleXmlFile = new File(fsmDir, 'META-INF/module.xml')
assert moduleXmlFile.exists() : "The module.xml descriptor was not created"
def moduleIsolatedXmlFile = new File(fsmDir, 'META-INF/module-isolated.xml')
assert moduleIsolatedXmlFile.exists() : "The module-isolated.xml descriptor was not created"

def doc = new XmlSlurper().parse(moduleXmlFile)
assert doc.version == 11 : "The module.xml version was not filtered"

// verify lib is under <components> -> <web-app> -> <web-resources>
def resource = doc.components.'web-app'.'web-resources'.childNodes().find { it.attributes().name = "com.fasterxml.jackson.core:jackson-databind" }
assert resource != null : "com.fasterxml.jackson.core:jackson-databind resource must exist in module.xml"

// verify dependency has been copied to <fsm-root>/lib/
def libDir = new File(fsmDir, 'lib');
assert libDir.exists() : "The lib directory was not created"
assert (new File(libDir, 'jackson-databind-2.15.2.jar').exists()) : "The resource lib 'jackson-databind-2.15.2.jar' was not copied to the lib/ directory"
