def moduleXmlFile = new File(basedir, 'target/extra-resources/module.xml')
assert moduleXmlFile.exists() : "The module.xml descriptor was not created.";
def doc = new XmlSlurper().parse(moduleXmlFile)
assert doc.version == "15.5.0" : "The module.xml version was not filtered."

// Verify resolved versions for guava

static def getResource(doc, mode, scope, name) {
    return doc.resources.childNodes().findAll {
        def attr = it.attributes()
        attr.mode == mode && attr.scope == scope && attr.name == name
    }
}

static def verifyResource(doc, mode, scope, version) {
    def guava = "com.google.guava:guava"

    def resource = getResource(doc, mode, scope, guava)
    def resourceSize = resource.size()
    assert resourceSize == 1
    def resourceVersion = resource.get(0).attributes().version
    assert resourceVersion == version : "Guava version for " + mode + "-" + scope + " must be " + version
}

verifyResource(doc, "isolated", "module", "27.1-jre")
verifyResource(doc, "isolated", "server", "19.0.20150826")
verifyResource(doc, "legacy", "server", "31.1-jre")
