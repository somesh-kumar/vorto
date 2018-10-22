# Eclipse Ditto Generator

### Overview

This generator features

- JSON Schema files for validating feature properties and feature messages of Things managed by [Eclipse Ditto](https://www.eclipse.org/ditto/)

You can optionally use the online tool [JSON Schema Faker](http://json-schema-faker.js.org) in order to generate example JSON content
after JSON Schema was generated by this generator.

## Example Usage

The following curl commands show, how you can invoke the Eclipse Ditto Generator

	curl -GET http://vorto.eclipse.org/api/v1/generators/eclipseditto/models/com.ipso.smartobjects.Load_Control:1.1.0