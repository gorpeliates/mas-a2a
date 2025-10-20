### SWE-based Multi-Agent System Using the A2A Protocol and Koog Framework
Experimental project that enables communication between software agents using the A2A protocol and Koog framework.

### Architecture

```
                                 ┌──────────────────────────┐
                                 │      Client Agent        │
                                 │      (A2A Client )       │
                                 └────────────┬─────────────┘
                                              │
                             ┌────────────────┼────────────────┐
                             │                │                │
                             ▼                ▼                ▼
               ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
               │  Coding Agent    │  │ Reviewing Agent  │  │  Testing Agent   │
               │  (A2A Server)    │  │  (A2A Server)    │  │  (A2A Server)    │
               │  Port: 9996      │  │  Port: 9997      │  │  Port: 9998      │
               └─────────┬────────┘  └─────────┬────────┘  └─────────┬────────┘
                         │                     │                     │
                         │                     │                     │
                         └─────────────────────┼─────────────────────┘
                                               │            
                                               ▼           
                                 ┌──────────────────────────┐
                                 │      Client Agent        │
                                 │   (Receives Responses)   │
                                 └──────────────────────────┘
```


### Usage
- Run `docker compose up` to start the jaeger backend for tracing.
- Run `Launcher.kt` to start agents servers and `ConsoleApp.kt` to run the client console application to send requests to agents. 

You can accesses the Jaeger UI at `http://localhost:16686` to view traces of the messages.