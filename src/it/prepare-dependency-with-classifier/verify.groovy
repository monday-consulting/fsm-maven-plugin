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
def resource = doc.components.'web-app'.'web-resources'.childNodes().find { it.attributes().name = "org.xmlresolver:xmlresolver" }
assert resource != null : "org.xmlresolver:xmlresolver (no classifier) resource must exist in module.xml"
def resourceWithClassifier = doc.components.'web-app'.'web-resources'.childNodes().find { it.attributes().name = "org.xmlresolver:xmlresolver:data" }
assert resourceWithClassifier != null : "org.xmlresolver:xmlresolver:data (with classifier) resource must exist in module.xml"

// verify dependency has been copied to <fsm-root>/lib/
def libDir = new File(fsmDir, 'lib');
assert libDir.exists() : "The lib directory was not created"
assert (new File(libDir, 'xmlresolver-5.2.0.jar').exists()) : "The resource lib 'xmlresolver-5.2.0.jar' was not copied to the lib/ directory"
assert (new File(libDir, 'xmlresolver-5.2.0-data.jar').exists()) : "The resource lib 'xmlresolver-5.2.0.jar-data' was not copied to the lib/ directory"
