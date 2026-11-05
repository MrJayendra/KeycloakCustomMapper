# Custom Token Mapper for Keycloak

A production-ready **Custom Protocol Mapper** for Keycloak that dynamically injects structured request parameters into issued tokens. This mapper reads request parameters prefixed with `custom_value.*`, constructs a JSON object, and adds it as a configurable claim in **Access Tokens**, **ID Tokens**, and **UserInfo responses**.

Fully compatible with **Keycloak 22.0.1+** and implemented using the official Keycloak SPI.

---

## Features

* Custom Protocol Mapper for Keycloak 22.0.1 and above
* Builds a JSON object from request parameters (`custom_value.*`)
* **Supports complex data structures:**
  * JSON objects (nested structures)
  * JSON arrays
  * Booleans
  * Integers
  * Decimals (doubles)
  * Strings
* Dynamically injects the object into tokens at runtime
* Supports Access Token, ID Token, and UserInfo Token
* Configurable claim name via Admin Console
* Easy deployment with standalone Keycloak or Docker

---

## Compatibility

| Component | Version          |
| --------- | ---------------- |
| Keycloak  | 22.0.1 or higher |
| Java      | 17 (recommended) |

---

## Installation

### 1. Build the JAR

Compile the provider using Maven:

```bash
mvn clean package
```

This will generate:

```text
target/custom-token-mapper-1.0.jar
```

### 2. Deploy to Keycloak

Copy the JAR into the Keycloak providers directory:

```text
/opt/keycloak/providers/custom-token-mapper-1.0.jar
```

### 3. Restart Keycloak

Restart Keycloak to load the provider:

```bash
./kc.sh restart
```

> For Docker setups, rebuild the image or restart the container after adding the JAR.

---

## Mapper Configuration

### Enable the Mapper

1. Log in to the **Keycloak Admin Console**
2. Select your **Realm**
3. Go to **Clients** → Select your client
4. Navigate to **Client Scopes** or **Protocol Mappers**
5. Click **Add Mapper**
6. Select **Custom Value Object Mapper**

### Mapper Settings

| Field               | Description                               |
| ------------------- | ----------------------------------------- |
| Token Claim Name    | Name of the JSON claim added to the token |
| Add to Access Token | Enable if required                        |
| Add to ID Token     | Enable if required                        |
| Add to UserInfo     | Enable if required                        |

Save the configuration.

---

## Usage

### Request Parameters

Send request parameters prefixed with `custom_value.` when requesting a token. Each parameter becomes a field inside a JSON object.

### Supported Value Types

| Type        | Example Value                          | Parsed As               |
| ----------- | -------------------------------------- | ----------------------- |
| JSON Object | `{"name":"John","age":30}`             | `Map<String, Object>`   |
| JSON Array  | `["admin","user","guest"]`             | `List<Object>`          |
| Boolean     | `true` or `false`                      | `Boolean`               |
| Integer     | `42`                                   | `Integer`               |
| Decimal     | `3.14`                                 | `Double`                |
| String      | `hello`                                | `String`                |

### Example Token Request (Simple Values)

```http
POST /realms/{realm}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=my-client
&client_secret=secret
&grant_type=client_credentials
&custom_value.id=1
&custom_value.role=admin
&custom_value.active=true
```

### Resulting Token Claim

```json
{
  "customObject": {
    "id": 1,
    "role": "admin",
    "active": true
  }
}
```

### Example Token Request (Complex Structures)

```http
POST /realms/{realm}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=my-client
&client_secret=secret
&grant_type=client_credentials
&custom_value.user={"name":"John","age":30,"email":"john@example.com"}
&custom_value.roles=["admin","user","manager"]
&custom_value.permissions={"read":true,"write":true,"delete":false}
&custom_value.score=95.5
&custom_value.verified=true
```

### Resulting Token Claim

```json
{
  "customObject": {
    "user": {
      "name": "John",
      "age": 30,
      "email": "john@example.com"
    },
    "roles": ["admin", "user", "manager"],
    "permissions": {
      "read": true,
      "write": true,
      "delete": false
    },
    "score": 95.5,
    "verified": true
  }
}
```

### cURL Example

```bash
curl -X POST "https://keycloak.example.com/realms/myrealm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=my-client" \
  -d "client_secret=secret" \
  -d "grant_type=client_credentials" \
  -d 'custom_value.profile={"name":"Alice","department":"Engineering"}' \
  -d 'custom_value.tags=["vip","enterprise"]' \
  -d 'custom_value.level=5' \
  -d 'custom_value.premium=true'
```

---

## How It Works

1. Reads decoded form parameters from the token request
2. Filters parameters starting with `custom_value.`
3. Removes the prefix and parses each value:
   - If value looks like JSON (`{...}` or `[...]`), parse as JSON object/array
   - If value is `true`/`false`, parse as boolean
   - If value is numeric, parse as integer or double
   - Otherwise, keep as string
4. Builds a `Map<String, Object>` with all parsed values
5. Injects the object into the token under the configured claim name

---

## Security Considerations

* Do **not** expose sensitive or unvalidated user input
* Always use HTTPS for token requests
* Validate JSON input on the client side before sending
* Be cautious with deeply nested structures (potential for large tokens)
* Extend validation logic if mapping user-controlled data

---

## Troubleshooting

### JSON not being parsed

Ensure your JSON values are properly URL-encoded when sending via form parameters:

```bash
# Correct - URL encoded
custom_value.data=%7B%22key%22%3A%22value%22%7D

# Or use single quotes in curl to prevent shell interpretation
-d 'custom_value.data={"key":"value"}'
```

### Value treated as string instead of number

The mapper attempts to parse values in this order:
1. JSON object/array (if starts with `{` or `[`)
2. Boolean (if `true` or `false`)
3. Integer (if all digits)
4. Double (if contains decimal point)
5. String (fallback)

If parsing fails at any step, it falls back to the next type.

---

## License

This project is released under the **MIT License**.
