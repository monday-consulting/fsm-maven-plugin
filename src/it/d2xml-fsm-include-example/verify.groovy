import groovy.xml.XmlSlurper

def moduleXmlFile = new File(basedir, 'target/extra-resources/module.xml')
assert moduleXmlFile.exists() : "The module.xml descriptor was not created"
def doc = new XmlSlurper().parse(moduleXmlFile)
assert doc.version == 11 : "The module.xml version was not filtered"

def components = doc.components
assert components.size() == 1 : "There must be only one <components> element"

// Validate structure under <components>
def webApp = components.'web-app'
assert webApp.size() == 1 : "There must be exactly one <web-app> element"
assert webApp.name.text() == 'My Example WebApp' : "Unexpected <name> in <web-app>"
assert webApp.version.text() == '11' : "Unexpected <version> in <web-app>"

def webResources = webApp.'web-resources'
assert webResources.size() == 1 : "There must be exactly one <web-resources> element"

def resource = webResources.resource
assert resource.size() == 1 : "There must be exactly one <resource> element"
assert resource.@name.text() == 'org.apache.commons:commons-math3' : "Unexpected resource @name"
assert resource.@version.text() == '3.6.1' : "Unexpected resource @version"
assert resource.text().trim() == 'lib/commons-math3-3.6.1.jar' : "Unexpected resource text content"
