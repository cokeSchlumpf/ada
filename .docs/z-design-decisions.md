# Design Decisions

## 001 Spring Boot vs MicroProfile

**Context:** For the server component which bundles the whole application we need a simple container for serving the application. Allowing to easily implement things like auth/ auth, logging, etc..

**Status:** Accepted

**Decision:** We use Spring Boot as PoC with MicroProfile has shown several disadvantages, especially in developer experience and performance.

**Consequences:** We are not totally bound to Spring Boot; at a later stage we can create other server layers. Most of the business logic is implemented in other components.