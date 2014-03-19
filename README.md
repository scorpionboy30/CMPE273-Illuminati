CMPE273-Illuminati
==================

zerorpc is a flexible RPC implementation based on zeromq and messagepack. Service APIs exposed with zerorpc are called "zeroservices".

This project provides java bindings for zerorpc.
Both the server as well as the client are written in java.
This zerorpc can be used programmatically.

I) 'sayHelloToServer' api exposed as a zeroservice


Access the zeroservice:
-----------------------

Create a <test-class-name>.java.

In its main(), start the server and the client.

Now call the api from the client.

e.g. /src/test/TestZeroRPC.java


To Run this project:
--------------------

Install maven on your local machine.

Fork this repo and clone it to your local machine.

In the command prompt, traverse to this repo.

> mvn clean package

> mvn exec:java -Dexec.mainClass="test.TestZeroRPC"