# Monday FirstSpirit Module Maven Plugin

You can use this Maven plugin to generate XML descriptors for FirstSpirit modules.

Make sure you use Maven 3.3 or newer when integrating this plugin into your workspace.

**Attention:** Due to internal changes in Maven 3.8.8 or 3.9.0 and newer, incomplete module.xml files and
FirstSpirit modules may be created with the old `dependencyToXML` goal. Therefore, you should migrate to
plug-in version 2.0.0 and the new `prepare` goal as quickly as possible.

# Getting started

## Maven Dependency [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.monday-consulting.maven.plugins/fsm-maven-plugin/badge.svg?style=flat)](http://mvnrepository.com/artifact/com.monday-consulting.maven.plugins/fsm-maven-plugin)

The FSM Maven plugin is published on Maven Central:

```
    <dependency>
      <groupId>com.monday-consulting.maven.plugins</groupId>
      <artifactId>fsm-maven-plugin</artifactId>
      <version>2.0.2</version>
    </dependency>
```

## Maven Goals

As of version 2.0.0, the recommended Maven goal is `prepare`.

- `prepare`
- `dependencyToXML` _(deprecated)_
- `attachFSM` _(deprecated)_

## Plugin Configuration

Two things are important for the configuration of the plugin:
- `configXml` defines a list of modules for which dependencies are collected. These are then inserted into the prototype
`module.xml`.
- `prototypeXml` is the template for the `module.xml` to be generated. This is completed with all dependencies.

Full examples can be found in the integration tests, e.g. [prepare-example](./src/it/prepare-example).

```
    <plugin>
        <groupId>com.monday-consulting.maven.plugins</groupId>
        <artifactId>fsm-maven-plugin</artifactId>
        <configuration>
            <configXml>${basedir}/target/extra-resources/fsm-plugin.xml</configXml>
            <prototypeXml>${basedir}/target/extra-resources/prototype.module.xml</prototypeXml>
        </configuration>
        <executions>
            <execution>
                <id>prepare-module</id>
                <phase>package</phase>
                <goals>
                    <goal>prepare</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
```

## Config XML

Here you configure for which modules dependencies should be collected. The identifier from `dependencyTagValueInXml`
can then be used in the `module.xml` prototype.

```
    <?xml version="1.0" encoding="UTF-8"?>
    <fsm-maven-plugin>
        <scopes>
            <scope>runtime</scope>
            <scope>compile</scope>
        </scopes>
        <modules>
            <module>
                <id>org.example:example-webapp:war</id>
                <dependencyTagValueInXml>example-webapp-resources</dependencyTagValueInXml>
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
        </modules>
    </fsm-maven-plugin>
```
This example includes everything in the WEB-INF folder but excludes explicitly the folders WEB-INF/lib and WEB-INF/classes.

## Prototype XML

This is the prototype FirstSpirit module descriptor which will be consumed by this plugin to generate the working
`module.xml`. We recommend filtering this prototype beforehand with the Maven Resources plugin so that all variables
(e.g. `${project.version}`) are replaced.

This is basically a `module.xml` with missing (web-)resources. Here, `<dependencies>identifier</dependencies>` is used
instead to refer to entries from the `configXml`.

```
    <module>
        <name>My FirstSpirit Module (I)</name>
        <version>${project.version}</version>
        <description>A simple example for a prototype.module.xml</description>
        <vendor>Monday Consulting GmbH</vendor>
        <components>
            <public>
                <name>MY_PUBLIC_COMPONENT</name>
                <description>A FirstSpirit Gadget</description>
                <class>de.espirit.firstspirit.module.GadgetSpecification</class>
                <configuration>
                    <gom>org.example.GomExampleEditor</gom>
                    <factory>org.example.gadgets.ExampleSwingGadgetFactory</factory>
                    <value>org.example.gadgets.ExmplaeValueEngineerFactory</value>
                    <scope data="yes" content="yes" link="yes"/>
                </configuration>
                <resources>
                    <resource>files/</resource>
                    <dependencies>my-gadget-dependencies</dependencies>
                </resources>
            </public>
            <web-app scopes="global">
                <name>Example WebApp</name>
                <description>Monday Webforms FIRSTspirit integration.</description>
                <configurable>org.example.webapp.WebAppConfiguration</configurable>
                <class>org.example.ExanokeWebApp</class>
                <web-xml>web.xml</web-xml>
                <resources>
                    <dependencies>example-webapp-config-resources</dependencies>
                </resources>
                <web-resources>
                    <dependencies>example-webapp-resources</dependencies>
                </web-resources>
            </web-app>
        </components>
    </module>
```

## Plugin output

The plugin prepares the folder structure of a FirstSpirit module, which already contains the `module.xml` as well as
`module-isolated.xml` and all required dependencies:

- `target/fsm-root`
  - `META-INF`
    - `module.xml`
    - `module-isolated.xml`
  - `lib`
    - _all JARs referenced in the module descriptor_

Finally, to create the FSM archive, the Maven Assembly Plugin is required. With the help of an assembly descriptor,
additional files (configurations, customized `web.xml`, etc.) can be added to the module. You can find examples of
this in the integration tests: [prepare-example](./src/it/prepare-example).
