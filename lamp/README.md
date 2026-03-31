# Lamp Digital Twin - Microservices Architecture


## Services

### Physical Service: port 8081
Simulate the real lamp, sending the state of it to kafka every second.
The state of the physical lamp : 
-Control of ON/OFF.
-Brightness of lamp.
-Romm temp, and temperature of the lamp.

### Model Service: port 8084
The calculation and brain of DT lamp example, consume data from physical and simulation data from twin. It computes lamp's characteristics (Power, Energy, Lifespan, and TempStatus): 

### Digital Twin Service: port 8082
Has two functionalities:
-Mirrors the physical entity when the twining flag is on.
-Start and manage simulations built with different scenarios (steps).
Plus the possibility to control the physical entity (turn it On/Off)


### Shadow Service: port 8083
Consume model results from Model service and stores data coming from synchro (as Source = PHYSICAL) and from simulation (as Source = SIMULATION)


### Frontend: port 3000
the UI interface of the DT project 




## Run the project 

```
docker-compose up --build
```
Then open **http://localhost:3000**

