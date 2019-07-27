# The-Twins-Server
This is a NetBeans project written in Java programming language. The Twins protocol provides clients with access to a service allowing them to register their name and date of birth in an online database and learn the names of other people who share the same birthday. For the purposes of this service, a "twin" is any other person sharing the same day and month of birth (the year is ignored). The database is a local .txt file (will be created if it does not yet exist) and stores all the registered names and their date of birth.

The server is single-threaded, it will not be possible for more than one client to connect at a time. In this case, the refresh option is not much use to the client (since the database cannot be updated by any other client until the currently connected client quits) but refresh is implemented.

Also, it can be converted to multi-threaded server (check how, in the comments at line 151) as several connections can be active at the same time. In that case, the user cannot login if his name already exist in the database.

A pdf file with usage samples and a pdf file with the server protocol are provided.

# Connect to the server
We use Putty to connect to this server which is a graphical interface. Steps: 
1. Run Server.java from Twins\src\twins
2. Download-install-open Putty
3. Select the Connection type
4. Complete the Host and Port fields as required (you can find them from the Netbeans console)
5. Click Open.

Link to download: https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html

# Video link
Under construction
