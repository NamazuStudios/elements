File openApiSpec = new File(basedir, "project/basic/element/target/classes/META-INF/openapi.json")
assert openApiSpec.exists() : "Expected build-time generated OpenAPI spec at ${openApiSpec}"

def content = openApiSpec.text
assert content.contains('"openapi"') : "Generated spec is missing the openapi root key"
assert content.contains('"paths"') : "Generated spec is missing a paths section"
assert content.length() > 200 : "Generated spec looks suspiciously small/empty (${content.length()} chars)"

return true
