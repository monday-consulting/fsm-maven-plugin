# Monday FirstSpirit Module Maven Plugin

You can use this Maven plugin to generate XML descriptors for FirstSpirit modules.

Make sure you use maven 3.1.x or newer when integrating this plugin into your workspace.

# Getting started

## Maven Dependency [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.monday-consulting.maven.plugins/fsm-maven-plugin/badge.svg?style=flat)](http://mvnrepository.com/artifact/com.monday-consulting.maven.plugins/fsm-maven-plugin)

We published our plugin to the maven central repository.
You could include it into your workspace with the following maven dependency:

```
    <dependency>
      <groupId>com.monday-consulting.maven.plugins</groupId>
      <artifactId>fsm-maven-plugin</artifactId>
      <version>1.8.2</version>
    </dependency>
```

## Plugin Configuration

* You have to configure the plugin with an prototype.xml, the config.xml and a target.
* Your plugin configuration should look like this:

```
    <plugin>
        <groupId>com.monday-consulting.maven.plugins</groupId>
        <artifactId>fsm-maven-plugin</artifactId>
        <configuration>
            <prototypeXml>${basedir}/target/extra-resources/prototype.module.xml</prototypeXml>
            <configXml>${basedir}/target/extra-resources/fsm-plugin.xml</configXml>
            <targetXml>${basedir}/target/extra-resources/module.xml</targetXml>
        </configuration>
        <executions>
            <execution>
                <id>dependencyToXML</id>
                <phase>package</phase>
                <goals>
                    <goal>dependencyToXML</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
```

## Prototype XML

* This is your prototype FirstSpirit module descriptor which will be consumed by this plugin to generate your working module.xml
* In this file you have to configure your FirstSpirit module components (i.e. project and web components).
* Your prototype XML could look like this:

```
    <module>
        <name>Monday Webforms</name>
        <version>${project.version}</version>
        <description>Monday Webforms Form Editor</description>
        <vendor>Monday Consulting GmbH</vendor>

        <components>
            <public>
                <name>MONDAY_FORMEDITOR</name>
                <description>Monday Webforms editor component</description>
                <class>de.espirit.firstspirit.module.GadgetSpecification</class>
                <configuration>
                    <gom>com.monday.webforms.editor.gom.GomFormEditor</gom>
                    <factory>com.monday.webforms.editor.gadgets.FormEditorSwingGadgetFactory</factory>
                    <value>com.monday.webforms.editor.gadgets.FormValueEngineerFactory</value>
                    <scope data="yes" content="yes" link="yes"/>
                </configuration>
                <resources>
                    <resource>files/</resource>
                    <dependencies>webforms-editor-resources</dependencies>
                </resources>
            </public>

            <project-app>
                <name>Monday Webforms Resources</name>
                <description>Monday Webforms project resources</description>
                <class>com.monday.webforms.admin.project.WebformsProjectApp</class>
                <resources>
                    <resource>files/</resource>
                    <dependencies>webforms-admin-resources</dependencies>
                </resources>
            </project-app>

            <web-app scopes="global">
                <name>Monday Webforms WebApp</name>
                <description>Monday Webforms FIRSTspirit integration.</description>
                <configurable>com.monday.webforms.admin.webapp.WebAppConfiguration</configurable>
                <class>com.monday.webforms.admin.WebformsWebApp</class>
                <web-xml>web.xml</web-xml>
                <resources>
                    <dependencies>webforms-admin-resources</dependencies>
                </resources>
                <web-resources>
                    <dependencies>webforms-webapp-resources</dependencies>
                </web-resources>
            </web-app>

            <web-app scopes="global">
                <name>Monday Webforms Analytics Backend</name>
                <description>Monday Webforms Analytics Backend FIRSTspirit integration.</description>
                <configurable>com.monday.webforms.admin.analytics.backend.WebAppConfiguration</configurable>
                <class>com.monday.webforms.admin.WebformsWebApp</class>
                <web-xml>external/backend/WEB-INF/web.xml</web-xml>
                <resources>
                    <dependencies>webforms-admin-resources</dependencies>
                </resources>
                <web-resources>
                    <dependencies>webforms-backend-libs</dependencies>
                </web-resources>
            </web-app>
        </components>
    </module>
```

## Config XML

* In your config.xml you have to configure which folder or artifact should be included in your module descriptor.
* Your config.xml could look like this:

```
    <?xml version="1.0" encoding="UTF-8"?>
    <fsm-maven-plugin>
        <scopes>
            <scope>runtime</scope>
            <scope>compile</scope>
        </scopes>
        <modules>
            <module>
                <id>com.monday.webforms.firstspirit:webforms-webapp:war</id>
                <prefix>lib/</prefix>
                <dependencyTagValueInXml>webforms-webapp-resources</dependencyTagValueInXml>
                <firstSpiritScope>module</firstSpiritScope>
                <resource>
                    <web-xml>WEB-INF/web.xml</web-xml>
                    <includes>
                        <include>WEB-INF/**</include>
                    </includes>
                    <excludes>
                        <exclude>WEB-INF/classes/</exclude>
                        <exclude>WEB-INF/lib/</exclude>
                    </excludes>
                </resource>
            </module>
            <module>
                <id>com.monday.webforms.analytics:webforms-reporting-webapp:war:${monday.webforms.backend.version}</id>
                <prefix>external/reporting/WEB-INF/lib/</prefix>
                <dependencyTagValueInXml>webforms-reporting-libs</dependencyTagValueInXml>
                <firstSpiritScope>module</firstSpiritScope>
                <resource>
                    <prefix>external/reporting/</prefix>
                    <web-xml>WEB-INF/web.xml</web-xml>
                    <includes>
                        <include>**</include>
                    </includes>
                    <excludes>
                        <exclude>META-INF</exclude>
                        <exclude>WEB-INF/lib/</exclude>
                    </excludes>
                </resource>
            </module>
        </modules>
    </fsm-maven-plugin>
```
This example includes everything in the WEB-INF folder but excludes explicitly the folders WEB-INF/lib and WEB-INF/classes.

