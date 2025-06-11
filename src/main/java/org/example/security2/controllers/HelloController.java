package org.example.security2.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 1 - At SecurityConfig -> add RobotFilter (with a meaningful class)
// 2 - Create the RobotFilter -> extends OncePerRequestFilter -> logic to authentication -> add RobotAuthentication to Context
// 3 - Create RobotAuthentication -> extends Authentication (represents either a logged in entity or a bunch of data
//     that need to be authenticated to validate if this is a legit entity that wants to log in). AuthenticationManager
//     (or its child AuthenticationProvider - an even more specialized interface) processes an Authentication entity
//     (in our case, an UsernamePasswordAuthenticationToken) and returns the same entity. However, the incoming entity
//     has Username and Password properties, while the returned entity has more detailed properties (UserDetails and Authorities).
//     This is a usual work flow:
//     -- AuthenticationManager is a generic interface that is implemented by ProviderManager.
//     -- AuthenticationManager receives an Authentication (for example a UsernamePasswordAuthenticationToken) with
//        username and password not validated yet.
//     -- It loops through the list of configured AuthenticationProvider (a specialized version of ProviderManager which
//     -- only deals with a specific token like UsernamePasswordAuthenticationToken).
//     -- For each provider, it asks provider.supports(token.getClass())? If true, ProviderManager gives the token to the provider (provider.authenticate(token))
//     -- In this case, provider returns a populated Authentication (with UserDetails and Authorities) to ProviderManager and
//        it returns the Authentication and the process is successful
// 4 - Create UserDetailsService -> returns InMemoryUserDetailsManager



@RestController
@RequestMapping("/sec")
public class HelloController {
    @GetMapping("/public")
    public String getHello() {
        return "Hello, public!";
    }

    @GetMapping("/private")
    public String getPrivate(Authentication auth) {
        return "This is private! Hello, " + auth.getName();
    }
}
