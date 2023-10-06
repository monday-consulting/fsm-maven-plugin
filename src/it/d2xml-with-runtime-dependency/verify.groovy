import groovy.xml.XmlSlurper

def moduleXmlFile = new File(basedir, 'target/extra-resources/module.xml')
assert moduleXmlFile.exists() : "The module.xml descriptor was not created"

def doc = new XmlSlurper().parse(moduleXmlFile)
assert doc.version == 11 : "The module.xml version was not filtered"

// verify lib is under <components> -> <web-app> -> <web-resources>
def resource = doc.components.'web-app'.'web-resources'.childNodes().find { it.attributes().name = "com.fasterxml.jackson.core:jackson-databind" }
assert resource != null : "com.fasterxml.jackson.core:jackson-databind resource must exist in module.xml"


def moduleXmlFileNoRuntime = new File(basedir, 'target/extra-resources/module-no-runtime.xml')
assert moduleXmlFileNoRuntime.exists() : "The module-no-runtime.xml descriptor was not created"

def docNoRuntime = new XmlSlurper().parse(moduleXmlFileNoRuntime)
def resourcesNoRuntime = docNoRuntime.components.'web-app'.'web-resources'.childNodes()
assert resourcesNoRuntime.size() == 0 : "module-no-runtime's resources must be empty"
