import groovy.xml.XmlSlurper

def moduleXmlFile = new File(basedir, 'target/extra-resources/module.xml')
assert moduleXmlFile.exists() : "The module.xml descriptor was not created"
def doc = new XmlSlurper().parse(moduleXmlFile)
assert doc.version == 11 : "The module.xml version was not filtered"

// verify commons-math3 is under <components> -> <web-app> -> <web-resources> and has the correct version

def resource = doc.components.'web-app'.'web-resources'.childNodes().find { it.attributes().name = "org.apache.commons:commons-math3" }
assert resource != null : "commons-math3 resource must exist in module.xml"
def resourceVersion = resource.attributes().version
assert resourceVersion == "3.6.1" : "commons-math3 version must be 3.6.1"
