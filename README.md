# Monday FirstSpirit Module Maven Plugin [![Build Status](https://travis-ci.org/monday-consulting/fsm-maven-plugin.svg?branch=master)](https://travis-ci.org/monday-consulting/fsm-maven-plugin)

You can use this Maven plugin to generate XML descriptors for FirstSpirit modules.

Make sure you use maven 3.x.x or newer when integrating this plugin into your workspace.

# Getting started

## Plugin Configuration

* You have to configure the plugin with an config.xml, the prototype.xml and a target.
* Your plugin configuration should look like this:

```
    <plugin>
        <groupId>com.monday.maven.plugins</groupId>
        <artifactId>fsm-maven-plugin</artifactId>
        <configuration>
            <configXml>${basedir}/target/extra-resources/fsm-plugin.xml</configXml>
            <prototypeXml>${basedir}/target/extra-resources/prototype.module.xml</prototypeXml>
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

## Config XML

* In your config.xml you have to configure which folder or artifact should be included in your module descriptor.
* Your config.xml could look like this:

```
    <?xml version="1.0" encoding="UTF-8"?>
    <webforms-maven-plugin>
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
    </webforms-maven-plugin>
```
This example includes everything in the WEB-INF folder but excludes explicitly the folders WEB-INF/lib and WEB-INF/classes.

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

## Generated module XML

* A module.xml file generated by this plugin could look like this:

```
    <module>
      <name>Monday Webforms</name>
      <version>5.0.0-SNAPSHOT</version>
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
            <scope content="yes" link="yes" data="yes"/>
          </configuration>
          <resources>
            <resource>files/</resource>
            <resource scope="module">lib/webforms-common-5.0.0-SNAPSHOT.jar</resource>
            <resource scope="module">lib/commons-io-1.4.jar</resource>
            <resource scope="module">lib/rhino-1.7R5.jar</resource>
            <resource scope="module">lib/jPod-5.5.1.jar</resource>
            <resource scope="module">lib/iscwt-5.5.jar</resource>
            <resource scope="module">lib/isfreetype-5.5.jar</resource>
            <resource scope="module">lib/isnativec-5.5.jar</resource>
            <resource scope="module">lib/jna-3.2.7.jar</resource>
            <resource scope="module">lib/jai-core-1.1.3.jar</resource>
            <resource scope="module">lib/jai-codec-1.1.3.jar</resource>
            <resource scope="module">lib/isrt-4.10.jar</resource>
            <resource scope="module">lib/jbig2-5.5.1.jar</resource>
            <resource scope="module">lib/license4j-1.6.jar</resource>
            <resource scope="module">lib/webforms-gui-5.0.0-SNAPSHOT.jar</resource>
            <resource scope="module">lib/webforms-admin-5.0.0-SNAPSHOT.jar</resource>
            <resource scope="module">lib/designgridlayout-1.10.jar</resource>
            <resource scope="module">lib/monday-common-2.46.jar</resource>
            <resource scope="module">lib/activation-1.1.jar</resource>
            <resource scope="module">lib/mail-1.4.1.jar</resource>
            <resource scope="module">lib/fs-access-5.1.507.jar</resource>
            <resource scope="module">lib/slf4j-api-1.7.5.jar</resource>
            <resource scope="module">lib/gson-2.3.jar</resource>
            <resource scope="module">lib/webforms-editor-5.0.0-SNAPSHOT.jar</resource>
          </resources>
        </public>
        <web-app scopes="global">
          <name>Monday Webforms WebApp</name>
          <description>Monday Webforms FIRSTspirit integration.</description>
          <configurable>com.monday.webforms.admin.webapp.WebAppConfiguration</configurable>
          <class>com.monday.webforms.admin.WebformsWebApp</class>
          <web-xml>web.xml</web-xml>
          <resources>
            <resource scope="module">lib/webforms-gui-5.0.0-SNAPSHOT.jar</resource>
            <resource scope="module">lib/webforms-common-5.0.0-SNAPSHOT.jar</resource>
            <resource scope="module">lib/commons-io-1.4.jar</resource>
            <resource scope="module">lib/slf4j-api-1.7.5.jar</resource>
            <resource scope="module">lib/rhino-1.7R5.jar</resource>
            <resource scope="module">lib/gson-2.3.jar</resource>
            <resource scope="module">lib/jPod-5.5.1.jar</resource>
            <resource scope="module">lib/iscwt-5.5.jar</resource>
            <resource scope="module">lib/isfreetype-5.5.jar</resource>
            <resource scope="module">lib/isnativec-5.5.jar</resource>
            <resource scope="module">lib/jna-3.2.7.jar</resource>
            <resource scope="module">lib/jai-core-1.1.3.jar</resource>
            <resource scope="module">lib/jai-codec-1.1.3.jar</resource>
            <resource scope="module">lib/isrt-4.10.jar</resource>
            <resource scope="module">lib/jbig2-5.5.1.jar</resource>
            <resource scope="module">lib/monday-common-2.46.jar</resource>
            <resource scope="module">lib/activation-1.1.jar</resource>
            <resource scope="module">lib/mail-1.4.1.jar</resource>
            <resource scope="module">lib/fs-access-5.1.507.jar</resource>
            <resource scope="module">lib/license4j-1.6.jar</resource>
            <resource scope="module">lib/designgridlayout-1.10.jar</resource>
            <resource scope="module">lib/webforms-admin-5.0.0-SNAPSHOT.jar</resource>
          </resources>
          <web-resources>
            <resource scope="module">lib/webforms-webapp-lib-5.0.0-SNAPSHOT.jar</resource>
            <resource scope="module">lib/webforms-common-5.0.0-SNAPSHOT.jar</resource>
            <resource scope="module">lib/jPod-5.5.1.jar</resource>
            <resource scope="module">lib/iscwt-5.5.jar</resource>
            <resource scope="module">lib/isfreetype-5.5.jar</resource>
            <resource scope="module">lib/isnativec-5.5.jar</resource>
            <resource scope="module">lib/jna-3.2.7.jar</resource>
            <resource scope="module">lib/jai-core-1.1.3.jar</resource>
            <resource scope="module">lib/jai-codec-1.1.3.jar</resource>
            <resource scope="module">lib/isrt-4.10.jar</resource>
            <resource scope="module">lib/jbig2-5.5.1.jar</resource>
            <resource scope="module">lib/webforms-spring-5.0.0-SNAPSHOT.jar</resource>
            <resource scope="module">lib/jcl-over-slf4j-1.7.5.jar</resource>
            <resource scope="module">lib/monday-common-2.46.jar</resource>
            <resource scope="module">lib/mail-1.4.1.jar</resource>
            <resource scope="module">lib/webforms-backend-client-1.0.2.jar</resource>
            <resource scope="module">lib/webforms-backend-common-1.0.2.jar</resource>
            <resource scope="module">lib/fs-access-5.1.507.jar</resource>
            <resource scope="module">lib/standard-1.1.2.jar</resource>
            <resource scope="module">lib/commons-beanutils-1.9.2.jar</resource>
            <resource scope="module">lib/commons-collections-3.2.1.jar</resource>
            <resource scope="module">lib/commons-io-1.4.jar</resource>
            <resource scope="module">lib/easystream-1.2.15.jar</resource>
            <resource scope="module">lib/slf4j-api-1.7.5.jar</resource>
            <resource scope="module">lib/slf4j-log4j12-1.7.5.jar</resource>
            <resource scope="module">lib/log4j-1.2.17.jar</resource>
            <resource scope="module">lib/rhino-1.7R5.jar</resource>
            <resource scope="module">lib/com.springsource.net.sf.cglib-2.1.3.jar</resource>
            <resource scope="module">lib/jcaptcha-2.0-alpha-1.jar</resource>
            <resource scope="module">lib/filters-2.0.235.jar</resource>
            <resource scope="module">lib/jcaptcha-api-2.0-alpha-1.jar</resource>
            <resource scope="module">lib/activation-1.1.jar</resource>
            <resource scope="module">lib/spring-context-4.1.8.RELEASE.jar</resource>
            <resource scope="module">lib/spring-aop-4.1.8.RELEASE.jar</resource>
            <resource scope="module">lib/aopalliance-1.0.jar</resource>
            <resource scope="module">lib/spring-expression-4.1.8.RELEASE.jar</resource>
            <resource scope="module">lib/commons-fileupload-1.3.1.jar</resource>
            <resource scope="module">lib/spring-web-4.1.8.RELEASE.jar</resource>
            <resource scope="module">lib/spring-webmvc-4.1.8.RELEASE.jar</resource>
            <resource scope="module">lib/spring-core-4.1.8.RELEASE.jar</resource>
            <resource scope="module">lib/license4j-1.6.jar</resource>
            <resource scope="module">lib/poi-ooxml-3.13.jar</resource>
            <resource scope="module">lib/poi-ooxml-schemas-3.13.jar</resource>
            <resource scope="module">lib/xmlbeans-2.6.0.jar</resource>
            <resource scope="module">lib/owasp-java-html-sanitizer-r239.jar</resource>
            <resource scope="module">lib/guava-18.0.jar</resource>
            <resource scope="module">lib/poi-3.13.jar</resource>
            <resource scope="module">lib/commons-codec-1.9.jar</resource>
            <resource scope="module">lib/spring-beans-4.1.8.RELEASE.jar</resource>
            <resource scope="module">lib/javax.servlet.jsp.jstl-api-1.2.1.jar</resource>
            <resource scope="module">lib/accountvalidator-1.0-494b170d6a.jar</resource>
            <resource scope="module">lib/tika-core-1.6.jar</resource>
            <resource scope="module">lib/gson-2.3.jar</resource>
            <resource scope="module">lib/markdown4j-2.2.jar</resource>
            <resource scope="module">lib/mapdb-1.0.6.jar</resource>
            <resource scope="module">lib/freemarker-2.3.22.jar</resource>
            <resource scope="module">lib/imgscalr-lib-4.2.jar</resource>
            <resource target="/WEB-INF">WEB-INF/log4j.properties</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-actions.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-analytics.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-captcha.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-connection.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-controllers.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-license.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-mail.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-resourcebundle.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-services.xml</resource>
            <resource target="/WEB-INF/spring">WEB-INF/spring/webforms-views.xml</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/button.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/calculatedValue.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/captcha.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/checkBoxGroup.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/comboBox.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/condition.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/default.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/error.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/exit.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/fileUpload.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/hiddenField.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/inputField.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/invalidLicense.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/layout.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/pageBreak.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/paragraph.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/passwordField.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/pdf.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/radioGroup.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/sessionExpired.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/success.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/summary.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/textArea.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/uploadSizeExceeded.jsp</resource>
            <resource target="/WEB-INF/templates">WEB-INF/templates/webform.jsp</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-analytics.properties</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-connection.properties</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-excel.properties</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-license.txt</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-mail.properties</resource>
            <resource target="/WEB-INF">WEB-INF/webforms-pdf.properties</resource>
          </web-resources>
        </web-app>
      </components>
    </module>
```
