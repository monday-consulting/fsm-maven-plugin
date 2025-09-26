import groovy.xml.XmlSlurper

def fsmDir = new File(basedir, 'target/fsm-root')
assert fsmDir.exists() : "The FSM root directory was not created"

def moduleXmlFile = new File(fsmDir, 'META-INF/module.xml')
assert moduleXmlFile.exists() : "The module.xml descriptor was not created"
def moduleIsolatedXmlFile = new File(fsmDir, 'META-INF/module-isolated.xml')
assert moduleIsolatedXmlFile.exists() : "The module-isolated.xml descriptor was not created"

def doc = new XmlSlurper().parse(moduleXmlFile)
assert doc.version == 1 : "The module.xml version was not filtered"
