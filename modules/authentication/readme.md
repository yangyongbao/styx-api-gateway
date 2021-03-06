# Authentication

This is request stage that applies authentication process to your incoming requests:

- If authentication success it will return a principal object
    - Adds security permission custom header to the request
    - Adds security metainfo custom headers to the request
- If authentication fails abort an stage and returns an 401 error (customizable)
       

## Installation

Maven:
```xml
<dependency>
  <groupId>com.dexmatech.styx</groupId>
  <artifactId>authentication</artifactId>
  <version>VERSION</version>
</dependency>

```
## Simplest usage

For the simplest usage you only has to provide:

- Header token key: We must to know where must extract the token in order to perform the authentication
- Authenticator dependency: This is your work, provide us an implementation from who really authenticates

```java
AuthenticationProvider authenticationProvider 

RequestPipelineStage stage = AuthenticationStage
                    .authenticationByToken("X-token")
                    .withAuthenticationProvider(authenticationProvider)
                    .build();

```

If a request enter to a pipeline with:

    request -> GET http://www.mydomain.com:8080/api/v1/users
    headers -> X-token : Xxcew23ASDzxwsadsadsad
    
And stage returns a Principal object with:
 
        permissions -> [(user,R),(user,W),(items,R)]
        meta info -> [(account, 101)]
   
Then the stage will return an success request to the pipeline with custom headers:

        X-security-permissions: user:R,user:W,items:R
        X-security-account: 101


## Authentication provider

In order to authenticate you must to provide an implementation of AuthenticationProvider:
```java
@FunctionalInterface
public interface AuthenticationProvider {
	CompletableFuture<Optional<Principal>> authenticate(String token);
}
```

Here you have some glue code to adapt your non-async code:

```java
    SecurityService securityService = // your real non async security service 

	AuthenticationProvider myAuthenticator = token -> {
    
    		return CompletableFuture.supplyAsync(() -> {
    
    			Optional<Principal> result = Optional.empty();
    			// supose your service returns a context with all your authentication info
    			YourSecurityContext yourContext = securityService.authenticate(token);
    			if (yourContext.isAuthenticated()) {
    				// here transform you permissions to our permissions (if not empty)
    				List<Permission> permissions = transformToPermissions(yourContext.getMyPermissions());
    				// here add any custom info you want in the request as headers
    				MetaInfo metaInfo = MetaInfo.initWith("account", yourContext.getAccountId());
    				return Optional.of(new Principal(permissions, metaInfo));
    
    			}
    			return result;
    		};
    	};
```

This is only an example, for a custom usages please we recommend you to create a DSL as explained in 
[Implementing complex stage](https://github.com/dexma/styx-api-gateway#implementing-complex-stage)

#### Principal

If authentication succeed you must to return a [Principal](https://en.wikipedia.org/wiki/Principal_(computer_security)) Object

A styx principal is composed by:

- List of permissions: A permission represents what are your token allowed to do. Single permission is composed also by:
    - target : An object or scope that permission refers 
    - action : Action allowed to do over the target
    
    Sample: permission in an api context could be **do posts on /items/\* calls**, then **Permission(target=items, action=Write)**
    
- Meta info: A dictionary of any info that you want to bring with your security context
    
Creating a principal:

```java
    Principal principal = new Principal(
    				Arrays.asList(Permission.of("items", "Write"),Permission.of("items", "Read")),
    				MetaInfo.initWith("acccount-id","2")
    		);
```

It will be 'as default' converted and added as http headers:

        X-security-permissions: items:Write,items:Read
        X-security-account-id: 2


## Custom usage

The default behaviour could be override:
```java
    RequestPipelineStage stage = AuthenticationStage
    				.authenticationByToken("X-token")
    				.withAuthenticationProvider(AUTHENTICATION_PROVIDER)
    				// if you want to change the default fail response 
    				// you must to implement a function request -> response
    				.whenAuthenticationFailsRespondWith(httpRequest -> null)
    				// if you want to override how your principal permission and transformed to a headers
    				// you must to implement a function metaInfo -> headers
    				.generatingMetaInfoHeadersWith(metaInfo -> null)
    				// if you want to override how your principal meta info and transformed to a headers
    				// you must to implement a function permissions -> headers
    				.generatingPermissionHeadersWith(permissions -> null)
    				.build();
```
