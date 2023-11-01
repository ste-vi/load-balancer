# load-balancer
Application which distributes incoming traffic across multiple servers to improve system performance, reliability, and availability

## Structure

### Balancer

Simple implementation of load-balancer.
Any service can register itself in balancer registry by specific URI path.
If there are more then one server with the same path for all incomming request, the best available (with the less amount of connections will be choosed)

Balancer handles load and http exceptions for missing path.
### Serviceone

Just sample service which contains get books http endpoint.
On startup register itself in load balancer registry, periodically pings load balancer to re-register and before shutting down, unregister itself.\

### Servicetwo

Just sample service which contains get authors http endpoint.
On startup register itself in load balancer registry, periodically pings load balancer to re-register and before shutting down, unregister itself.
