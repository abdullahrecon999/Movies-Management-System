# Movies-Management-System
Single user Movies management system created in JAVA with Client/Server architecture using TCP sockets. Uses Mongo Atlas to store data.
## Description
This is a Movies Management System which will helps the users in managing their movies. Users will be 
able to Add their favorite (or not so favorite) movies in the database with their preferred rating and the 
data will be stored in a cloud Database to be accessible anywhere. Users will be able to Add the Status 
of the movie either they have watched it, not watched it or yet to be watched and can search based on 
that parameter or on Title too. Users can easily add new Movies from the client interface provided.
The users can create an account and login to their menu. Each user will have a separate 
collection created in the main database in MongoDB. This way user data will be kept safe and secured.
The feature Send list to WhatsApp is also added. This way users can get their movie list on 
WhatsApp.
It has the following entities:
- Movie Manager
- User
- Movie
- Custom Packet
