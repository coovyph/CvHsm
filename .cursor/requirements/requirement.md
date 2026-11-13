# Project Brief
I would like to create an application that convert incoming gRPC message and convert it into Thales HSM message and send the request into Thales HSM Machine. When the application receive HSM response, it will convert back the response in to gRPC message

# Project requirements
1. The HSM machine is Thales 10K
2. The connection between application to HSM is TCP IP connection. The application may use same connection session to send multiple requests. The Application must have mechanism to match the request and the response.
3. The incoming message is gRPC.
4. Use java, spring boot and netty
5. Use thaleshostcommand.pdf as message format reference

#Tasks
[] Develop TCP/IP communication.
[] Develop HSM message object.
[] Develop echo-test scheduler.


# Develop TCP/IP communication
1. The Application will act as client, and allowed to open multiple connection sessions to HSM.
2. One socket connection session are reusable, application may send multiple request message on a session connection, do not disconnect the socket connection after use.
3. If Connection is disconnected, application must have a mechanism to reconnect to HSM machine.
4. The maximum session connection open is configurable on spring boot application properties.

The message format is : 
2 bytes MLI header + hsm message

the MLI header is 2 bytes that determine the maximum byte length for hsm message. the length is exclude the header. For example if the length is 10, then hsm message expected length is 10 bytes.

# HSM message objects
The HSM message format is : 
## request 
4 char header + 2 char command code + body request

## response
4 char header + 2 char command code + 2 char of error code + body response

The header is random char, i would like to use header as correlation id between request and response. The header is expected to be unique

the command code is 2 char, the value is depend on HSM function
the body request and body response message format is depend on command code

# Echo Test Scheduler
Has functionallity to make sure the connection session between application and HSM machine is active.

Application will periodically send a echo command to HSM through every active socket sessions. The scheduler time is configurable

Please check at thaleshostcommand.pdf page 438 for detail specs. 
Please fill field "Data" with date and time information in long number format. 
