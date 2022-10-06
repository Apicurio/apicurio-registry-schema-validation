# apicurio-registry-schema-validation

Standalone schema validation library with Apicurio Registry integration.

## What is this?

With this library you can manage your JSON and Protobuf Schemas in Apicurio Registry and easily use them from your Java applications to validate objects and to verify that a given object meets the requirements as defined by the Schema.

## Check it out

For json-schema, you can add this dependency to your pom.xml
```
<dependency>
    <groupId>io.apicurio</groupId>
    <artifactId>apicurio-registry-schema-validation-jsonschema</artifactId>
    <version>0.0.1.Final</version>
</dependency>
```

For protobuf you can add this other dependency to your pom.xml
```
<dependency>
    <groupId>io.apicurio</groupId>
    <artifactId>apicurio-registry-schema-validation-protobuf</artifactId>
    <version>0.0.2.Final</version>
</dependency>
```

Examples of this library for json-schema can be found in the [apicurio-registry-examples](https://github.com/Apicurio/apicurio-registry-examples/tree/master/jsonschema-validation) repository
