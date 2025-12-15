# Custom Token Mapper for Keycloak

A production-ready **Custom Protocol Mapper** for Keycloak that dynamically injects structured request parameters into issued tokens. This mapper reads request parameters prefixed with `custom_value.*`, constructs a JSON object, and adds it as a configurable claim in **Access Tokens**, **ID Tokens**, and **UserInfo responses**.

Fully compatible with **Keycloak 22.0.1+** and implemented using the official Keycloak SPI.

---

## Features

* Custom Protocol Mapper for Keycloak 22.0.1 and above
* Builds a JSON object from request parameters (`custom_value.*`)
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

### Example Token Request

```http
POST /realms/{realm}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=my-client
&client_secret=secret
&grant_type=client_credentials
&custom_value.id=1
&custom_value.role=admin
```

### Resulting Token Claim

If the configured claim name is `customObject`, the token will contain:

```json
{
  "customObject": {
    "id": 1,
    "role": "admin"
  }
}
```

> Numeric values are automatically parsed as integers when possible.

---

## How It Works

* Reads decoded form parameters from the token request
* Filters parameters starting with `custom_value.`
* Removes the prefix and builds a `Map<String, Object>`
* Attempts integer conversion for numeric values
* Injects the object into the token under the configured claim name

---

## Security Considerations

* Do **not** expose sensitive or unvalidated user input
* Always use HTTPS for token requests
* Extend validation logic if mapping user-controlled data

---

## Extensibility

You can extend this mapper to:

* Support nested objects or arrays
* Read values from headers or session attributes
* Add conditional logic per client or realm

---

## License

This project is released under the **MIT License**.
