CMPE273-Illuminati
==================

zerorpc is a flexible RPC implementation based on zeromq and messagepack. Service APIs exposed with zerorpc are called "zeroservices".

This project provides java bindings for zerorpc.
Both the server as well as the client are written in java.
This zerorpc can be used programmatically.

I) basic arithematic operations api exposed as a zeroservice
II) different datatypes like array,hashmap can be sent from client to server and received back to client.
III) error propagation from server to client.

Access the zeroservice:
-----------------------

Create a "test-class-name".java.

In its main(), start the server and the client.

Now call the api from this class.

e.g. /src/test/TestMain.java


To Run this project:
--------------------

Install maven on your local machine.

Fork this repo and clone it to your local machine.

In the command prompt, traverse to this repo.

> mvn clean package

> mvn exec:java -Dexec.mainClass="test.Main"