## Generated module XML

* A module.xml file generated by this plugin could look like this:

```
    <module>
      <name>Monday Webforms</name>
      <version>5.5.0-SNAPSHOT</version>
      <description>Monday Webforms Form Editor</description>
      <vendor>Monday Consulting GmbH</vendor>
      <components>
        <public>
          <name>MONDAY_FORMEDITOR</name>
          <description>Monday Webforms editor component</description>
          <class>de.espirit.firstspirit.module.GadgetSpecification</class>
          <configuration>
            <gom>com.monday.webforms.editor.gom.GomFormEditor</gom>
            <factory>com.monday.webforms.editor.gadgets.FormEditorSwingGadgetFactory</factory>
            <value>com.monday.webforms.editor.gadgets.FormValueEngineerFactory</value>
            <scope data="yes" link="yes" content="yes"/>
          </configuration>
          <resources>
            <resource>files/</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-common" version="5.5.0-SNAPSHOT">lib/webforms-common-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-shared" version="5.5.0-SNAPSHOT">lib/webforms-shared-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="commons-io:commons-io" version="1.3.2">lib/commons-io-1.3.2.jar</resource>
            <resource scope="module" name="org.mozilla:rhino" version="1.7.7.1">lib/rhino-1.7.7.1.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:jPod" version="5.5.1">lib/jPod-5.5.1.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:iscwt" version="5.5">lib/iscwt-5.5.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isfreetype" version="5.5">lib/isfreetype-5.5.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isnativec" version="5.5">lib/isnativec-5.5.jar</resource>
            <resource scope="module" name="javax.media:jai-core" version="1.1.3">lib/jai-core-1.1.3.jar</resource>
            <resource scope="module" name="com.sun.media:jai-codec" version="1.1.3">lib/jai-codec-1.1.3.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isrt" version="4.10">lib/isrt-4.10.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:jbig2" version="5.5.1">lib/jbig2-5.5.1.jar</resource>
            <resource scope="module" name="com.smardec:license4j" version="1.6">lib/license4j-1.6.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-gui" version="5.5.0-SNAPSHOT">lib/webforms-gui-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-admin" version="5.5.0-SNAPSHOT">lib/webforms-admin-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="net.java.dev.designgridlayout:designgridlayout" version="1.1">lib/designgridlayout-1.1.jar</resource>
            <resource scope="module" name="net.java.dev.swing-layout:swing-layout" version="1.0.2">lib/swing-layout-1.0.2.jar</resource>
            <resource scope="module" name="com.monday:monday-common" version="3.1.1">lib/monday-common-3.1.1.jar</resource>
            <resource scope="module" name="javax.mail:javax.mail-api" version="1.4.4">lib/javax.mail-api-1.4.4.jar</resource>
            <resource scope="module" name="javax.activation:activation" version="1.1.1">lib/activation-1.1.1.jar</resource>
            <resource scope="module" name="net.java.dev.jna:jna" version="4.1.0">lib/jna-4.1.0.jar</resource>
            <resource scope="module" name="de.espirit.firstspirit:fs-access" version="5.2.181206">lib/fs-access-5.2.181206.jar</resource>
            <resource scope="module" name="org.slf4j:slf4j-api" version="1.6.4">lib/slf4j-api-1.6.4.jar</resource>
            <resource scope="module" name="com.google.code.gson:gson" version="2.8.2">lib/gson-2.8.2.jar</resource>
            <resource scope="module" name="org.apache.commons:commons-lang3" version="3.7">lib/commons-lang3-3.7.jar</resource>
            <resource scope="module" name="com.fifesoft:rsyntaxtextarea" version="2.6.1">lib/rsyntaxtextarea-2.6.1.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-editor" version="5.5.0-SNAPSHOT">lib/webforms-editor-5.5.0-SNAPSHOT.jar</resource>
          </resources>
        </public>
        <web-app scopes="global">
          <name>Monday Webforms WebApp</name>
          <description>Monday Webforms FIRSTspirit integration.</description>
          <configurable>com.monday.webforms.admin.webapp.WebAppConfiguration</configurable>
          <class>com.monday.webforms.admin.WebformsWebApp</class>
          <web-xml>web.xml</web-xml>
          <resources>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-gui" version="5.5.0-SNAPSHOT">lib/webforms-gui-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="com.fifesoft:rsyntaxtextarea" version="2.6.1">lib/rsyntaxtextarea-2.6.1.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-common" version="5.5.0-SNAPSHOT">lib/webforms-common-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-shared" version="5.5.0-SNAPSHOT">lib/webforms-shared-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="commons-io:commons-io" version="1.3.2">lib/commons-io-1.3.2.jar</resource>
            <resource scope="module" name="org.slf4j:slf4j-api" version="1.6.4">lib/slf4j-api-1.6.4.jar</resource>
            <resource scope="module" name="org.mozilla:rhino" version="1.7.7.1">lib/rhino-1.7.7.1.jar</resource>
            <resource scope="module" name="com.google.code.gson:gson" version="2.8.2">lib/gson-2.8.2.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:jPod" version="5.5.1">lib/jPod-5.5.1.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:iscwt" version="5.5">lib/iscwt-5.5.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isfreetype" version="5.5">lib/isfreetype-5.5.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isnativec" version="5.5">lib/isnativec-5.5.jar</resource>
            <resource scope="module" name="net.java.dev.jna:jna" version="4.1.0">lib/jna-4.1.0.jar</resource>
            <resource scope="module" name="javax.media:jai-core" version="1.1.3">lib/jai-core-1.1.3.jar</resource>
            <resource scope="module" name="com.sun.media:jai-codec" version="1.1.3">lib/jai-codec-1.1.3.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isrt" version="4.10">lib/isrt-4.10.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:jbig2" version="5.5.1">lib/jbig2-5.5.1.jar</resource>
            <resource scope="module" name="com.monday:monday-common" version="3.1.1">lib/monday-common-3.1.1.jar</resource>
            <resource scope="module" name="javax.mail:javax.mail-api" version="1.4.4">lib/javax.mail-api-1.4.4.jar</resource>
            <resource scope="module" name="javax.activation:activation" version="1.1.1">lib/activation-1.1.1.jar</resource>
            <resource scope="module" name="de.espirit.firstspirit:fs-access" version="5.2.181206">lib/fs-access-5.2.181206.jar</resource>
            <resource scope="module" name="com.smardec:license4j" version="1.6">lib/license4j-1.6.jar</resource>
            <resource scope="module" name="net.java.dev.designgridlayout:designgridlayout" version="1.1">lib/designgridlayout-1.1.jar</resource>
            <resource scope="module" name="net.java.dev.swing-layout:swing-layout" version="1.0.2">lib/swing-layout-1.0.2.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-executables" version="5.5.0-SNAPSHOT">lib/webforms-executables-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-admin" version="5.5.0-SNAPSHOT">lib/webforms-admin-5.5.0-SNAPSHOT.jar</resource>
          </resources>
          <web-resources>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-webapp-lib" version="5.5.0-SNAPSHOT">lib/webforms-webapp-lib-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="de.espirit.firstspirit:fs-access" version="5.2.181206">lib/fs-access-5.2.181206.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-common" version="5.5.0-SNAPSHOT">lib/webforms-common-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:jPod" version="5.5.1">lib/jPod-5.5.1.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:iscwt" version="5.5">lib/iscwt-5.5.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isfreetype" version="5.5">lib/isfreetype-5.5.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isnativec" version="5.5">lib/isnativec-5.5.jar</resource>
            <resource scope="module" name="net.java.dev.jna:jna" version="4.1.0">lib/jna-4.1.0.jar</resource>
            <resource scope="module" name="javax.media:jai-core" version="1.1.3">lib/jai-core-1.1.3.jar</resource>
            <resource scope="module" name="com.sun.media:jai-codec" version="1.1.3">lib/jai-codec-1.1.3.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isrt" version="4.10">lib/isrt-4.10.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:jbig2" version="5.5.1">lib/jbig2-5.5.1.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-spring" version="5.5.0-SNAPSHOT">lib/webforms-spring-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="org.slf4j:jcl-over-slf4j" version="1.6.4">lib/jcl-over-slf4j-1.6.4.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-shared" version="5.5.0-SNAPSHOT">lib/webforms-shared-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-executables" version="5.5.0-SNAPSHOT">lib/webforms-executables-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="com.monday:monday-common" version="3.1.1">lib/monday-common-3.1.1.jar</resource>
            <resource scope="module" name="com.monday.webforms.analytics:webforms-backend-api-client" version="2.2.2">lib/webforms-backend-api-client-2.2.2.jar</resource>
            <resource scope="module" name="com.monday.webforms.analytics:webforms-backend-common" version="2.2.2">lib/webforms-backend-common-2.2.2.jar</resource>
            <resource scope="module" name="com.monday.webforms.analytics:webforms-backend-api-model" version="2.2.2">lib/webforms-backend-api-model-2.2.2.jar</resource>
            <resource scope="module" name="org.jetbrains:annotations" version="13.0">lib/annotations-13.0.jar</resource>
            <resource scope="module" name="com.fasterxml.jackson.core:jackson-core" version="2.8.11">lib/jackson-core-2.8.11.jar</resource>
            <resource scope="module" name="com.fasterxml.jackson.core:jackson-databind" version="2.8.11.2">lib/jackson-databind-2.8.11.2.jar</resource>
            <resource scope="module" name="com.fasterxml.jackson.core:jackson-annotations" version="2.8.0">lib/jackson-annotations-2.8.0.jar</resource>
            <resource scope="module" name="com.github.jai-imageio:jai-imageio-core" version="1.3.1">lib/jai-imageio-core-1.3.1.jar</resource>
            <resource scope="module" name="org.apache.commons:commons-lang3" version="3.7">lib/commons-lang3-3.7.jar</resource>
            <resource scope="module" name="org.apache.taglibs:taglibs-standard-impl" version="1.2.5">lib/taglibs-standard-impl-1.2.5.jar</resource>
            <resource scope="module" name="commons-beanutils:commons-beanutils" version="1.9.3">lib/commons-beanutils-1.9.3.jar</resource>
            <resource scope="module" name="commons-collections:commons-collections" version="3.2.2">lib/commons-collections-3.2.2.jar</resource>
            <resource scope="module" name="commons-io:commons-io" version="1.3.2">lib/commons-io-1.3.2.jar</resource>
            <resource scope="module" name="net.sf.jsignature.io-tools:easystream" version="1.2.15">lib/easystream-1.2.15.jar</resource>
            <resource scope="module" name="org.slf4j:slf4j-api" version="1.6.4">lib/slf4j-api-1.6.4.jar</resource>
            <resource scope="module" name="org.slf4j:slf4j-log4j12" version="1.6.4">lib/slf4j-log4j12-1.6.4.jar</resource>
            <resource scope="module" name="log4j:log4j" version="1.2.17">lib/log4j-1.2.17.jar</resource>
            <resource scope="module" name="javax.mail:javax.mail-api" version="1.4.4">lib/javax.mail-api-1.4.4.jar</resource>
            <resource scope="module" name="javax.activation:activation" version="1.1.1">lib/activation-1.1.1.jar</resource>
            <resource scope="module" name="org.mozilla:rhino" version="1.7.7.1">lib/rhino-1.7.7.1.jar</resource>
            <resource scope="module" name="cglib:cglib-nodep" version="3.2.7">lib/cglib-nodep-3.2.7.jar</resource>
            <resource scope="module" name="com.octo.captcha:jcaptcha" version="2.0-alpha-1">lib/jcaptcha-2.0-alpha-1.jar</resource>
            <resource scope="module" name="com.jhlabs:filters" version="2.0.235">lib/filters-2.0.235.jar</resource>
            <resource scope="module" name="com.octo.captcha:jcaptcha-api" version="2.0-alpha-1">lib/jcaptcha-api-2.0-alpha-1.jar</resource>
            <resource scope="module" name="org.springframework:spring-context" version="4.3.22.RELEASE">lib/spring-context-4.3.22.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-aop" version="4.3.22.RELEASE">lib/spring-aop-4.3.22.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-expression" version="4.3.22.RELEASE">lib/spring-expression-4.3.22.RELEASE.jar</resource>
            <resource scope="module" name="commons-fileupload:commons-fileupload" version="1.3.1">lib/commons-fileupload-1.3.1.jar</resource>
            <resource scope="module" name="commons-validator:commons-validator" version="1.6">lib/commons-validator-1.6.jar</resource>
            <resource scope="module" name="commons-digester:commons-digester" version="1.8.1">lib/commons-digester-1.8.1.jar</resource>
            <resource scope="module" name="org.springframework:spring-web" version="4.3.22.RELEASE">lib/spring-web-4.3.22.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-webmvc" version="4.3.22.RELEASE">lib/spring-webmvc-4.3.22.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-core" version="4.3.22.RELEASE">lib/spring-core-4.3.22.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-context-support" version="4.3.22.RELEASE">lib/spring-context-support-4.3.22.RELEASE.jar</resource>
            <resource scope="module" name="com.smardec:license4j" version="1.6">lib/license4j-1.6.jar</resource>
            <resource scope="module" name="org.apache.poi:poi-ooxml" version="3.17">lib/poi-ooxml-3.17.jar</resource>
            <resource scope="module" name="org.apache.poi:poi-ooxml-schemas" version="3.17">lib/poi-ooxml-schemas-3.17.jar</resource>
            <resource scope="module" name="org.apache.xmlbeans:xmlbeans" version="2.6.0">lib/xmlbeans-2.6.0.jar</resource>
            <resource scope="module" name="com.github.virtuald:curvesapi" version="1.04">lib/curvesapi-1.04.jar</resource>
            <resource scope="module" name="com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer" version="20180219.1">lib/owasp-java-html-sanitizer-20180219.1.jar</resource>
            <resource scope="module" name="com.google.guava:guava" version="11.0.2">lib/guava-11.0.2.jar</resource>
            <resource scope="module" name="org.apache.poi:poi" version="3.17">lib/poi-3.17.jar</resource>
            <resource scope="module" name="commons-codec:commons-codec" version="1.6">lib/commons-codec-1.6.jar</resource>
            <resource scope="module" name="org.apache.commons:commons-collections4" version="4.1">lib/commons-collections4-4.1.jar</resource>
            <resource scope="module" name="org.springframework:spring-beans" version="4.3.22.RELEASE">lib/spring-beans-4.3.22.RELEASE.jar</resource>
            <resource scope="module" name="javax.servlet.jsp.jstl:javax.servlet.jsp.jstl-api" version="1.2.2">lib/javax.servlet.jsp.jstl-api-1.2.2.jar</resource>
            <resource scope="module" name="org.apache.tika:tika-core" version="1.17">lib/tika-core-1.17.jar</resource>
            <resource scope="module" name="com.google.code.gson:gson" version="2.8.2">lib/gson-2.8.2.jar</resource>
            <resource scope="module" name="com.atlassian.commonmark:commonmark" version="0.12.1">lib/commonmark-0.12.1.jar</resource>
            <resource scope="module" name="com.atlassian.commonmark:commonmark-ext-gfm-tables" version="0.12.1">lib/commonmark-ext-gfm-tables-0.12.1.jar</resource>
            <resource scope="module" name="com.monday.commonmark:commonmark-ext-link-attributes" version="1.0.1">lib/commonmark-ext-link-attributes-1.0.1.jar</resource>
            <resource scope="module" name="org.mapdb:mapdb" version="1.0.6">lib/mapdb-1.0.6.jar</resource>
            <resource scope="module" name="org.freemarker:freemarker" version="2.3.28">lib/freemarker-2.3.28.jar</resource>
            <resource scope="module" name="org.imgscalr:imgscalr-lib" version="4.2">lib/imgscalr-lib-4.2.jar</resource>
            <resource scope="module" name="org.apache.httpcomponents:httpclient" version="4.4">lib/httpclient-4.4.jar</resource>
            <resource scope="module" name="org.apache.httpcomponents:httpcore" version="4.4">lib/httpcore-4.4.jar</resource>
            <resource scope="module" name="commons-logging:commons-logging" version="1.1.3">lib/commons-logging-1.1.3.jar</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-analytics.properties</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-mail.properties</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-network.properties</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-license.txt</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-license.properties</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-pdf.properties</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-connection.properties</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/pageBreak.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/layout.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/error.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/checkBoxGroup.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/fileUpload.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/captcha.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/success.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/uploadSizeExceeded.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/exit.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/textArea.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/pdf.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/redirect.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/radioGroup.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/summary.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/condition.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/default.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/button.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/webform.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/passwordField.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/paragraph.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/hiddenField.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/methodNotSupported.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/inputField.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/optin.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/calculatedValue.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/comboBox.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/sessionExpired.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/invalidLicense.jsp</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-excel.properties</resource>
            <resource target="/WEB-INF">WEB-INF/log4j.properties</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-views.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-resourcebundle.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-mail.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-actions.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-analytics.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-network.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-application.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-services.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-license.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-connection.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-controllers.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-captcha.xml</resource>
          </web-resources>
        </web-app>
        <web-app scopes="global">
          <name>Monday Webforms Analytics Backend</name>
          <version>2.2.2</version>
          <description>Monday Webforms Analytics Backend FIRSTspirit integration.</description>
          <configurable>com.monday.webforms.admin.analytics.backend.WebAppConfiguration</configurable>
          <class>com.monday.webforms.admin.WebformsWebApp</class>
          <web-xml>external/backend/WEB-INF/web.xml</web-xml>
          <resources>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-gui" version="5.5.0-SNAPSHOT">lib/webforms-gui-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="com.fifesoft:rsyntaxtextarea" version="2.6.1">lib/rsyntaxtextarea-2.6.1.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-common" version="5.5.0-SNAPSHOT">lib/webforms-common-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-shared" version="5.5.0-SNAPSHOT">lib/webforms-shared-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="commons-io:commons-io" version="1.3.2">lib/commons-io-1.3.2.jar</resource>
            <resource scope="module" name="org.slf4j:slf4j-api" version="1.6.4">lib/slf4j-api-1.6.4.jar</resource>
            <resource scope="module" name="org.mozilla:rhino" version="1.7.7.1">lib/rhino-1.7.7.1.jar</resource>
            <resource scope="module" name="com.google.code.gson:gson" version="2.8.2">lib/gson-2.8.2.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:jPod" version="5.5.1">lib/jPod-5.5.1.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:iscwt" version="5.5">lib/iscwt-5.5.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isfreetype" version="5.5">lib/isfreetype-5.5.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isnativec" version="5.5">lib/isnativec-5.5.jar</resource>
            <resource scope="module" name="net.java.dev.jna:jna" version="4.1.0">lib/jna-4.1.0.jar</resource>
            <resource scope="module" name="javax.media:jai-core" version="1.1.3">lib/jai-core-1.1.3.jar</resource>
            <resource scope="module" name="com.sun.media:jai-codec" version="1.1.3">lib/jai-codec-1.1.3.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:isrt" version="4.10">lib/isrt-4.10.jar</resource>
            <resource scope="module" name="de.intarsys.opensource:jbig2" version="5.5.1">lib/jbig2-5.5.1.jar</resource>
            <resource scope="module" name="com.monday:monday-common" version="3.1.1">lib/monday-common-3.1.1.jar</resource>
            <resource scope="module" name="javax.mail:javax.mail-api" version="1.4.4">lib/javax.mail-api-1.4.4.jar</resource>
            <resource scope="module" name="javax.activation:activation" version="1.1.1">lib/activation-1.1.1.jar</resource>
            <resource scope="module" name="de.espirit.firstspirit:fs-access" version="5.2.181206">lib/fs-access-5.2.181206.jar</resource>
            <resource scope="module" name="com.smardec:license4j" version="1.6">lib/license4j-1.6.jar</resource>
            <resource scope="module" name="net.java.dev.designgridlayout:designgridlayout" version="1.1">lib/designgridlayout-1.1.jar</resource>
            <resource scope="module" name="net.java.dev.swing-layout:swing-layout" version="1.0.2">lib/swing-layout-1.0.2.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-executables" version="5.5.0-SNAPSHOT">lib/webforms-executables-5.5.0-SNAPSHOT.jar</resource>
            <resource scope="module" name="com.monday.webforms.firstspirit:webforms-admin" version="5.5.0-SNAPSHOT">lib/webforms-admin-5.5.0-SNAPSHOT.jar</resource>
          </resources>
          <web-resources>
            <resource scope="module" name="com.monday.webforms.analytics:webforms-backend-webapp-lib" version="2.2.2">external/backend/WEB-INF/lib/webforms-backend-webapp-lib-2.2.2.jar</resource>
            <resource scope="module" name="com.monday.webforms.analytics:webforms-backend-common" version="2.2.2">external/backend/WEB-INF/lib/webforms-backend-common-2.2.2.jar</resource>
            <resource scope="module" name="com.monday.webforms.analytics:webforms-backend-api-model" version="2.2.2">external/backend/WEB-INF/lib/webforms-backend-api-model-2.2.2.jar</resource>
            <resource scope="module" name="com.fasterxml.jackson.core:jackson-annotations" version="2.8.0">external/backend/WEB-INF/lib/jackson-annotations-2.8.0.jar</resource>
            <resource scope="module" name="com.monday:monday-common" version="3.1.0">external/backend/WEB-INF/lib/monday-common-3.1.0.jar</resource>
            <resource scope="module" name="org.freemarker:freemarker" version="2.3.28">external/backend/WEB-INF/lib/freemarker-2.3.28.jar</resource>
            <resource scope="module" name="org.apache.commons:commons-csv" version="1.5">external/backend/WEB-INF/lib/commons-csv-1.5.jar</resource>
            <resource scope="module" name="org.apache.commons:commons-lang3" version="3.7">external/backend/WEB-INF/lib/commons-lang3-3.7.jar</resource>
            <resource scope="module" name="org.apache.poi:poi" version="3.17">external/backend/WEB-INF/lib/poi-3.17.jar</resource>
            <resource scope="module" name="org.apache.commons:commons-collections4" version="4.1">external/backend/WEB-INF/lib/commons-collections4-4.1.jar</resource>
            <resource scope="module" name="org.apache.poi:poi-ooxml" version="3.17">external/backend/WEB-INF/lib/poi-ooxml-3.17.jar</resource>
            <resource scope="module" name="org.apache.poi:poi-ooxml-schemas" version="3.17">external/backend/WEB-INF/lib/poi-ooxml-schemas-3.17.jar</resource>
            <resource scope="module" name="org.apache.xmlbeans:xmlbeans" version="2.6.0">external/backend/WEB-INF/lib/xmlbeans-2.6.0.jar</resource>
            <resource scope="module" name="stax:stax-api" version="1.0.1">external/backend/WEB-INF/lib/stax-api-1.0.1.jar</resource>
            <resource scope="module" name="com.github.virtuald:curvesapi" version="1.04">external/backend/WEB-INF/lib/curvesapi-1.04.jar</resource>
            <resource scope="module" name="org.apache.xbean:xbean-classloader" version="4.8">external/backend/WEB-INF/lib/xbean-classloader-4.8.jar</resource>
            <resource scope="module" name="org.springframework.boot:spring-boot-starter-amqp" version="1.5.15.RELEASE">external/backend/WEB-INF/lib/spring-boot-starter-amqp-1.5.15.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-messaging" version="4.3.18.RELEASE">external/backend/WEB-INF/lib/spring-messaging-4.3.18.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework.amqp:spring-rabbit" version="1.7.9.RELEASE">external/backend/WEB-INF/lib/spring-rabbit-1.7.9.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework.amqp:spring-amqp" version="1.7.9.RELEASE">external/backend/WEB-INF/lib/spring-amqp-1.7.9.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework.retry:spring-retry" version="1.2.2.RELEASE">external/backend/WEB-INF/lib/spring-retry-1.2.2.RELEASE.jar</resource>
            <resource scope="module" name="com.rabbitmq:http-client" version="1.1.1.RELEASE">external/backend/WEB-INF/lib/http-client-1.1.1.RELEASE.jar</resource>
            <resource scope="module" name="org.apache.httpcomponents:httpclient" version="4.5.6">external/backend/WEB-INF/lib/httpclient-4.5.6.jar</resource>
            <resource scope="module" name="org.apache.httpcomponents:httpcore" version="4.4.10">external/backend/WEB-INF/lib/httpcore-4.4.10.jar</resource>
            <resource scope="module" name="com.rabbitmq:amqp-client" version="4.0.3">external/backend/WEB-INF/lib/amqp-client-4.0.3.jar</resource>
            <resource scope="module" name="org.springframework.boot:spring-boot-starter-actuator" version="1.5.15.RELEASE">external/backend/WEB-INF/lib/spring-boot-starter-actuator-1.5.15.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework.boot:spring-boot-actuator" version="1.5.15.RELEASE">external/backend/WEB-INF/lib/spring-boot-actuator-1.5.15.RELEASE.jar</resource>
            <resource scope="module" name="javax.persistence:persistence-api" version="1.0.2">external/backend/WEB-INF/lib/persistence-api-1.0.2.jar</resource>
            <resource scope="module" name="org.apache.commons:commons-math3" version="3.6.1">external/backend/WEB-INF/lib/commons-math3-3.6.1.jar</resource>
            <resource scope="module" name="com.google.code.gson:gson" version="2.8.5">external/backend/WEB-INF/lib/gson-2.8.5.jar</resource>
            <resource scope="module" name="org.jetbrains:annotations" version="13.0">external/backend/WEB-INF/lib/annotations-13.0.jar</resource>
            <resource scope="module" name="org.flywaydb:flyway-core" version="3.2.1">external/backend/WEB-INF/lib/flyway-core-3.2.1.jar</resource>
            <resource scope="module" name="com.thoughtworks.xstream:xstream" version="1.4.10">external/backend/WEB-INF/lib/xstream-1.4.10.jar</resource>
            <resource scope="module" name="xmlpull:xmlpull" version="1.1.3.1">external/backend/WEB-INF/lib/xmlpull-1.1.3.1.jar</resource>
            <resource scope="module" name="xpp3:xpp3_min" version="1.1.4c">external/backend/WEB-INF/lib/xpp3_min-1.1.4c.jar</resource>
            <resource scope="module" name="io.reactivex:rxjava" version="1.3.8">external/backend/WEB-INF/lib/rxjava-1.3.8.jar</resource>
            <resource scope="module" name="org.springframework.boot:spring-boot-legacy" version="1.1.0.RELEASE">external/backend/WEB-INF/lib/spring-boot-legacy-1.1.0.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework.security:spring-security-core" version="4.2.7.RELEASE">external/backend/WEB-INF/lib/spring-security-core-4.2.7.RELEASE.jar</resource>
            <resource scope="module" name="aopalliance:aopalliance" version="1.0">external/backend/WEB-INF/lib/aopalliance-1.0.jar</resource>
            <resource scope="module" name="org.springframework:spring-aop" version="4.3.18.RELEASE">external/backend/WEB-INF/lib/spring-aop-4.3.18.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-beans" version="4.3.18.RELEASE">external/backend/WEB-INF/lib/spring-beans-4.3.18.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-context" version="4.3.18.RELEASE">external/backend/WEB-INF/lib/spring-context-4.3.18.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-core" version="4.3.18.RELEASE">external/backend/WEB-INF/lib/spring-core-4.3.18.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-expression" version="4.3.18.RELEASE">external/backend/WEB-INF/lib/spring-expression-4.3.18.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework.security:spring-security-web" version="4.2.7.RELEASE">external/backend/WEB-INF/lib/spring-security-web-4.2.7.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-web" version="4.3.18.RELEASE">external/backend/WEB-INF/lib/spring-web-4.3.18.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework.security:spring-security-config" version="4.2.7.RELEASE">external/backend/WEB-INF/lib/spring-security-config-4.2.7.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework.security.oauth:spring-security-oauth2" version="2.0.15.RELEASE">external/backend/WEB-INF/lib/spring-security-oauth2-2.0.15.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-webmvc" version="4.3.18.RELEASE">external/backend/WEB-INF/lib/spring-webmvc-4.3.18.RELEASE.jar</resource>
            <resource scope="module" name="commons-codec:commons-codec" version="1.10">external/backend/WEB-INF/lib/commons-codec-1.10.jar</resource>
            <resource scope="module" name="org.codehaus.jackson:jackson-mapper-asl" version="1.9.13">external/backend/WEB-INF/lib/jackson-mapper-asl-1.9.13.jar</resource>
            <resource scope="module" name="org.codehaus.jackson:jackson-core-asl" version="1.9.13">external/backend/WEB-INF/lib/jackson-core-asl-1.9.13.jar</resource>
            <resource scope="module" name="org.springframework.security:spring-security-aspects" version="4.2.7.RELEASE">external/backend/WEB-INF/lib/spring-security-aspects-4.2.7.RELEASE.jar</resource>
            <resource scope="module" name="org.slf4j:slf4j-api" version="1.7.12">external/backend/WEB-INF/lib/slf4j-api-1.7.12.jar</resource>
            <resource scope="module" name="org.springframework.boot:spring-boot-starter" version="1.5.15.RELEASE">external/backend/WEB-INF/lib/spring-boot-starter-1.5.15.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework.boot:spring-boot" version="1.5.15.RELEASE">external/backend/WEB-INF/lib/spring-boot-1.5.15.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework.boot:spring-boot-autoconfigure" version="1.5.15.RELEASE">external/backend/WEB-INF/lib/spring-boot-autoconfigure-1.5.15.RELEASE.jar</resource>
            <resource scope="module" name="org.yaml:snakeyaml" version="1.17">external/backend/WEB-INF/lib/snakeyaml-1.17.jar</resource>
            <resource scope="module" name="org.springframework.boot:spring-boot-starter-log4j2" version="1.5.15.RELEASE">external/backend/WEB-INF/lib/spring-boot-starter-log4j2-1.5.15.RELEASE.jar</resource>
            <resource scope="module" name="org.apache.logging.log4j:log4j-slf4j-impl" version="2.7">external/backend/WEB-INF/lib/log4j-slf4j-impl-2.7.jar</resource>
            <resource scope="module" name="org.apache.logging.log4j:log4j-api" version="2.7">external/backend/WEB-INF/lib/log4j-api-2.7.jar</resource>
            <resource scope="module" name="org.apache.logging.log4j:log4j-core" version="2.7">external/backend/WEB-INF/lib/log4j-core-2.7.jar</resource>
            <resource scope="module" name="org.slf4j:jcl-over-slf4j" version="1.7.12">external/backend/WEB-INF/lib/jcl-over-slf4j-1.7.12.jar</resource>
            <resource scope="module" name="org.slf4j:jul-to-slf4j" version="1.7.25">external/backend/WEB-INF/lib/jul-to-slf4j-1.7.25.jar</resource>
            <resource scope="module" name="org.springframework.boot:spring-boot-starter-web" version="1.5.15.RELEASE">external/backend/WEB-INF/lib/spring-boot-starter-web-1.5.15.RELEASE.jar</resource>
            <resource scope="module" name="org.hibernate:hibernate-validator" version="5.3.6.Final">external/backend/WEB-INF/lib/hibernate-validator-5.3.6.Final.jar</resource>
            <resource scope="module" name="javax.validation:validation-api" version="1.1.0.Final">external/backend/WEB-INF/lib/validation-api-1.1.0.Final.jar</resource>
            <resource scope="module" name="org.jboss.logging:jboss-logging" version="3.3.2.Final">external/backend/WEB-INF/lib/jboss-logging-3.3.2.Final.jar</resource>
            <resource scope="module" name="com.fasterxml:classmate" version="1.3.4">external/backend/WEB-INF/lib/classmate-1.3.4.jar</resource>
            <resource scope="module" name="com.fasterxml.jackson.core:jackson-databind" version="2.8.11.2">external/backend/WEB-INF/lib/jackson-databind-2.8.11.2.jar</resource>
            <resource scope="module" name="com.fasterxml.jackson.core:jackson-core" version="2.8.11">external/backend/WEB-INF/lib/jackson-core-2.8.11.jar</resource>
            <resource scope="module" name="org.springframework.boot:spring-boot-starter-jdbc" version="1.5.15.RELEASE">external/backend/WEB-INF/lib/spring-boot-starter-jdbc-1.5.15.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-jdbc" version="4.3.18.RELEASE">external/backend/WEB-INF/lib/spring-jdbc-4.3.18.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-tx" version="4.3.18.RELEASE">external/backend/WEB-INF/lib/spring-tx-4.3.18.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework.boot:spring-boot-starter-mail" version="1.5.15.RELEASE">external/backend/WEB-INF/lib/spring-boot-starter-mail-1.5.15.RELEASE.jar</resource>
            <resource scope="module" name="org.springframework:spring-context-support" version="4.3.18.RELEASE">external/backend/WEB-INF/lib/spring-context-support-4.3.18.RELEASE.jar</resource>
            <resource scope="module" name="com.sun.mail:javax.mail" version="1.5.6">external/backend/WEB-INF/lib/javax.mail-1.5.6.jar</resource>
            <resource scope="module" name="javax.activation:activation" version="1.1">external/backend/WEB-INF/lib/activation-1.1.jar</resource>
            <resource scope="module" name="org.apache.commons:commons-dbcp2" version="2.3.0">external/backend/WEB-INF/lib/commons-dbcp2-2.3.0.jar</resource>
            <resource scope="module" name="org.apache.commons:commons-pool2" version="2.4.3">external/backend/WEB-INF/lib/commons-pool2-2.4.3.jar</resource>
            <resource scope="module" name="commons-logging:commons-logging" version="1.2">external/backend/WEB-INF/lib/commons-logging-1.2.jar</resource>
            <resource target="/WEB-INF/classes">external/backend/WEB-INF/classes/log4j2.xml</resource>
            <resource target="/WEB-INF/classes">external/backend/WEB-INF/classes/banner.txt</resource>
            <resource target="/WEB-INF/classes">external/backend/WEB-INF/classes/application.properties</resource>
            <resource target="/WEB-INF/lib-migration">external/backend/WEB-INF/lib-migration/spring-security-oauth2-2.0.14.RELEASE.jar</resource>
            <resource target="/WEB-INF/lib-migration">external/backend/WEB-INF/lib-migration/spring-security-core-4.0.4.RELEASE.jar</resource>
            <resource target="/WEB-INF">external/backend/WEB-INF/webforms-backend.properties</resource>
            <resource target="/WEB-INF/spring">external/backend/WEB-INF/spring/backend-custom-overrides.xml</resource>
            <resource target="/WEB-INF/spring">external/backend/WEB-INF/spring/backend-authentication.xml</resource>
            <resource target="/WEB-INF/spring">external/backend/WEB-INF/spring/backend-security.xml</resource>
          </web-resources>
        </web-app>
      </components>
    </module>
```
